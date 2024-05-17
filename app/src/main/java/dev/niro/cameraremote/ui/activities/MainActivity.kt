package dev.niro.cameraremote.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.ambient.AmbientLifecycleObserver
import dev.niro.cameraremote.bluetooth.BluetoothController
import dev.niro.cameraremote.bluetooth.helper.BluetoothPermission
import dev.niro.cameraremote.interfaces.IAmbientModeState
import dev.niro.cameraremote.ui.pages.RemoteLayout
import dev.niro.cameraremote.ui.pages.RemotePage

class MainActivity : ComponentActivity(), AmbientLifecycleObserver.AmbientLifecycleCallback, IAmbientModeState {

    private val ambientObserver = AmbientLifecycleObserver(this, this)

    private var isInAmbientMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        lifecycle.addObserver(ambientObserver)

        BluetoothController.init(this)
        RemotePage.registerCallbacks(this)
        RemotePage.updateButtons()

        // Must be created at activity startup, otherwise the app will crash.
        val permissionLauncher = BluetoothPermission.buildPermissionLauncher(this) { permissionGranted ->
            Log.d(null, "Bluetooth permission granted: $permissionGranted")

            if (permissionGranted) {
                BluetoothController.registerBluetoothService(this)
            } else {
                val bluetoothPermissionIntent = Intent(this, BluetoothPermissionActivity::class.java)
                this.startActivity(bluetoothPermissionIntent)
            }
        }

        setContent {
            RemoteLayout(permissionLauncher)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(ambientObserver)

        BluetoothController.destroy(this)
    }

    override fun onEnterAmbient(ambientDetails: AmbientLifecycleObserver.AmbientDetails) {
        isInAmbientMode = true
    }

    override fun onExitAmbient() {
        isInAmbientMode = false
    }

    override fun isAmbientModeActive(): Boolean {
        return isInAmbientMode
    }
}