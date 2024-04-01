package den.niro.cameraremote.controller

import android.content.Context
import den.niro.cameraremote.utils.Vibrator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object UserInputController {

    private var timerDelay = 0
    private var autoTriggerEnabled = false
    private var autoTriggerActive = false

    private var triggerCoroutine: Job? = null

    fun clickTrigger(context: Context) {
        triggerCoroutine?.cancel()

        if (autoTriggerEnabled) {
            autoTriggerActive = !autoTriggerActive

            if (!autoTriggerActive) {
                return
            }
        }

        triggerCoroutine = CoroutineScope(Dispatchers.Default).launch {
            var firstPicture = true
            do {
                for (waitCounter in 1..timerDelay) {
                    if (firstPicture || waitCounter > 1) {
                        Vibrator.tick(context)
                    }
                    delay(1000L)
                }

                Vibrator.shoot(context)
                BluetoothController.takePicture()
                firstPicture = false
            } while (autoTriggerEnabled)
        }
    }

    fun toggleTimer() {
        timerDelay = when (timerDelay) {
            0, 1 -> 3
            3 -> 5
            5 -> 10
            else -> (if (autoTriggerEnabled) 1 else 0)
        }
    }

    fun getTimerDelay(): Int {
        return timerDelay
    }

    fun toggleAutoTrigger() {
        autoTriggerEnabled = !autoTriggerEnabled
        autoTriggerActive = false

        triggerCoroutine?.cancel()

        if (autoTriggerEnabled && timerDelay < 1) {
            timerDelay = 1
        }
    }

    fun isAutoTriggerEnabled(): Boolean {
        return autoTriggerEnabled
    }

    fun isAutoTriggerActive(): Boolean {
        return autoTriggerActive
    }

}