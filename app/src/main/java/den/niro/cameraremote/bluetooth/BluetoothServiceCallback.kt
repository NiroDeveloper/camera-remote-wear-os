package den.niro.cameraremote.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothProfile
import android.util.Log

class BluetoothServiceCallback(val connectionStateChanged: () -> Unit) : BluetoothProfile.ServiceListener {

    var hidDevice: BluetoothHidDevice? = null
        private set

    var hidCallback: HidDeviceCallback? = null
        private set

    override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
        Log.i(null, "onServiceConnected($profile, $proxy)")

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
        registerApp()
    }

    override fun onServiceDisconnected(profile: Int) {
        Log.d(null, "onServiceDisconnected($profile)")

        hidDevice = null
        hidCallback = null

        connectionStateChanged()
    }

    private fun registerApp() {
        val localHidDevice = hidDevice
        if (localHidDevice == null) {
            Log.w(null, "BluetoothHidDevice is not available")

            return
        }

        try {
            hidCallback = HidDeviceCallback(localHidDevice, connectionStateChanged) {
                hidCallback = null
            }

            val registerAppResponse = localHidDevice.registerApp(
                BluetoothConstants.SPD_RECORD,
                null,
                BluetoothConstants.QOS_OUT,
                { it.run() },
                hidCallback
            )

            Log.i(null, "Called BluetoothHidDevice.registerApp and received response: $registerAppResponse")

            autoConnect()
        } catch (ex: SecurityException) {
            Log.wtf(null, "Failed app registration: $ex")
        }
    }

    fun autoConnect() {
        val localHidDevice = hidDevice
        if (localHidDevice == null) {
            Log.w(null, "BluetoothHidDevice is not available")

            return
        }

        if (hidCallback == null) {
            Log.e(null, "No hid callback, so the app is not registered")

            return
        }

        try {
            val connectionStates = intArrayOf(
                BluetoothProfile.STATE_CONNECTED,
                BluetoothProfile.STATE_CONNECTING,
                BluetoothProfile.STATE_DISCONNECTED,
                BluetoothProfile.STATE_DISCONNECTING
            )

            localHidDevice.getDevicesMatchingConnectionStates(connectionStates).forEach { device ->
                val state = when(localHidDevice.getConnectionState(device)) {
                    BluetoothProfile.STATE_CONNECTED -> "STATE_CONNECTED"
                    BluetoothProfile.STATE_CONNECTING -> "STATE_CONNECTING"
                    BluetoothProfile.STATE_DISCONNECTED -> "STATE_DISCONNECTED"
                    BluetoothProfile.STATE_DISCONNECTING -> "STATE_DISCONNECTING"
                    else -> "STATE_UNKNOWN"
                }

                val bond = when(device.bondState) {
                    BluetoothDevice.BOND_BONDED -> "BOND_BONDED"
                    BluetoothDevice.BOND_BONDING -> "BOND_BONDING"
                    BluetoothDevice.BOND_NONE -> "BOND_NONE"
                    else -> "BOND_UNKNOWN"
                }

                Log.i(null, "Connect with device: ${device.name} ($state, $bond)")

                localHidDevice.connect(device)
            }
        } catch (ex: SecurityException) {
            Log.wtf(null, "Failed auto connect: $ex")
        }
    }

}