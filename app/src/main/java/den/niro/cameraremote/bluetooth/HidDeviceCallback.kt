package den.niro.cameraremote.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothProfile
import android.util.Log

class HidDeviceCallback(private val hidDevice: BluetoothHidDevice, val connectionStateChanged: () -> Unit, val appDisconnect: () -> Unit) :
    BluetoothHidDevice.Callback() {

    val connectedDevices = mutableListOf<BluetoothDevice>()

    override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
        super.onAppStatusChanged(pluggedDevice, registered)

        Log.i(null, "onAppStatusChanged($pluggedDevice, $registered)")

        if (!registered) {
            appDisconnect()
        }
    }

    override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
        super.onConnectionStateChanged(device, state)

        Log.i(null, "onConnectionStateChanged($device, $state)")

        if (device != null) {
            if (state == BluetoothProfile.STATE_CONNECTED) {
                connectedDevices.add(device)
            } else {
                connectedDevices.remove(device)
            }

            connectionStateChanged()
        }
    }

    override fun onGetReport(device: BluetoothDevice?, type: Byte, id: Byte, bufferSize: Int) {
        super.onGetReport(device, type, id, bufferSize)

        Log.i(null, "onGetReport($device, $type, $id, $bufferSize)")

        try {
            if (type == BluetoothHidDevice.REPORT_TYPE_INPUT) {
                hidDevice.replyReport(device, type, id, byteArrayOf(0, 0, 0))
            } else {
                hidDevice.reportError(device, BluetoothHidDevice.ERROR_RSP_UNSUPPORTED_REQ)
            }
        } catch (ex: SecurityException) {
            Log.wtf(null, "Failed replyReport or reportError: $ex")
        }
    }

    override fun onSetReport(device: BluetoothDevice?, type: Byte, id: Byte, data: ByteArray?) {
        super.onSetReport(device, type, id, data)

        Log.i(null, "onSetReport($device, $type, $id, $data)")

        try {
            hidDevice.reportError(device, BluetoothHidDevice.ERROR_RSP_SUCCESS)
        } catch (ex: SecurityException) {
            Log.wtf(null, "Failed reportError: $ex")
        }
    }

    override fun onSetProtocol(device: BluetoothDevice?, protocol: Byte) {
        super.onSetProtocol(device, protocol)

        Log.i(null, "onSetProtocol($device, $protocol)")
    }

    override fun onInterruptData(device: BluetoothDevice?, reportId: Byte, data: ByteArray?) {
        super.onInterruptData(device, reportId, data)

        Log.i(null, "onInterruptData($device, $reportId, $data)")
    }

    override fun onVirtualCableUnplug(device: BluetoothDevice?) {
        super.onVirtualCableUnplug(device)

        Log.i(null, "onVirtualCableUnplug($device)")
    }

}