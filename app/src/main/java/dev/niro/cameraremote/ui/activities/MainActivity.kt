package dev.niro.cameraremote.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.horologist.compose.ambient.AmbientAware
import dev.niro.cameraremote.bluetooth.BluetoothController
import dev.niro.cameraremote.bluetooth.helper.BluetoothPermission
import dev.niro.cameraremote.ui.pages.RemoteLayout

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        BluetoothController.init(this)

        // Must be created at activity startup, otherwise the app will crash.
        val permissionLauncher = BluetoothPermission.buildPermissionLauncher(this)

        setContent {
            AmbientAware {
                RemoteLayout(permissionLauncher)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        BluetoothController.destroy(this)
    }
}