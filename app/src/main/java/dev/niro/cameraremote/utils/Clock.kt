package dev.niro.cameraremote.utils

import kotlinx.coroutines.delay

object Clock {

    suspend fun sleep(milliseconds: Long) {
        val startTime = System.currentTimeMillis()

        while (true) {
            val elapsedTime = System.currentTimeMillis() - startTime
            val remainingTime = milliseconds - elapsedTime

            if (remainingTime <= 0) {
                break
            }

            if (remainingTime < 10) {
                delay(remainingTime)
            } else {
                delay(remainingTime / 2)
            }
        }
    }

}