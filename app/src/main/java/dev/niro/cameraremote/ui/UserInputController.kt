package dev.niro.cameraremote.ui

import android.content.Context
import dev.niro.cameraremote.bluetooth.BluetoothController
import dev.niro.cameraremote.interfaces.IUserInterfaceTimerCallback
import dev.niro.cameraremote.utils.Clock
import dev.niro.cameraremote.utils.Vibrator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

object UserInputController {

    var timerDelay = 0
        private set
    var autoTriggerEnabled = false
        private set
    var autoTriggerActive = false
        private set

    private var triggerCoroutine: Job? = null

    var uiCallback: IUserInterfaceTimerCallback? = null

    fun clickTrigger(context: Context) {
        triggerCoroutine?.cancel()

        if (autoTriggerEnabled) {
            autoTriggerActive = !autoTriggerActive

            if (!autoTriggerActive) {
                return
            }
        }

        triggerCoroutine = CoroutineScope(Dispatchers.Default).launch {
            Vibrator.tick(context)

            do {
                runTimerProcess(context)
            } while (autoTriggerEnabled)
        }
    }

    private suspend fun runTimerProcess(context: Context) {
        val configuredTimerDelay = timerDelay

        for (waitCounter in 0..<configuredTimerDelay) {
            if (waitCounter > 0) {
                Vibrator.tick(context)
            }

            val uiFPS = if (uiCallback?.isAmbientModeActive() == false) { 40 } else { 1 }

            for (subTick in 1..uiFPS) {
                Clock.sleep(1000L / uiFPS)

                val subTickProgress = subTick / uiFPS.toFloat()
                val progress = (waitCounter + subTickProgress) / configuredTimerDelay

                uiCallback?.changeProgressIndicatorState(progress)
            }
        }

        if (configuredTimerDelay == 0) {
            uiCallback?.changeProgressIndicatorState(1f)
        }

        Vibrator.shoot(context)
        BluetoothController.takePicture()
    }

    fun toggleTimer() {
        timerDelay = when (timerDelay) {
            0, 1 -> 3
            3 -> 5
            5 -> 10
            else -> (if (autoTriggerEnabled) 1 else 0)
        }
    }

    fun toggleAutoTrigger() {
        autoTriggerEnabled = !autoTriggerEnabled
        autoTriggerActive = false

        triggerCoroutine?.cancel()

        if (autoTriggerEnabled && timerDelay < 1) {
            timerDelay = 1
        }
    }

}