package dev.niro.cameraremote.interfaces

interface IUserInterfaceTimerCallback {

    fun changeProgressIndicatorState(progress: Float)

    fun isAmbientModeActive(): Boolean

}