package dev.niro.cameraremote.bluetooth

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.WorkerThread
import dev.niro.cameraremote.R
import dev.niro.cameraremote.bluetooth.enums.ConnectionState
import dev.niro.cameraremote.bluetooth.helper.BluetoothPermission
import dev.niro.cameraremote.bluetooth.helper.sendKeyboardPress
import dev.niro.cameraremote.bluetooth.helper.toDebugString
import dev.niro.cameraremote.bluetooth.helper.toDeviceWrapper
import dev.niro.cameraremote.interfaces.IConnectionStateCallback
import dev.niro.cameraremote.interfaces.IServiceStateCallback
import dev.niro.cameraremote.interfaces.IUserInterfaceBluetoothCallback
import dev.niro.cameraremote.ui.activities.BluetoothPermissionActivity

object BluetoothController {

    var uiCallback: IUserInterfaceBluetoothCallback? = null

    private var bluetoothCallback: BluetoothServiceCallback? = null

    private val uiCallbackProxy = object : IConnectionStateCallback, IServiceStateCallback {
        override fun onConnectionStateChange(device: BluetoothDevice, state: ConnectionState) {
            uiCallback?.onConnectionStateChange(device.toDeviceWrapper(state))
        }

        override fun onServiceStateChange(available: Boolean) {
            uiCallback?.onServiceStateChange(available)
        }

        override fun onServiceError(message: Int) {
            uiCallback?.onServiceError(message)
        }
    }

    @WorkerThread
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

    @WorkerThread
    fun isDeviceConnected() = bluetoothCallback?.isDeviceConnected() ?: false

    @WorkerThread
    fun takePicture() {
        Log.i(null, "Taking picture now")

        val localHidDevice = bluetoothCallback?.hidDevice ?: return

        try {
            bluetoothCallback?.getDevices()?.forEach { bluetoothDevice ->
                bluetoothDevice.sendKeyboardPress(localHidDevice, 40.toByte())

                Log.i(null, "Sent report signal to device: ${bluetoothDevice.name}")
            }
        } catch (ex: SecurityException) {
            Log.wtf(null, "Failed calling BluetoothHidDevice.unregisterApp(): $ex")
        }
    }

    @WorkerThread
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
                uiCallbackProxy.onServiceError(R.string.error_service_register)
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

    @WorkerThread
    fun registerBluetoothService(context: Context) {
        bluetoothCallback?.let {
            Log.w(null, "BluetoothService is already registered")
            return
        }

        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        val newBluetoothCallback = BluetoothServiceCallback(uiCallbackProxy, uiCallbackProxy)

        val buildSuccessful = bluetoothAdapter.getProfileProxy(context, newBluetoothCallback, BluetoothProfile.HID_DEVICE)

        if (buildSuccessful) {
            Log.i(null, "Registered bluetooth service callback for hid device")

            bluetoothCallback = newBluetoothCallback
        } else {
            Log.e(null, "BluetoothAdapter.getProfileProxy failed")

            uiCallbackProxy.onServiceError(R.string.error_adapter_register)
        }
    }

    @WorkerThread
    fun connectDevice(device: DeviceWrapper) {
        val hostHidDevice = bluetoothCallback?.hidDevice ?: return

        bluetoothCallback?.let { bluetoothCallback ->
            val hidDevice = bluetoothCallback.getDevices().firstOrNull { it.address == device.address } ?: return

            Log.i(null, "Connect with device: ${hidDevice.toDebugString(hostHidDevice)}")

            try {
                hostHidDevice.connect(hidDevice)
            } catch (ex: SecurityException) {
                Log.wtf(null, "Failed connect: $ex")
            }
        }
    }

    @WorkerThread
    fun disconnectDevice(device: DeviceWrapper) {
        val hostHidDevice = bluetoothCallback?.hidDevice ?: return

        bluetoothCallback?.let { bluetoothCallback ->
            val hidDevice = bluetoothCallback.getDevices().firstOrNull { it.address == device.address } ?: return

            Log.i(null, "Disconnect with device: ${hidDevice.toDebugString(hostHidDevice)}")

            try {
                hostHidDevice.disconnect(hidDevice)
            } catch (ex: SecurityException) {
                Log.wtf(null, "Failed disconnect: $ex")
            }
        }
    }

}