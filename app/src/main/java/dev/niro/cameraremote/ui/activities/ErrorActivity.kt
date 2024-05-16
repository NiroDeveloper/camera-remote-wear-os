package dev.niro.cameraremote.ui.activities

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import dev.niro.cameraremote.R
import kotlin.math.roundToInt

class ErrorActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val errorId = intent.getIntExtra("messageId", -1)
        val errorMessage = resources.getString(errorId)

        setContent {
            ErrorLayout(errorMessage)
        }
    }

}

@Preview(device = WearDevices.RECT, showSystemUi = true)
@Preview(device = WearDevices.SQUARE, showSystemUi = true)
@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Preview(device = WearDevices.LARGE_ROUND, showSystemUi = true)
@Composable
fun ErrorLayout(message: String = "This is a very long error message that describes what happened.") {
    val activity = LocalContext.current as Activity

    BoxWithConstraints {
        val horizontalPadding = (maxWidth.value * 0.1).roundToInt()
        val verticalPadding = (maxHeight.value * 0.1).roundToInt()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = ScrollState(0))
                .padding(horizontal = horizontalPadding.dp, vertical = verticalPadding.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_error_24),
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )

            Text(
                text = message,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            Button(
                onClick = { activity.finish() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(text = stringResource(id = R.string.ok))
            }
        }
    }
}