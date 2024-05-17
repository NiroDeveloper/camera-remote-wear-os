package dev.niro.cameraremote.bluetooth

import android.app.Activity
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import dev.niro.cameraremote.R
import dev.niro.cameraremote.bluetooth.helper.BluetoothPermission
import dev.niro.cameraremote.bluetooth.helper.sendKeyboardPress
import dev.niro.cameraremote.interfaces.IConnectionStateCallback
import dev.niro.cameraremote.interfaces.IUserInterfaceCallback
import dev.niro.cameraremote.ui.activities.BluetoothPermissionActivity

object BluetoothController {

    var uiCallback: IUserInterfaceCallback? = null

    private var bluetoothCallback: BluetoothServiceCallback? = null

    private val uiCallbackProxy = object : IConnectionStateCallback {
        override fun onConnectionStateChanged(connected: Boolean) {
            uiCallback?.onConnectionStateChanged(connected)
        }
        override fun onConnectionError(message: Int) {
            uiCallback?.onConnectionError(message)
        }
    }

    fun init(context: Context) {
        if (!BluetoothPermission.hasBluetoothPermission(context)) {
            return
        }

        registerBluetoothService(context)
    }

    fun destroy(context: Context) {
        bluetoothCallback?.hidDevice?.let {
            try {
                it.unregisterApp()
            } catch (ex: SecurityException) {
                Log.wtf(null, "Failed calling BluetoothHidDevice.unregisterApp(): $ex")
            }

            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val bluetoothAdapter = bluetoothManager.adapter

            bluetoothAdapter.closeProfileProxy(BluetoothProfile.HID_DEVICE, it)

            Log.i(null, "Destroyed bluetooth service")
        }
    }

    fun handleBluetooth(activity: Activity, permissionLauncher: ActivityResultLauncher<String>) {
        if (BluetoothPermission.hasBluetoothPermission(activity)) {
            val localBluetoothCallback = bluetoothCallback
            if (localBluetoothCallback == null) {
                registerBluetoothService(activity)
                return
            }

            val localHidDevice = localBluetoothCallback.hidDevice
            if (localHidDevice == null) {
                Log.e(null, "Bluetooth service is not connected")
                uiCallbackProxy.onConnectionError(R.string.error_service_register)
            } else {
                localBluetoothCallback.startAutoConnect()
            }

            return
        }

        if (BluetoothPermission.shouldShowPermissionDescription(activity)) {
            val bluetoothPermissionIntent = Intent(activity, BluetoothPermissionActivity::class.java)
            activity.startActivity(bluetoothPermissionIntent)

            return
        }

        Log.i(null, "Requesting bluetooth permission")
        BluetoothPermission.requestBluetoothPermission(permissionLauncher)
    }

    fun registerBluetoothService(context: Context) {
        bluetoothCallback?.let {
            Log.w(null, "BluetoothService is already registered")
            return
        }

        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        val bluetoothCallback = BluetoothServiceCallback(uiCallbackProxy)

        val buildSuccessful = bluetoothAdapter.getProfileProxy(context, bluetoothCallback, BluetoothProfile.HID_DEVICE)

        if (buildSuccessful) {
            Log.i(null, "Registered bluetooth service callback for hid device")

            this.bluetoothCallback = bluetoothCallback
        } else {
            Log.e(null, "BluetoothAdapter.getProfileProxy failed")

            uiCallbackProxy.onConnectionError(R.string.error_adapter_register)
        }
    }

    fun isDeviceConnected() = bluetoothCallback?.isDeviceConnected() ?: false

    fun takePicture() {
        Log.i(null, "Taking picture now")

        try {
            val hidDevice = bluetoothCallback?.hidDevice
            val connectedDevices = hidDevice?.connectedDevices

            connectedDevices?.forEach { bluetoothDevice ->
                bluetoothDevice.sendKeyboardPress(hidDevice, 40.toByte())

                Log.i(null, "Sent report signal to device: ${bluetoothDevice.name}")
            }
        } catch (ex: SecurityException) {
            Log.wtf(null, "Failed calling BluetoothHidDevice.unregisterApp(): $ex")
        }
    }

}