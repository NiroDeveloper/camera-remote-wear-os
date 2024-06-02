package dev.niro.cameraremote.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHidDevice
import android.bluetooth.BluetoothProfile
import android.util.Log
import dev.niro.cameraremote.R
import dev.niro.cameraremote.bluetooth.enums.ConnectionState
import dev.niro.cameraremote.bluetooth.helper.BluetoothConstants
import dev.niro.cameraremote.bluetooth.helper.getAddressString
import dev.niro.cameraremote.bluetooth.helper.getConnectionStateEnum
import dev.niro.cameraremote.bluetooth.helper.toDebugString
import dev.niro.cameraremote.interfaces.IConnectionStateCallback
import dev.niro.cameraremote.interfaces.IServiceStateCallback

class BluetoothServiceCallback(
    private val connectionStateListener: IConnectionStateCallback,
    private val serviceStateListener: IServiceStateCallback
) : BluetoothProfile.ServiceListener {

    var hidDevice: BluetoothHidDevice? = null
        private set

    private var hidCallback: HidDeviceCallback? = null

    var appRegistered = false
        private set

    private val radarDeviceRegister = mutableMapOf<String, ConnectionState>()

    override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
        Log.d(null, "onServiceConnected($profile, $proxy)")

        if (profile != BluetoothProfile.HID_DEVICE) {
            Log.wtf(null, "BluetoothProfile is not a HidDevice")
            return
        }

        if (proxy == null) {
            Log.wtf(null, "No proxy received as HidDevice")
            return
        }

        if (proxy !is BluetoothHidDevice) {
            Log.wtf(null, "BluetoothProfile is not a BluetoothHidDevice instance")
            return
        }

        hidDevice = proxy
        hidCallback = registerApp(proxy)
    }

    override fun onServiceDisconnected(profile: Int) {
        Log.d(null, "onServiceDisconnected($profile)")

        hidDevice = null
        hidCallback = null
        appRegistered = false

        serviceStateListener.onServiceStateChange(false)
    }

    private fun registerApp(registerHidDevice: BluetoothHidDevice): HidDeviceCallback {
        val serviceStateListener = object : IServiceStateCallback, IConnectionStateCallback {
            override fun onServiceStateChange(available: Boolean) {
                appRegistered = available

                if (available) {
                    startAutoConnect()

                    startRadarThread()
                }

                serviceStateListener.onServiceStateChange(available)
            }

            override fun onServiceError(message: Int) {
                serviceStateListener.onServiceError(message)
            }

            override fun onConnectionStateChange(device: BluetoothDevice, state: ConnectionState) {
                connectionStateListener.onConnectionStateChange(device, state)

                radarDeviceRegister[device.getAddressString()] = state
            }
        }

        val newHidCallback = HidDeviceCallback(registerHidDevice, serviceStateListener, serviceStateListener)

        try {
            // Somehow always returns false (unsuccessful).
            registerHidDevice.registerApp(
                BluetoothConstants.SPD_RECORD,
                null,
                BluetoothConstants.QOS_OUT,
                Runnable::run,
                newHidCallback
            )

            Log.i(null, "Called BluetoothHidDevice.registerApp")
        } catch (ex: SecurityException) {
            Log.wtf(null, "Failed app registration: $ex")
        }

        return newHidCallback
    }

    private fun startRadarThread() {
        val thread = Thread {
            Log.i(null, "Starting radar thread")

            while (true) {
                val localHidDevice = hidDevice ?: return@Thread
                val newDeviceList = getDevices()

                for (device in newDeviceList) {
                    val deviceAddress = device.getAddressString()
                    val oldConnectionState = radarDeviceRegister[deviceAddress]
                    val newConnectionState = device.getConnectionStateEnum(localHidDevice)

                    if (oldConnectionState == newConnectionState) {
                        continue
                    }

                    Log.i(null, "Radar detected device change: $device ($oldConnectionState -> $newConnectionState)")

                    connectionStateListener.onConnectionStateChange(device, newConnectionState)
                    radarDeviceRegister[deviceAddress] = newConnectionState
                }

                val sleepDelay = if (isDeviceConnected()) 10_000L else 1_000L
                Thread.sleep(sleepDelay)
            }
        }
        thread.name = "Radar"
        thread.priority = Thread.MIN_PRIORITY
        thread.start()
    }

    fun startAutoConnect() {
        val hostHidDevice = hidDevice
        if (hostHidDevice == null) {
            Log.e(null, "Bluetooth service is not connected")
            serviceStateListener.onServiceError(R.string.error_service_register)

            return
        }

        if (!appRegistered) {
            Log.e(null, "Bluetooth app is not registered")
            serviceStateListener.onServiceError(R.string.error_app_register)

            return
        }

        if (isDeviceConnected()) {
            Log.w(null, "Device already connected, no auto connect required")
            return
        }

        val devices = getDevices(BluetoothProfile.STATE_DISCONNECTED)

        if (devices.isEmpty()) {
            // If all devices are in the disconnecting state, show an error.
            if (getDevices(BluetoothProfile.STATE_DISCONNECTING).size == getDevices().size) {
                Log.e(null, "All devices are disconnecting, seems like the app unregistered silently")
                serviceStateListener.onServiceError(R.string.error_app_register)

                return
            }

            Log.e(null, "No devices found")
            serviceStateListener.onServiceError(R.string.error_no_devices_found)

            return
        }

        try {
            devices.forEach { device ->
                Log.i(null, "Connect with device: ${device.toDebugString(hostHidDevice)}")

                hostHidDevice.connect(device)
            }
        } catch (ex: SecurityException) {
            Log.wtf(null, "Failed auto connect: $ex")
        }
    }

    fun isDeviceConnected() = getDevices(BluetoothProfile.STATE_CONNECTED).isNotEmpty()

    fun getDevices(
        vararg states: Int = intArrayOf(
            BluetoothProfile.STATE_DISCONNECTED,
            BluetoothProfile.STATE_DISCONNECTING,
            BluetoothProfile.STATE_CONNECTED,
            BluetoothProfile.STATE_DISCONNECTING
        )
    ): List<BluetoothDevice> {
        try {
            hidDevice?.let {
                return it.getDevicesMatchingConnectionStates(states)
            }
        } catch (ex: SecurityException) {
            Log.wtf(null, "Failed receiving devices: $ex")
        }

        return listOf()
    }

}