package den.niro.cameraremote.bluetooth

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

object BluetoothPermission {

    fun hasBluetoothPermission(context: Context): Boolean {
        val bluetoothPermission = getBluetoothPermissionName()
        val permissionResult = ContextCompat.checkSelfPermission(context, bluetoothPermission)

        return permissionResult == PackageManager.PERMISSION_GRANTED
    }

    fun buildPermissionLauncher(activity: ComponentActivity): ActivityResultLauncher<String> {
        return activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { permissionGranted ->
            if (permissionGranted) {
                BluetoothController.registerBluetoothService(activity)
            }
        }
    }

    fun requestBluetoothPermission(permissionLauncher: ActivityResultLauncher<String>) {
        val bluetoothPermission = getBluetoothPermissionName()

        permissionLauncher.launch(bluetoothPermission)
    }

    private fun getBluetoothPermissionName(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Manifest.permission.BLUETOOTH_CONNECT
        } else {
            Manifest.permission.BLUETOOTH_ADMIN
        }
    }

}