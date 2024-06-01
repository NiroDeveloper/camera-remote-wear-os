package dev.niro.cameraremote.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.util.Log
import dev.niro.cameraremote.bluetooth.enums.ConnectionState
import dev.niro.cameraremote.interfaces.IConnectionStateCallback
import dev.niro.cameraremote.interfaces.IServiceStateCallback

class HidDeviceCallback(
    private val hidDevice: BluetoothHidDevice,
    private val connectionStateListener: IConnectionStateCallback,
    private val serviceStateListener: IServiceStateCallback
) : BluetoothHidDevice.Callback() {

    override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
        super.onAppStatusChanged(pluggedDevice, registered)

        Log.d(null, "onAppStatusChanged($pluggedDevice, $registered)")

        serviceStateListener.onServiceStateChange(registered)
    }

    override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
        super.onConnectionStateChanged(device, state)

        Log.d(null, "onConnectionStateChanged($device, $state)")

        device?.let {
            connectionStateListener.onConnectionStateChange(it, ConnectionState.fromBluetoothProfile(state))
        }
    }

    override fun onGetReport(device: BluetoothDevice?, type: Byte, id: Byte, bufferSize: Int) {
        super.onGetReport(device, type, id, bufferSize)

        Log.d(null, "onGetReport($device, $type, $id, $bufferSize)")

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

        Log.d(null, "onSetReport($device, $type, $id, $data)")

        try {
            hidDevice.reportError(device, BluetoothHidDevice.ERROR_RSP_SUCCESS)
        } catch (ex: SecurityException) {
            Log.wtf(null, "Failed reportError: $ex")
        }
    }

    override fun onSetProtocol(device: BluetoothDevice?, protocol: Byte) {
        super.onSetProtocol(device, protocol)

        Log.d(null, "onSetProtocol($device, $protocol)")
    }

    override fun onInterruptData(device: BluetoothDevice?, reportId: Byte, data: ByteArray?) {
        super.onInterruptData(device, reportId, data)

        Log.d(null, "onInterruptData($device, $reportId, $data)")
    }

    override fun onVirtualCableUnplug(device: BluetoothDevice?) {
        super.onVirtualCableUnplug(device)

        Log.d(null, "onVirtualCableUnplug($device)")
    }

}