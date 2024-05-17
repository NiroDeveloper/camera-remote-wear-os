package dev.niro.cameraremote.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import dev.niro.cameraremote.R

class ErrorActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val errorId = intent.getIntExtra("messageId", -1)
        val errorMessage = resources.getString(errorId)

        setContent {
            ErrorLayout(errorMessage) {
                finish()
            }
        }
    }

}
@Preview(device = WearDevices.RECT, showSystemUi = true)
@Preview(device = WearDevices.SQUARE, showSystemUi = true)
@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Preview(device = WearDevices.LARGE_ROUND, showSystemUi = true)
@Composable
fun ErrorLayout(
    message: String = "This is a very long error message that describes what happened.\n\nHere is some more text to read.",
    onOk: (() -> Unit)? = null
) {
    val scalingState = rememberScalingLazyListState(0, 100)

    PositionIndicator(scalingLazyListState = scalingState)

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = scalingState
    ) {
        item {
            Icon(
                painter = painterResource(id = R.drawable.baseline_error_24),
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
        }

        item {
            Text(
                text = message,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            )
        }

        item {
            Button(
                onClick = {
                    if (onOk != null) {
                        onOk()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                Text(text = stringResource(id = R.string.ok))
            }
        }
    }
}