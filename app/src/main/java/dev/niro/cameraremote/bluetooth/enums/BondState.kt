package dev.niro.cameraremote.bluetooth.enums

import android.bluetooth.BluetoothDevice
import dev.niro.cameraremote.R

enum class BondState(val stringId: Int) {

    BONDED(R.string.bond_state_bonded),
    BONDING(R.string.bond_state_bonding),
    UNBOUND(R.string.bond_state_unbound),
    ERROR(R.string.bond_state_error);

    companion object {
        fun fromBluetoothDevice(state: Int) = when(state) {
            BluetoothDevice.BOND_BONDED -> BONDED
            BluetoothDevice.BOND_BONDING -> BONDING
            BluetoothDevice.BOND_NONE -> UNBOUND
            else -> ERROR
        }
    }

}