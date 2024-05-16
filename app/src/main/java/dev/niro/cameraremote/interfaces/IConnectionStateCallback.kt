package dev.niro.cameraremote.interfaces

interface IConnectionStateCallback {

    fun onConnectionStateChanged(connected: Boolean)

    fun onConnectionError(message: Int)

}