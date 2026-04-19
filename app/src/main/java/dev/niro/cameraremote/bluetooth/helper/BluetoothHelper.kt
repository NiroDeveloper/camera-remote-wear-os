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

fun BluetoothDevice.sendKeyboardPress(hidDevice: BluetoothHidDevice, triggerKey: dev.niro.cameraremote.bluetooth.enums.TriggerKey) {
    try {
        val reportId = if (triggerKey == dev.niro.cameraremote.bluetooth.enums.TriggerKey.VOLUME_UP || 
            triggerKey == dev.niro.cameraremote.bluetooth.enums.TriggerKey.VOLUME_DOWN) {
            BluetoothConstants.ID_CONSUMER
        } else {
            BluetoothConstants.ID_KEYBOARD
        }

        val reportData = if (reportId == BluetoothConstants.ID_CONSUMER) {
            if (triggerKey == dev.niro.cameraremote.bluetooth.enums.TriggerKey.VOLUME_UP) {
                byteArrayOf(0xE9.toByte(), 0x00.toByte())
            } else {
                byteArrayOf(0xEA.toByte(), 0x00.toByte())
            }
        } else {
            byteArrayOf(0x0, 0x0, triggerKey.keyCode)
        }

        hidDevice.sendReport(this, reportId.toInt(), reportData)

        // Release
        val releaseData = if (reportId == BluetoothConstants.ID_CONSUMER) {
            byteArrayOf(0x00, 0x00)
        } else {
            byteArrayOf(0x0, 0x0, 0x0)
        }
        hidDevice.sendReport(this, reportId.toInt(), releaseData)

    } catch (ex: SecurityException) {
        Log.wtf(null, "Failed sending device input: $ex")
    }
}