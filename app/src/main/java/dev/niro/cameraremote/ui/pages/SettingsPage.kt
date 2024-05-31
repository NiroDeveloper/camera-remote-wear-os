package dev.niro.cameraremote.ui.pages

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import androidx.wear.tooling.preview.devices.WearDevices
import dev.niro.cameraremote.BuildConfig
import dev.niro.cameraremote.R

object SettingsPage {

    var vibrationEnabled = mutableStateOf(true)

}

@Preview(device = WearDevices.RECT, showSystemUi = true)
@Preview(device = WearDevices.SQUARE, showSystemUi = true)
@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Preview(device = WearDevices.LARGE_ROUND, showSystemUi = true)
@Composable
fun SettingsLayout() {
    val scalingState = rememberScalingLazyListState()

    PositionIndicator(scalingLazyListState = scalingState)

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = scalingState
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
            var vibrationEnabled by remember { mutableStateOf(true) }

            ToggleChip(
                checked = vibrationEnabled,
                onCheckedChange = { vibrationEnabled = it },
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