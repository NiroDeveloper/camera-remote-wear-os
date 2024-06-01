package dev.niro.cameraremote.bluetooth.enums

import android.bluetooth.BluetoothProfile
import dev.niro.cameraremote.R

enum class ConnectionState(val stringId: Int) {

    CONNECTED(R.string.connection_state_connected),
    DISCONNECTED(R.string.connection_state_disconnected),
    CONNECTING(R.string.connection_state_connecting),
    DISCONNECTING(R.string.connection_state_disconnecting),
    ERROR(R.string.connection_state_error);

    companion object {
        fun fromBluetoothProfile(state: Int) = when(state) {
            BluetoothProfile.STATE_CONNECTED -> CONNECTED
            BluetoothProfile.STATE_CONNECTING -> CONNECTING
            BluetoothProfile.STATE_DISCONNECTED -> DISCONNECTED
            BluetoothProfile.STATE_DISCONNECTING -> DISCONNECTING
            else -> ERROR
        }
    }

}