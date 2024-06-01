package dev.niro.cameraremote.bluetooth.enums

import android.bluetooth.BluetoothDevice

enum class BondState {

    BONDED,
    BONDING,
    UNBOUND,
    ERROR;

    companion object {
        fun fromBluetoothDevice(state: Int) = when(state) {
            BluetoothDevice.BOND_BONDED -> BONDED
            BluetoothDevice.BOND_BONDING -> BONDING
            BluetoothDevice.BOND_NONE -> UNBOUND
            else -> ERROR
        }
    }

}