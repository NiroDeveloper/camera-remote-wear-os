package dev.niro.cameraremote.bluetooth.helper

import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothHidDeviceAppQosSettings
import android.bluetooth.BluetoothHidDeviceAppSdpSettings

object BluetoothConstants {

    const val ID_KEYBOARD: Byte = 1

    // Copy from https://github.com/ginkage/wearmouse/blob/master/app/src/main/java/com/ginkage/wearmouse/bluetooth/Constants.java
    private val HID_REPORT_DESC = byteArrayOf(
        0x05.toByte(),
        0x01.toByte(),
        0x09.toByte(),
        0x06.toByte(),
        0xA1.toByte(),
        0x01.toByte(),
        0x85.toByte(),
        ID_KEYBOARD,
        0x05.toByte(),
        0x07.toByte(),
        0x19.toByte(),
        0xE0.toByte(),
        0x29.toByte(),
        0xE7.toByte(),
        0x15.toByte(),
        0x00.toByte(),
        0x25.toByte(),
        0x01.toByte(),
        0x75.toByte(),
        0x01.toByte(),
        0x95.toByte(),
        0x08.toByte(),
        0x81.toByte(),
        0x02.toByte(),
        0x75.toByte(),
        0x08.toByte(),
        0x95.toByte(),
        0x01.toByte(),
        0x81.toByte(),
        0x01.toByte(),
        0x75.toByte(),
        0x08.toByte(),
        0x95.toByte(),
        0x06.toByte(),
        0x15.toByte(),
        0x00.toByte(),
        0x25.toByte(),
        0x65.toByte(),
        0x05.toByte(),
        0x07.toByte(),
        0x19.toByte(),
        0x00.toByte(),
        0x29.toByte(),
        0x65.toByte(),
        0x81.toByte(),
        0x00.toByte(),
        0xC0.toByte(),
    )

    val SPD_RECORD = BluetoothHidDeviceAppSdpSettings(
        "Camera Remote",
        "Camera Remote for Wear OS",
        "Android",
        BluetoothHidDevice.SUBCLASS1_COMBO,
        HID_REPORT_DESC
    )

    val QOS_OUT = BluetoothHidDeviceAppQosSettings(
        BluetoothHidDeviceAppQosSettings.SERVICE_BEST_EFFORT,
        800,
        9,
        0,
        11250,
        BluetoothHidDeviceAppQosSettings.MAX
    )

}