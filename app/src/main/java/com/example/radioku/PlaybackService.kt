package com.example.radioku

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer

    override fun onCreate() {
        super.onCreate()

        player = ExoPlayer.Builder(this)
            .setHandleAudioBecomingNoisy(true)
            .build()

        val mediaItem = MediaItem.Builder()
            .setUri(RADIO_URL)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle("RadioKu")
                    .setArtist("Radio Streaming")
                    .build()
            )
            .build()

        player.setMediaItem(mediaItem)

        mediaSession = MediaSession.Builder(this, player)
            .build()
    }

    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo
    ): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.release()
        player.release()
        mediaSession = null

        super.onDestroy()
    }

    companion object {
        const val RADIO_URL =
            "https://stream-ssl.arenastreaming.com:8000/jakarta"
    }
}
