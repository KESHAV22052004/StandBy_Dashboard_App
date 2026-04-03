package org.example.project

import android.Manifest
import android.content.ComponentName
import android.content.pm.PackageManager
import android.media.MediaMetadata
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

class MainActivity : ComponentActivity() {

    // Location state
    private var locationData by mutableStateOf(
        LocationData(28.61, 77.20, "Delhi")
    )

    // Music state
    private var musicInfo by mutableStateOf(MusicInfo())

    // Location permission launcher
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            locationData = getDeviceLocation(applicationContext)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ── Fullscreen ──────────────────────────────
        WindowCompat.setDecorFitsSystemWindows(window, false)

        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )

        // ── Location ────────────────────────────────
        val fineLocation = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (fineLocation == PackageManager.PERMISSION_GRANTED) {
            locationData = getDeviceLocation(applicationContext)
        } else {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        // ── Music polling (every 2 seconds) ─────────
        lifecycleScope.launch {
            while (true) {
                musicInfo = getCurrentMusic()
                delay(2000)
            }
        }

        // ── UI ──────────────────────────────────────
        setContent {
            CompositionLocalProvider(
                LocalLocationData provides locationData,
                LocalMusicInfo provides musicInfo
            ) {
                App()
            }
        }
    }

    // ── Read currently playing music ────────────────
    private fun getCurrentMusic(): MusicInfo {
        return try {
            val manager = getSystemService(
                MEDIA_SESSION_SERVICE
            ) as MediaSessionManager

            val component = ComponentName(
                this,
                MusicNotificationListener::class.java
            )

            val sessions = manager.getActiveSessions(component)
            val session = sessions.firstOrNull()

            if (session != null) {
                val metadata = session.metadata
                val title = metadata?.getString(
                    MediaMetadata.METADATA_KEY_TITLE
                ) ?: ""
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
}