package dev.niro.cameraremote.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.VibratorManager
import dev.niro.cameraremote.ui.pages.SettingsPage

object Vibrator {

    fun tick(context: Context) {
        if (!SettingsPage.vibrationEnabled.value) {
            return
        }

        getVibrator(context).vibrate(VibrationEffect.createOneShot(50, 100))
    }

    fun shoot(context: Context) {
        if (!SettingsPage.vibrationEnabled.value) {
            return
        }

        getVibrator(context).vibrate(VibrationEffect.createOneShot(100, 200))
    }

    private fun getVibrator(context: Context): android.os.Vibrator {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            return vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            return context.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
        }
    }

}