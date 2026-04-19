package dev.niro.cameraremote.bluetooth

import android.app.Activity
import android.bluetooth.BluetoothAdapter
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
    private var bluetoothAdapter: BluetoothAdapter? = null

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
        bluetoothCallback?.let { callback ->
            val hidDevice = callback.hidDevice

            callback.destroy()

            if (hidDevice != null) {
                val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                val bluetoothAdapter = bluetoothManager.adapter

                bluetoothAdapter.closeProfileProxy(BluetoothProfile.HID_DEVICE, hidDevice)
            }

            Log.i(null, "Destroyed bluetooth service")
        }

        bluetoothCallback = null
    }

    @WorkerThread
    fun isDeviceConnected() = bluetoothCallback?.isDeviceConnected() ?: false

    @WorkerThread
    fun takePicture() {
        Log.i(null, "Taking picture now")

        val callback = bluetoothCallback ?: return
        val localHidDevice = callback.hidDevice ?: return

        // Ensure app is registered before sending reports
        if (!callback.appRegistered) {
            Log.w(null, "App not registered, trying to re-register before taking picture")
            registerBluetoothService(null) // Context is not needed if already initialized
            return
        }

        try {
            val triggerKey = dev.niro.cameraremote.ui.UserInputController.triggerKey
            callback.getDevices(BluetoothProfile.STATE_CONNECTED).forEach { bluetoothDevice ->
                bluetoothDevice.sendKeyboardPress(localHidDevice, triggerKey)

                Log.i(null, "Sent report signal to device: ${bluetoothDevice.name}")
            }
        } catch (ex: SecurityException) {
            Log.wtf(null, "Failed sending report: $ex")
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
    fun registerBluetoothService(context: Context?) {
        synchronized(this) {
            bluetoothCallback?.let {
                if (!it.appRegistered && it.hidDevice != null) {
                    Log.i(null, "Service exists but app is not registered, re-registering...")
                    it.reRegisterApp()
                } else {
                    Log.w(null, "BluetoothService is already registered and active")
                }
                return
            }

            context?.let { ctx ->
                val bluetoothManager = ctx.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                val adapter = bluetoothManager.adapter
                bluetoothAdapter = adapter
                
                val newBluetoothCallback = BluetoothServiceCallback(adapter, uiCallbackProxy, uiCallbackProxy)

                val buildSuccessful = adapter.getProfileProxy(ctx, newBluetoothCallback, BluetoothProfile.HID_DEVICE)

                if (buildSuccessful) {
                    Log.i(null, "Registered bluetooth service callback for hid device")
                    bluetoothCallback = newBluetoothCallback
                } else {
                    Log.e(null, "BluetoothAdapter.getProfileProxy failed")
                    uiCallbackProxy.onServiceError(R.string.error_adapter_register)
                }
            }
        }
    }

    @WorkerThread
    fun connectDevice(device: DeviceWrapper) {
        val hostHidDevice = bluetoothCallback?.hidDevice ?: return
        val bluetoothDevice = bluetoothAdapter?.getRemoteDevice(device.address) ?: return

        Log.i(null, "Connecting to device: ${bluetoothDevice.toDebugString(hostHidDevice)}")

        try {
            val success = hostHidDevice.connect(bluetoothDevice)
            Log.d(null, "Connect call result: $success")
        } catch (ex: SecurityException) {
            Log.wtf(null, "Failed connect: $ex")
        }
    }

    @WorkerThread
    fun disconnectDevice(device: DeviceWrapper) {
        val hostHidDevice = bluetoothCallback?.hidDevice ?: return
        val bluetoothDevice = bluetoothAdapter?.getRemoteDevice(device.address) ?: return

        Log.i(null, "Disconnecting from device: ${bluetoothDevice.toDebugString(hostHidDevice)}")

        try {
            val success = hostHidDevice.disconnect(bluetoothDevice)
            Log.d(null, "Disconnect call result: $success")
        } catch (ex: SecurityException) {
            Log.wtf(null, "Failed disconnect: $ex")
        }
    }

}