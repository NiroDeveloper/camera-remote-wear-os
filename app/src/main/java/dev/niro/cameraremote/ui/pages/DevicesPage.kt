package dev.niro.cameraremote.ui.pages

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import dev.niro.cameraremote.R
import dev.niro.cameraremote.bluetooth.DeviceWrapper
import kotlinx.coroutines.flow.MutableStateFlow


object DevicesPage {

    val deviceList = MutableStateFlow(listOf<DeviceWrapper>())

    fun updateDevice(device: DeviceWrapper) {
        val mutableDeviceList = deviceList.value.toMutableList()

        val deviceIndex = mutableDeviceList.indexOfFirst { it.address == device.address }

        if (deviceIndex >= 0) {
            mutableDeviceList[deviceIndex] = device
        } else {
            mutableDeviceList.add(device)
        }

        deviceList.value = mutableDeviceList
    }

}

@Preview(device = WearDevices.RECT, showSystemUi = true)
@Preview(device = WearDevices.SQUARE, showSystemUi = true)
@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Preview(device = WearDevices.LARGE_ROUND, showSystemUi = true)
@Composable
fun DevicesLayout() {
    val context = LocalContext.current

    val deviceList = remember { DevicesPage.deviceList }
    val deviceListState by remember { deviceList }.collectAsState()

    ScalingLazyColumn(modifier = Modifier.fillMaxSize()) {

        item {
            Text(
                text = stringResource(id = R.string.devices),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }

        items(deviceListState.size) {
            val device = deviceListState[it]

            val stateString = stringResource(id = device.state.stringId)
            val bondString = stringResource(id = device.bond.stringId)

            Chip(
                onClick = { },
                label = { Text(text = device.name) },
                secondaryLabel = { Text(text = "$stateString, $bondString") },
                modifier = Modifier.fillMaxWidth(),
                colors = ChipDefaults.secondaryChipColors()
            )

        }


        item {
            val titleTextId = if (deviceListState.isEmpty()) {
                R.string.devices_none_paired
            } else {
                R.string.devices_connect_more
            }

            Chip(
                onClick = {
                    val intentOpenBluetoothSettings = Intent()
                    intentOpenBluetoothSettings.setAction(Settings.ACTION_BLUETOOTH_SETTINGS)
                    context.startActivity(intentOpenBluetoothSettings)
                },
                label = { Text(text = stringResource(id = titleTextId)) },
                secondaryLabel = { Text(text = stringResource(id = R.string.devices_open_bluetooth_settings)) },
                colors = ChipDefaults.primaryChipColors()
            )
        }

    }

}