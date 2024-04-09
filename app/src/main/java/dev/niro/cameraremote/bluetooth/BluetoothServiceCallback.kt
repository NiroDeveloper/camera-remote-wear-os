package dev.niro.cameraremote.bluetooth

import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothProfile
import android.util.Log

class BluetoothServiceCallback(val connectionStateChanged: () -> Unit) : BluetoothProfile.ServiceListener {

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

        connectionStateChanged()
    }

    private fun registerApp(registerHidDevice: BluetoothHidDevice): HidDeviceCallback {
        val newHidCallback = HidDeviceCallback(registerHidDevice, connectionStateChanged) { registered ->
            if (registered) {
                autoConnect(registerHidDevice)
            }
        }

        try {
            registerHidDevice.registerApp(
                BluetoothConstants.SPD_RECORD,
                null,
                BluetoothConstants.QOS_OUT,
                { it.run() },
                newHidCallback
            )

            Log.i(null, "Called BluetoothHidDevice.registerApp")
        } catch (ex: SecurityException) {
            Log.wtf(null, "Failed app registration: $ex")
        }

        return newHidCallback
    }

    fun autoConnect(connectHidDevice: BluetoothHidDevice) {
        val appRegistered = hidCallback?.appRegistered ?: false
        if (!appRegistered) {
            Log.e(null, "Bluetooth app is not registered")

            return
        }

        try {
            val connectionStates = intArrayOf(
                BluetoothProfile.STATE_CONNECTED,
                BluetoothProfile.STATE_CONNECTING,
                BluetoothProfile.STATE_DISCONNECTED,
                BluetoothProfile.STATE_DISCONNECTING
            )

            connectHidDevice.getDevicesMatchingConnectionStates(connectionStates).forEach { device ->
                Log.i(null, "Connect with device: ${device.getNameWithState(connectHidDevice)}")

                connectHidDevice.connect(device)
            }
        } catch (ex: SecurityException) {
            Log.wtf(null, "Failed auto connect: $ex")
        }
    }

}