package dev.niro.cameraremote.interfaces

import android.bluetooth.BluetoothDevice
import dev.niro.cameraremote.bluetooth.enums.ConnectionState

interface IConnectionStateCallback {

    fun onConnectionStateChange(device: BluetoothDevice, state: ConnectionState)

}