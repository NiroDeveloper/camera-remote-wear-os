package dev.niro.cameraremote.bluetooth

import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothProfile
import android.util.Log
import dev.niro.cameraremote.R
import dev.niro.cameraremote.bluetooth.helper.BluetoothConstants
import dev.niro.cameraremote.bluetooth.helper.getNameWithState
import dev.niro.cameraremote.interfaces.IAppStateCallback
import dev.niro.cameraremote.interfaces.IConnectionStateCallback

class BluetoothServiceCallback(private val connectionStateListener: IConnectionStateCallback) : BluetoothProfile.ServiceListener {

    var hidDevice: BluetoothHidDevice? = null
        private set

    var hidCallback: HidDeviceCallback? = null
        private set

    override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
        Log.d(null, "onServiceConnected($profile, $proxy)")

        if (profile != BluetoothProfile.HID_DEVICE) {
            Log.wtf(null, "BluetoothProfile is not a HidDevice")
            return
        }

        if (proxy == null) {
            Log.wtf(null, "No proxy received as HidDevice")
            return
        }

        if (proxy !is BluetoothHidDevice) {
            Log.wtf(null, "BluetoothProfile is not a BluetoothHidDevice instance")
            return
        }

        hidDevice = proxy
        hidCallback = registerApp(proxy)
    }

    override fun onServiceDisconnected(profile: Int) {
        Log.d(null, "onServiceDisconnected($profile)")

        hidDevice = null
        hidCallback = null

        connectionStateListener.onConnectionStateChanged(false)
    }

    fun isDeviceConnected() = hidCallback?.isDeviceConnected() ?: false

    private fun registerApp(registerHidDevice: BluetoothHidDevice): HidDeviceCallback {
        val appStateListener = object : IAppStateCallback {
            override fun onAppStateChanged(registered: Boolean) {
                if (registered) {
                    startAutoConnect()
                } else {
                    connectionStateListener.onConnectionStateChanged(false)
                }
            }
        }

        val newHidCallback = HidDeviceCallback(registerHidDevice, connectionStateListener, appStateListener)

        try {
            registerHidDevice.registerApp(
                BluetoothConstants.SPD_RECORD,
                null,
                BluetoothConstants.QOS_OUT,
                Runnable::run,
                newHidCallback
            )

            Log.i(null, "Called BluetoothHidDevice.registerApp")
        } catch (ex: SecurityException) {
            Log.wtf(null, "Failed app registration: $ex")
        }

        return newHidCallback
    }

    fun startAutoConnect() {
        val connectHidDevice = hidDevice
        if (connectHidDevice == null) {
            Log.e(null, "Bluetooth service is not connected")
            connectionStateListener.onConnectionError(R.string.error_service_register)

            return
        }

        val appRegistered = hidCallback?.appRegistered ?: false
        if (!appRegistered) {
            Log.e(null, "Bluetooth app is not registered")
            connectionStateListener.onConnectionError(R.string.error_app_register)

            return
        }

        try {
            val connectionStates = intArrayOf(BluetoothProfile.STATE_DISCONNECTED)
            val devices = connectHidDevice.getDevicesMatchingConnectionStates(connectionStates)

            if (devices.isEmpty()) {
                connectionStateListener.onConnectionError(R.string.error_no_devices_found)

                return
            }

            devices.forEach { device ->
                Log.i(null, "Connect with device: ${device.getNameWithState(connectHidDevice)}")

                connectHidDevice.connect(device)
            }
        } catch (ex: SecurityException) {
            Log.wtf(null, "Failed auto connect: $ex")
        }
    }

}