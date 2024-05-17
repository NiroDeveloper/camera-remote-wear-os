package dev.niro.cameraremote.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import dev.niro.cameraremote.R
import dev.niro.cameraremote.bluetooth.helper.BluetoothPermission

class BluetoothPermissionActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Must be created at activity startup, otherwise the app will crash.
        val permissionLauncher = BluetoothPermission.buildPermissionLauncher(this) { permissionGranted ->
            Log.d(null, "Bluetooth permission granted: $permissionGranted")

            if (permissionGranted) {
                finish()
            } else {
                val errorIntent = Intent(this, ErrorActivity::class.java)
                errorIntent.putExtra("messageId", R.string.error_bluetooth_permission_denied)
                this.startActivity(errorIntent)
            }
        }

        setContent {
            BluetoothPermissionLayout {
                Log.i(null, "Requesting bluetooth permission")

                BluetoothPermission.requestBluetoothPermission(permissionLauncher)
            }
        }
    }
}


@Preview(device = WearDevices.RECT, showSystemUi = true)
@Preview(device = WearDevices.SQUARE, showSystemUi = true)
@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Preview(device = WearDevices.LARGE_ROUND, showSystemUi = true)
@Composable
fun BluetoothPermissionLayout(onOk: (() -> Unit)? = null) {
    val scalingState = rememberScalingLazyListState(0, 100)

    PositionIndicator(scalingLazyListState = scalingState)

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = scalingState
    ) {
        item {
            Icon(
                painter = painterResource(id = R.drawable.baseline_info_24),
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
        }

        item {
            Text(
                text = stringResource(id = R.string.bluetooth_permission_description),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }

        item {
            Button(
                onClick = {
                    if (onOk != null) {
                        onOk()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(text = stringResource(id = R.string.ok))
            }
        }
    }
}