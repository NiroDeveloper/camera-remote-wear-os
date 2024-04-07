package den.niro.cameraremote.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.horologist.compose.ambient.AmbientAware
import den.niro.cameraremote.bluetooth.BluetoothController
import den.niro.cameraremote.bluetooth.BluetoothPermission
import den.niro.cameraremote.ui.pages.RemoteLayout
import kotlinx.coroutines.DelicateCoroutinesApi

class MainActivity : ComponentActivity() {

    @DelicateCoroutinesApi
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