package com.example.radioku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors

class MainActivity : ComponentActivity() {

    private var mediaController: MediaController? = null

    private var isPlaying by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionToken = SessionToken(
            this,
            PlaybackService::class.java
        )

        val controllerFuture =
            MediaController.Builder(this, sessionToken)
                .buildAsync()

        controllerFuture.addListener(
            {
                mediaController = controllerFuture.get()

                isPlaying = mediaController?.isPlaying == true
            },
            MoreExecutors.directExecutor()
        )

        setContent {
            RadioKuApp(
                isPlaying = isPlaying,
                onPlayPause = {
                    togglePlayback()
                }
            )
        }
    }

    private fun togglePlayback() {

        val controller = mediaController ?: return

        if (controller.isPlaying) {
            controller.pause()
            isPlaying = false
        } else {
            controller.prepare()
            controller.play()
            isPlaying = true
        }
    }

    override fun onDestroy() {

        mediaController?.release()
        mediaController = null

        super.onDestroy()
    }
}

@Composable
fun RadioKuApp(
    isPlaying: Boolean,
    onPlayPause: () -> Unit
) {

    MaterialTheme {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "RADIOKU",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "Radio Streaming Indonesia",
                fontSize = 16.sp
            )

            Spacer(
                modifier = Modifier.height(32.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {

                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "📻",
                        fontSize = 48.sp
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Text(
                        text = "ELSHINTA",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = if (isPlaying) {
                            "● Sedang Mengudara"
                        } else {
                            "Siap diputar"
                        },
                        fontSize = 16.sp
                    )

                    Spacer(
                        modifier = Modifier.height(24.dp)
                    )

                    Button(
                        onClick = onPlayPause,
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Text(
                            text = if (isPlaying) {
                                "⏸ PAUSE"
                            } else {
                                "▶ PLAY"
                            },
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}
