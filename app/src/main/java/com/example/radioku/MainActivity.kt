package com.example.radioku
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.OutlinedTextField
import android.os.Bundle
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

data class RadioStation(
    val name: String,
    val streamUrl: String
)

class MainActivity : ComponentActivity() {

    private var mediaController: MediaController? = null

    private var isPlaying by mutableStateOf(false)

    private var selectedRadio by mutableStateOf(
        RadioStation(
            "Memuat radio...",
            ""
        )
    )

    private var radioStations by mutableStateOf(
        listOf<RadioStation>()
    )

    private var isLoading by mutableStateOf(true)

    private var errorMessage by mutableStateOf("")
    private var searchText by mutableStateOf("")
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
                isLoading = isLoading,
                errorMessage = errorMessage,
                searchText = searchText,
                onSearchTextChange = {
                      searchText = it
        },

                onRadioSelected = { radio ->
                    selectedRadio = radio
                    playRadio(radio)
                },

                onPlayPause = {
                    togglePlayback()
                }
            )
        }

        loadIndonesianRadios()
    }

    private fun loadIndonesianRadios() {

        Thread {

            try {

                val url = URL(
                    "https://de1.api.radio-browser.info/json/stations/bycountrycodeexact/ID?hidebroken=true&limit=500"
                )

                val connection =
                    url.openConnection() as HttpURLConnection

                connection.requestMethod = "GET"

                connection.connectTimeout = 10000
                connection.readTimeout = 15000

                connection.setRequestProperty(
                    "User-Agent",
                    "RadioKu/1.0 Android"
                )

                val responseCode =
                    connection.responseCode

                if (responseCode != HttpURLConnection.HTTP_OK) {

                    throw Exception(
                        "Server mengembalikan kode $responseCode"
                    )
                }

                val response =
                    connection.inputStream
                        .bufferedReader()
                        .use { it.readText() }

                connection.disconnect()

                val jsonArray =
                    JSONArray(response)

                val stations =
                    mutableListOf<RadioStation>()

                for (i in 0 until jsonArray.length()) {

                    val station =
                        jsonArray.getJSONObject(i)

                    val name =
                        station.optString("name")

                    val streamUrl =
                        station.optString("url_resolved")
                           .ifEmpty {
                               station.optString("url")
        }

                  val lastCheckOk =
                      station.optInt("lastcheckok", 0)

                  if (
                     name.isNotBlank() &&
                     streamUrl.isNotBlank() &&
                     lastCheckOk == 1
                 ) {

                   stations.add(
                     RadioStation(
                      name = name,
                      streamUrl = streamUrl
                     )
                 )
             }
                }

                runOnUiThread {

                    if (stations.isNotEmpty()) {

                        radioStations = stations

                        selectedRadio =
                            stations.first()

                        isLoading = false
                        errorMessage = ""

                    } else {

                        isLoading = false

                        errorMessage =
                            "Tidak ada stasiun radio Indonesia yang ditemukan."
                    }
                }

            } catch (e: Exception) {

                runOnUiThread {

                    isLoading = false

                    errorMessage =
                        "Gagal mengambil daftar radio: ${e.message}"
                }
            }

        }.start()
    }

    private fun playRadio(radio: RadioStation) {

        val controller =
            mediaController ?: return

        if (radio.streamUrl.isBlank()) {
            return
        }

        val mediaItem =
            MediaItem.Builder()
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

        val controller =
            mediaController ?: return

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
    isLoading: Boolean,
    errorMessage: String,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onRadioSelected: (RadioStation) -> Unit,
    onPlayPause: () -> Unit
)
{

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
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor =
                                Color(0xFF2E7D32),
                            contentColor =
                                Color.White
                        )
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
                text = "Pilih Radio Indonesia",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )
         OutlinedTextField(
             value = searchText,
            onValueChange = onSearchTextChange,
           modifier = Modifier.fillMaxWidth(),
           label = {
                Text("Cari radio")
    },
    placeholder = {
        Text("Ketik nama radio...")
    },
    singleLine = true
)

Spacer(
    modifier = Modifier.height(12.dp)
)
            if (isLoading) {

                CircularProgressIndicator()

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Text(
                    text = "Memuat daftar radio Indonesia..."
                )

            } else if (errorMessage.isNotEmpty()) {

                Text(
                    text = errorMessage
                )

          } else {

    val filteredRadioStations =
        radioStations.filter { radio ->
            radio.name.contains(
                searchText,
                ignoreCase = true
            )
        }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
    ) {

        items(filteredRadioStations) { radio ->

            Button(
                onClick = {
                    onRadioSelected(radio)
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),

                colors =
                    if (radio == selectedRadio) {

                        ButtonDefaults.buttonColors(
                            containerColor =
                                Color(0xFF2E7D32),
                            contentColor =
                                Color.White
                        )

                    } else {

                        ButtonDefaults.buttonColors(
                            containerColor =
                                Color(0xFF757575),
                            contentColor =
                                Color.White
                        )
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
    }
}

@Composable
fun BannerAdView() {

    val context = LocalContext.current

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(),
        factory = {
            AdView(context).apply {

                setAdSize(
                    AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                        context,
                        360
                    )
                )

                adUnitId =
                    "ca-app-pub-3940256099942544/9214589741"

                loadAd(
                    AdRequest.Builder().build()
                )
            }
        }
    )
}
