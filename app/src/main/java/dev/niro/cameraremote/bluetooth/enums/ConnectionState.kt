package dev.niro.cameraremote.bluetooth.enums

import android.bluetooth.BluetoothProfile

enum class ConnectionState {

    CONNECTED,
    DISCONNECTED,
    CONNECTING,
    DISCONNECTING,
    ERROR;

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