package dev.niro.cameraremote.interfaces

interface IUserInterfaceTimerCallback : IAmbientModeState {

    fun shouldChangeProgressIndicator(progress: Float)

}