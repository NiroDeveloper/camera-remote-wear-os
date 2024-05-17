package dev.niro.cameraremote.bluetooth.helper

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object BluetoothPermission {

    fun hasBluetoothPermission(context: Context): Boolean {
        val bluetoothPermission = getBluetoothPermissionName()
        val permissionResult = ContextCompat.checkSelfPermission(context, bluetoothPermission)

        return permissionResult == PackageManager.PERMISSION_GRANTED
    }

    fun buildPermissionLauncher(activity: ComponentActivity, callback: (Boolean) -> Unit): ActivityResultLauncher<String> {
        return activity.registerForActivityResult(ActivityResultContracts.RequestPermission(), callback)
    }

    fun requestBluetoothPermission(permissionLauncher: ActivityResultLauncher<String>) {
        val bluetoothPermission = getBluetoothPermissionName()

        permissionLauncher.launch(bluetoothPermission)
    }

    /**
     * Requirement from android.
     * https://developer.android.com/training/permissions/requesting?hl=de#explain
     */
    fun shouldShowPermissionDescription(activity: Activity): Boolean {
        val bluetoothPermission = getBluetoothPermissionName()

        return ActivityCompat.shouldShowRequestPermissionRationale(activity, bluetoothPermission)
    }

    private fun getBluetoothPermissionName(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Manifest.permission.BLUETOOTH_CONNECT
        } else {
            Manifest.permission.BLUETOOTH
        }
    }

}