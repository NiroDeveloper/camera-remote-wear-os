package dev.niro.cameraremote.ui.pages

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import androidx.wear.tooling.preview.devices.WearDevices
import dev.niro.cameraremote.BuildConfig
import dev.niro.cameraremote.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object SettingsPage {

    private val VIBRATION_ENABLED_KEY = booleanPreferencesKey("vibration_enabled")
    private val Context.settingsPreferencesDataStore: DataStore<Preferences> by preferencesDataStore("settings")

    var vibrationEnabled = mutableStateOf(true)

    suspend fun loadSettings(context: Context) {
        val dataStore = context.settingsPreferencesDataStore.data.first()

        dataStore[VIBRATION_ENABLED_KEY]?.let {
            vibrationEnabled.value = it
        }
    }

    suspend fun writeSettings(context: Context) {
        context.settingsPreferencesDataStore.edit { preferences ->
            preferences[VIBRATION_ENABLED_KEY] = vibrationEnabled.value
        }
    }

}

@Preview(device = WearDevices.RECT, showSystemUi = true)
@Preview(device = WearDevices.SQUARE, showSystemUi = true)
@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Preview(device = WearDevices.LARGE_ROUND, showSystemUi = true)
@Composable
fun SettingsLayout() {
    val context = LocalContext.current

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {

        item {
            Text(
                text = stringResource(id = R.string.settings),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }

        item {
            var vibrationEnabled by remember { SettingsPage.vibrationEnabled }

            ToggleChip(
                checked = vibrationEnabled,
                onCheckedChange = {
                    vibrationEnabled = it

                    CoroutineScope(Dispatchers.Default).launch { SettingsPage.writeSettings(context) }
                },
                label = { Text(stringResource(id = R.string.vibration)) },
                toggleControl = { Switch(checked = vibrationEnabled) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Text(
                text = "v${BuildConfig.VERSION_NAME}",
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }
    }
}