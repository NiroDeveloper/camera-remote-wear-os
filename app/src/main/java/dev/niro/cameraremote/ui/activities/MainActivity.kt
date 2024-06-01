package dev.niro.cameraremote.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.ambient.AmbientLifecycleObserver
import androidx.wear.compose.material.HorizontalPageIndicator
import androidx.wear.compose.material.PageIndicatorState
import androidx.wear.tooling.preview.devices.WearDevices
import dev.niro.cameraremote.bluetooth.BluetoothController
import dev.niro.cameraremote.bluetooth.DeviceWrapper
import dev.niro.cameraremote.bluetooth.helper.BluetoothPermission
import dev.niro.cameraremote.interfaces.IUserInterfaceBluetoothCallback
import dev.niro.cameraremote.interfaces.IUserInterfaceTimerCallback
import dev.niro.cameraremote.ui.UserInputController
import dev.niro.cameraremote.ui.pages.DevicesLayout
import dev.niro.cameraremote.ui.pages.DevicesPage
import dev.niro.cameraremote.ui.pages.RemoteLayout
import dev.niro.cameraremote.ui.pages.RemotePage
import dev.niro.cameraremote.ui.pages.SettingsLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity(), AmbientLifecycleObserver.AmbientLifecycleCallback{

    private val ambientObserver = AmbientLifecycleObserver(this, this)

    private var isInAmbientMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        lifecycle.addObserver(ambientObserver)

        registerUiCallbacks()

        CoroutineScope(Dispatchers.Default).launch {
            RemotePage.updateUi()
            BluetoothController.init(this@MainActivity)
        }

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
            MainActivityLayout(permissionLauncher)
        }
    }

    private fun registerUiCallbacks() {
        BluetoothController.uiCallback = object : IUserInterfaceBluetoothCallback {
            override fun onConnectionStateChange(device: DeviceWrapper) {
                Log.d(null, "onConnectionStateChange($device)")

                RemotePage.updateUi()
                DevicesPage.updateDevice(device)
            }

            override fun onServiceError(message: Int) {
                val errorIntent = Intent(this@MainActivity, ErrorActivity::class.java)
                errorIntent.putExtra("messageId", message)
                startActivity(errorIntent)
            }

            override fun onServiceStateChange(available: Boolean) {
                Log.d(null, "onServiceStateChange($available)")

                RemotePage.updateUi()
            }
        }

        UserInputController.uiCallback = object : IUserInterfaceTimerCallback {
            override fun changeProgressIndicatorState(progress: Float) {
                RemotePage.progressIndicator.floatValue = progress
            }

            override fun isAmbientModeActive(): Boolean {
                return isInAmbientMode
            }
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

}

@OptIn(ExperimentalFoundationApi::class)
@Preview(device = WearDevices.RECT, showSystemUi = true)
@Preview(device = WearDevices.SQUARE, showSystemUi = true)
@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Preview(device = WearDevices.LARGE_ROUND, showSystemUi = true)
@Composable
fun MainActivityLayout(permissionLauncher: ActivityResultLauncher<String>? = null) {
    val pagerState = rememberPagerState { 3 }

    HorizontalPager(state = pagerState) {page ->
        when(page) {
            0 -> RemoteLayout(permissionLauncher)
            1 -> DevicesLayout()
            2 -> SettingsLayout()
        }
    }

    val pageIndicatorState: PageIndicatorState = remember {
        object : PageIndicatorState {
            override val pageOffset: Float
                get() = 0f
            override val selectedPage: Int
                get() = pagerState.currentPage
            override val pageCount: Int
                get() = pagerState.pageCount
        }
    }

    HorizontalPageIndicator(pageIndicatorState = pageIndicatorState, modifier = Modifier.padding(12.dp))
}