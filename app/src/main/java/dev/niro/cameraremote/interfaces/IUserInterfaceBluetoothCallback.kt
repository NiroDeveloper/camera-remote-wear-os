package dev.niro.cameraremote.interfaces

import dev.niro.cameraremote.bluetooth.DeviceWrapper

interface IUserInterfaceBluetoothCallback : IServiceStateCallback {

    fun onConnectionStateChange(device: DeviceWrapper)

}