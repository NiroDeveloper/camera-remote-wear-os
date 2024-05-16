package dev.niro.cameraremote.bluetooth.helper

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothProfile
import android.util.Log

fun BluetoothDevice.getStateName(hidDevice: BluetoothHidDevice): String {
    return try {
        val state = hidDevice.getConnectionState(this)

        when (state) {
            BluetoothProfile.STATE_CONNECTED -> "STATE_CONNECTED"
            BluetoothProfile.STATE_CONNECTING -> "STATE_CONNECTING"
            BluetoothProfile.STATE_DISCONNECTED -> "STATE_DISCONNECTED"
            BluetoothProfile.STATE_DISCONNECTING -> "STATE_DISCONNECTING"
            else -> "STATE_UNKNOWN"
        }
    } catch (ex: SecurityException) {
        Log.e(null, "Failed BluetoothHidDevice.getConnectionState: $ex")

        "STATE_UNKNOWN"
    }
}

fun BluetoothDevice.getBondStateName(): String {
    return try {
        when (this.bondState) {
            BluetoothDevice.BOND_BONDED -> "BOND_BONDED"
            BluetoothDevice.BOND_BONDING -> "BOND_BONDING"
            BluetoothDevice.BOND_NONE -> "BOND_NONE"
            else -> "BOND_UNKNOWN"
        }
    } catch (ex: SecurityException) {
        Log.e(null, "Failed BluetoothDevice.bondState: $ex")

        "BOND_UNKNOWN"
    }
}

fun BluetoothDevice.getNameWithState(hidDevice: BluetoothHidDevice): String {
    val deviceName = try {
        this.name
    } catch (ex: SecurityException) {
        Log.e(null, "Failed BluetoothDevice.name: $ex")

        "NAME_UNKNOWN"
    }

    return "$deviceName (${this.getStateName(hidDevice)}, ${this.getBondStateName()})"
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