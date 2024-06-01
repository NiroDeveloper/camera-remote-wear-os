package dev.niro.cameraremote.interfaces

interface IServiceStateCallback {

    fun onServiceStateChange(available: Boolean)

    fun onServiceError(message: Int)

}