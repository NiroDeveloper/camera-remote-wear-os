package den.niro.cameraremote.bluetooth

import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import androidx.activity.result.ActivityResultLauncher

object BluetoothController {

    var uiConnectionUpdateListener: (() -> Unit)? = null

    private var bluetoothCallback: BluetoothServiceCallback? = null

    fun init(context: Context) {
        if (!BluetoothPermission.hasBluetoothPermission(context)) {
            return
        }

        registerBluetoothService(context)
    }

    fun destroy(context: Context) {
        bluetoothCallback?.hidDevice?.let {
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val bluetoothAdapter = bluetoothManager.adapter

            bluetoothAdapter.closeProfileProxy(BluetoothProfile.HID_DEVICE, it)
        }
    }

    fun handleBluetooth(context: Context, permissionLauncher: ActivityResultLauncher<String>) {
        if (BluetoothPermission.hasBluetoothPermission(context)) {
            bluetoothCallback?.autoConnect() ?: run {
                registerBluetoothService(context)
            }

            return
        }

        // TODO: Show bluetooth explanation ui
        // https://developer.android.com/training/permissions/requesting?hl=de#explain

        BluetoothPermission.requestBluetoothPermission(permissionLauncher)
    }

    fun registerBluetoothService(context: Context) {
        bluetoothCallback?.let {
            Log.w(null, "BluetoothService is already registered")
            return
        }

        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        val bluetoothCallback = BluetoothServiceCallback {
            uiConnectionUpdateListener?.let { it() }
        }

        val buildSuccessful = bluetoothAdapter.getProfileProxy(context, bluetoothCallback, BluetoothProfile.HID_DEVICE)

        if (buildSuccessful) {
            this.bluetoothCallback = bluetoothCallback
        } else {
            Log.e(null, "BluetoothAdapter.getProfileProxy failed")
        }
    }

    fun isDeviceConnected() = bluetoothCallback?.hidCallback?.connectedDevices?.isNotEmpty() ?: false

    fun takePicture() {
        Log.i(null, "Taking picture now")

        try {
            bluetoothCallback?.let { bluetoothService ->
                bluetoothService.hidDevice?.let {hidDevice ->
                    bluetoothService.hidCallback?.connectedDevices?.forEach { bluetoothDevice ->
                        hidDevice.sendReport(
                            bluetoothDevice,
                            8,
                            byteArrayOf(0x0, 0x0, 40)
                        )

                        hidDevice.sendReport(
                            bluetoothDevice,
                            8,
                            byteArrayOf(0x0, 0x0, 0x0)
                        )

                        Log.i(null, "Sent report signal to device: ${bluetoothDevice.name}")
                    }
                }
            }
        } catch (ex: SecurityException) {
            Log.wtf(null, "Failed sending device input: $ex")
        }

    }

}