package org.example.project

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState



fun getCurrentMusic(context: Context): MusicInfo {
    return try {
        val manager = context.getSystemService(
            Context.MEDIA_SESSION_SERVICE
        ) as MediaSessionManager

        val component = ComponentName(
            context,
            MusicNotificationListener::class.java
        )

        val sessions = manager.getActiveSessions(component)
        val session = sessions.firstOrNull()

        if (session != null) {
            val metadata = session.metadata
            val title = metadata?.getString(
                MediaMetadata.METADATA_KEY_TITLE
            ) ?: "Unknown"
            val artist = metadata?.getString(
                MediaMetadata.METADATA_KEY_ARTIST
            ) ?: ""
            val isPlaying = session.playbackState?.state ==
                    PlaybackState.STATE_PLAYING

            MusicInfo(title, artist, isPlaying)
        } else {
            MusicInfo()
        }
    } catch (e: Exception) {
        MusicInfo()
    }
}

fun sendMediaAction(context: Context, action: Long) {
    try {
        val manager = context.getSystemService(
            Context.MEDIA_SESSION_SERVICE
        ) as MediaSessionManager

        val component = ComponentName(
            context,
            MusicNotificationListener::class.java
        )

        val session = manager.getActiveSessions(component)
            .firstOrNull() ?: return

        val controls = session.transportControls

        when (action) {
            ACTION_PLAY_PAUSE -> {
                if (session.playbackState?.state ==
                    PlaybackState.STATE_PLAYING
                ) controls.pause()
                else controls.play()
            }
            ACTION_NEXT -> controls.skipToNext()
            ACTION_PREV -> controls.skipToPrevious()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

const val ACTION_PLAY_PAUSE = 1L
const val ACTION_NEXT = 2L
const val ACTION_PREV = 3L