package den.niro.cameraremote.ui.pages

import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.OutlinedButton
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.tooling.preview.devices.WearDevices
import den.niro.cameraremote.R
import den.niro.cameraremote.bluetooth.BluetoothController
import den.niro.cameraremote.ui.UserInputController
import kotlin.math.roundToInt

@Preview(device = WearDevices.RECT, showSystemUi = true)
@Preview(device = WearDevices.SQUARE, showSystemUi = true)
@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Preview(device = WearDevices.LARGE_ROUND, showSystemUi = true)
@Composable
fun RemoteLayout(permissionLauncher: ActivityResultLauncher<String>? = null) {
    updateButtons()

    BluetoothController.uiConnectionUpdateListener = {
        Log.d(null, "Updating ui because of bluetooth state change")

        updateButtons()
    }

    BoxWithConstraints {
        TimeText()

        val horizontalPadding = (maxWidth.value * 0.1).roundToInt()
        val verticalPadding = (maxHeight.value * 0.1).roundToInt()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontalPadding.dp, verticalPadding.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TriggerButton(permissionLauncher)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DelayButton()
                ModeButton()
            }
        }
    }
}

val triggerButtonIcon = mutableIntStateOf(0)
val timerDelayText = mutableStateOf("")
val modeTextDecoration = mutableStateOf(TextDecoration.None)

fun updateButtons() {
    triggerButtonIcon.intValue = if (!BluetoothController.isDeviceConnected()) {
        R.drawable.baseline_bluetooth_24
    } else if (UserInputController.autoTriggerEnabled) {
        if (UserInputController.autoTriggerActive) {
            R.drawable.sharp_autostop_24
        } else {
            R.drawable.sharp_autoplay_24
        }
    } else {
        R.drawable.baseline_linked_camera_24
    }

    timerDelayText.value = "${UserInputController.timerDelay}s"

    modeTextDecoration.value = if (UserInputController.autoTriggerEnabled)
        TextDecoration.Underline else TextDecoration.LineThrough
}

@Composable
fun TriggerButton(permissionLauncher: ActivityResultLauncher<String>?) {
    val context = LocalContext.current
    val triggerButtonIcon by triggerButtonIcon

    Button(
        onClick = {
            if (BluetoothController.isDeviceConnected()) {
                UserInputController.clickTrigger(context)
                updateButtons()
            } else {
                permissionLauncher?.let {
                    BluetoothController.handleBluetooth(context, it)
                }
            }
        },
        modifier = Modifier
            .width(80.dp)
            .aspectRatio(1.0f),
    ) {
        Icon(
            painter = painterResource(id = triggerButtonIcon),
            contentDescription = stringResource(id = R.string.camera_trigger),
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp)
        )
    }
}

@Composable
fun DelayButton() {
    val timerDelayText by timerDelayText

    OutlinedButton(
        onClick = {
            UserInputController.toggleTimer()
            updateButtons()
        },
        modifier = Modifier
            .width(50.dp)
            .aspectRatio(1.0f),
    ) {
        Text(text = timerDelayText)
    }
}

@Composable
fun ModeButton() {
    val textDecoration by modeTextDecoration

    OutlinedButton(
        onClick = {
            UserInputController.toggleAutoTrigger()
            updateButtons()
        },
        modifier = Modifier
            .width(50.dp)
            .aspectRatio(1.0f),
    ) {
        Text(text = "Auto", textDecoration = textDecoration)
    }
}