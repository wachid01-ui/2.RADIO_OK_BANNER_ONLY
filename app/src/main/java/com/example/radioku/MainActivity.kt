package com.example.radioku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors

class MainActivity : ComponentActivity() {

    private var controller by mutableStateOf<MediaController?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionToken =
            SessionToken(this, android.content.ComponentName(this, PlaybackService::class.java))

        val controllerFuture =
            MediaController.Builder(this, sessionToken).buildAsync()

        controllerFuture.addListener(
            {
                controller = controllerFuture.get()

                setContent {
                    RadioScreen(controller)
                }
            },
            MoreExecutors.directExecutor()
        )
    }

    override fun onDestroy() {
        controller?.release()
        controller = null

        super.onDestroy()
    }
}

@androidx.compose.runtime.Composable
fun RadioScreen(controller: MediaController?) {

    var isPlaying by androidx.compose.runtime.remember {
        mutableStateOf(false)
    }

    DisposableEffect(controller) {

        val listener = object : androidx.media3.common.Player.Listener {

            override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                isPlaying = isPlayingNow
            }
        }

        controller?.addListener(listener)

        onDispose {
            controller?.removeListener(listener)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "📻 RadioKu",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(
            text = "Radio Streaming",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(
            modifier = Modifier.height(32.dp)
        )

        Button(
            onClick = {

                controller?.let {

                    if (it.isPlaying) {
                        it.pause()
                    } else {
                        it.play()
                    }
                }

            }
        ) {

            Text(
                text = if (isPlaying) {
                    "⏸ Pause"
                } else {
                    "▶ Play"
                }
            )
        }
    }
}
