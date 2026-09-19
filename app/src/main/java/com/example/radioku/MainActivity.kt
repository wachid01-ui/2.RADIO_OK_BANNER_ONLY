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
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors

data class RadioStation(
    val name: String,
    val streamUrl: String
)

class MainActivity : ComponentActivity() {

    private var mediaController: MediaController? = null

    private var isPlaying by mutableStateOf(false)

    private var selectedRadio by mutableStateOf(
        RadioStation(
            "ELSHINTA",
            "https://stream-ssl.arenastreaming.com:8000/jakarta"
        )
    )

    private val radioStations = listOf(
        RadioStation(
            "ELSHINTA",
            "https://stream-ssl.arenastreaming.com:8000/jakarta"
        ),
        RadioStation(
            "SUARA SURABAYA",
            "https://c5.siar.us/proxy/ssfm/stream"
        ),
        RadioStation(
            "SUARA GIRI FM",
            "https://streaming.girifm.com:8010/;stream.mp3"
)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionToken = SessionToken(
            this,
            android.content.ComponentName(
                this,
                PlaybackService::class.java
            )
        )

        val controllerFuture =
            MediaController.Builder(this, sessionToken)
                .buildAsync()

        controllerFuture.addListener(
            {
                mediaController = controllerFuture.get()

                isPlaying =
                    mediaController?.isPlaying == true
            },
            MoreExecutors.directExecutor()
        )

        setContent {
            RadioKuApp(
                radioStations = radioStations,
                selectedRadio = selectedRadio,
                isPlaying = isPlaying,

                onRadioSelected = { radio ->
                    selectedRadio = radio
                    playRadio(radio)
                },

                onPlayPause = {
                    togglePlayback()
                }
            )
        }
    }

    private fun playRadio(radio: RadioStation) {

        val controller = mediaController ?: return

        val mediaItem = MediaItem.Builder()
            .setUri(radio.streamUrl)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(radio.name)
                    .setArtist("RadioKu")
                    .build()
            )
            .build()

        controller.setMediaItem(mediaItem)
        controller.prepare()
        controller.play()

        isPlaying = true
    }

    private fun togglePlayback() {

        val controller = mediaController ?: return

        if (controller.isPlaying) {

            controller.pause()
            isPlaying = false

        } else {

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
    radioStations: List<RadioStation>,
    selectedRadio: RadioStation,
    isPlaying: Boolean,
    onRadioSelected: (RadioStation) -> Unit,
    onPlayPause: () -> Unit
) {

    MaterialTheme {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
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
                modifier = Modifier.height(24.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {

                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "📻",
                        fontSize = 48.sp
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = selectedRadio.name,
                        fontSize = 24.sp,
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
                        }
                    )

                    Spacer(
                        modifier = Modifier.height(16.dp)
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

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Text(
                text = "Pilih Radio",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            radioStations.forEach { radio ->

                Button(
    onClick = {
        onRadioSelected(radio)
    },
    modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp),
    colors = if (radio == selectedRadio) {
        androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    } else {
        androidx.compose.material3.ButtonDefaults.buttonColors()
    }
) {
    Text(
        text = radio.name
    )
}
            }
        }
    }
}
