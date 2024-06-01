package dev.niro.cameraremote.bluetooth.helper

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.util.Log
import dev.niro.cameraremote.bluetooth.DeviceWrapper
import dev.niro.cameraremote.bluetooth.enums.BondState
import dev.niro.cameraremote.bluetooth.enums.ConnectionState

fun BluetoothDevice.getConnectionStateEnum(hidDevice: BluetoothHidDevice): ConnectionState {
    return try {
        val state = hidDevice.getConnectionState(this)

        ConnectionState.fromBluetoothProfile(state)
    } catch (ex: SecurityException) {
        Log.e(null, "Failed BluetoothHidDevice.getConnectionState: $ex")

        ConnectionState.ERROR
    }
}

fun BluetoothDevice.getBondStateEnum(): BondState {
    return try {
        BondState.fromBluetoothDevice(this.bondState)
    } catch (ex: SecurityException) {
        Log.e(null, "Failed BluetoothDevice.bondState: $ex")

        BondState.ERROR
    }
}

fun BluetoothDevice.getNameString(): String {
    return try {
        this.name
    } catch (ex: SecurityException) {
        Log.e(null, "Failed BluetoothDevice.name: $ex")

        "NAME_ERROR"
    }
}

fun BluetoothDevice.getAddressString(): String {
    return try {
        this.address
    } catch (ex: SecurityException) {
        Log.e(null, "Failed BluetoothDevice.address: $ex")

        "ADDRESS_ERROR"
    }
}

fun BluetoothDevice.toDeviceWrapper(hidDevice: BluetoothHidDevice): DeviceWrapper {
    return toDeviceWrapper(getConnectionStateEnum(hidDevice))
}

fun BluetoothDevice.toDeviceWrapper(state: ConnectionState): DeviceWrapper {
    return DeviceWrapper(getAddressString(), getNameString(), state, getBondStateEnum())
}

fun BluetoothDevice.toDebugString(hidDevice: BluetoothHidDevice): String {
    return "${this.getAddressString()} (${this.getConnectionStateEnum(hidDevice).name}, ${this.getBondStateEnum().name})"
}

fun BluetoothDevice.sendKeyboardPress(hidDevice: BluetoothHidDevice, key: Byte) {
    try {
        hidDevice.sendReport(
            this,
            BluetoothConstants.ID_KEYBOARD.toInt(),
            byteArrayOf(0x0, 0x0, key)
        )

        hidDevice.sendReport(
            this,
            BluetoothConstants.ID_KEYBOARD.toInt(),
            byteArrayOf(0x0, 0x0, 0x0)
        )
    } catch (ex: SecurityException) {
        Log.wtf(null, "Failed sending device input: $ex")
    }
}