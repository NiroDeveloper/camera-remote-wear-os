package dev.niro.cameraremote.ui

import android.content.Context
import android.os.PowerManager
import dev.niro.cameraremote.bluetooth.BluetoothController
import dev.niro.cameraremote.bluetooth.enums.TriggerKey
import dev.niro.cameraremote.interfaces.IUserInterfaceTimerCallback
import dev.niro.cameraremote.utils.Vibrator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object UserInputController {

    var timerDelay = 0
        private set
    var triggerKey: TriggerKey = TriggerKey.ENTER
    var vibrationEnabled = true
    var autoTriggerEnabled = false
        private set
    var autoTriggerActive = false
        private set

    private var triggerCoroutine: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null

    var uiCallback: IUserInterfaceTimerCallback? = null

    fun clickTrigger(context: Context) {
        triggerCoroutine?.cancel()

        if (autoTriggerEnabled) {
            autoTriggerActive = !autoTriggerActive

            if (!autoTriggerActive) {
                releaseWakeLock()
                return
            }
        }

        acquireWakeLock(context)

        triggerCoroutine = CoroutineScope(Dispatchers.Default).launch {
            try {
                Vibrator.tick(context)

                do {
                    runTimerProcess(context)
                } while (autoTriggerEnabled)
            } finally {
                releaseWakeLock()
            }
        }
    }

    private fun acquireWakeLock(context: Context) {
        if (wakeLock == null) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CameraRemote:TimerLock")
        }
        if (wakeLock?.isHeld == false) {
            wakeLock?.acquire(10 * 60 * 1000L)
        }
    }

    private fun releaseWakeLock() {
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }

    private suspend fun runTimerProcess(context: Context) {
        val configuredTimerDelay = timerDelay

        for (waitCounter in 0..<configuredTimerDelay) {
            if (waitCounter > 0) {
                Vibrator.tick(context)
            }

            val uiFPS = if (uiCallback?.isAmbientModeActive() == false) { 30 } else { 1 }

            for (subTick in 1..uiFPS) {
                delay(1000L / uiFPS)

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