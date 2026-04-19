package dev.niro.cameraremote.bluetooth.enums

import dev.niro.cameraremote.R

enum class TriggerKey(val keyCode: Byte, val stringRes: Int) {
    ENTER(40, R.string.trigger_key_enter),
    VOLUME_UP(128.toByte(), R.string.trigger_key_volume_up),
    VOLUME_DOWN(129.toByte(), R.string.trigger_key_volume_down),
    SPACE(44, R.string.trigger_key_space);

    companion object {
        fun fromKeyCode(keyCode: Byte): TriggerKey {
            return entries.find { it.keyCode == keyCode } ?: ENTER
        }
    }
}