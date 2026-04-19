package dev.niro.cameraremote.ui.dialogs

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.RadioButton
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material.dialog.Dialog
import dev.niro.cameraremote.R
import dev.niro.cameraremote.bluetooth.enums.TriggerKey
import dev.niro.cameraremote.ui.UserInputController
import dev.niro.cameraremote.ui.pages.SettingsPage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun TriggerKeySelectionDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        showDialog = showDialog,
        onDismissRequest = onDismiss
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                ListHeader {
                    Text(
                        text = stringResource(id = R.string.trigger_key),
                        textAlign = TextAlign.Center
                    )
                }
            }

            val triggerKeys = TriggerKey.entries

            triggerKeys.forEach { key ->
                item {
                    var currentTriggerKey by remember { SettingsPage.triggerKey }
                    ToggleChip(
                        checked = currentTriggerKey == key.keyCode.toInt(),
                        onCheckedChange = {
                            if (it) {
                                currentTriggerKey = key.keyCode.toInt()
                                UserInputController.triggerKey = key
                                CoroutineScope(Dispatchers.Default).launch { 
                                    SettingsPage.writeSettings(context) 
                                }
                                onDismiss()
                            }
                        },
                        label = { Text(stringResource(id = key.stringRes)) },
                        toggleControl = { RadioButton(selected = currentTriggerKey == key.keyCode.toInt()) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}