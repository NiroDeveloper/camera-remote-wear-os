package dev.niro.cameraremote.bluetooth

import dev.niro.cameraremote.bluetooth.enums.BondState
import dev.niro.cameraremote.bluetooth.enums.ConnectionState

data class DeviceWrapper(val address: String, val name: String, val state: ConnectionState, val bond: BondState)