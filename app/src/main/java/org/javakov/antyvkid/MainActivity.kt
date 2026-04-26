package org.javakov.antyvkid

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import org.javakov.antyvkid.ui.SnusScreen
import org.javakov.antyvkid.ui.SnusViewModel
import org.javakov.antyvkid.ui.theme.AntyVkidTheme

class MainActivity : ComponentActivity() {

    private var bgPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AntyVkidTheme {
                val vm: SnusViewModel = viewModel(factory = SnusViewModel.Factory(this))
                val state by vm.state.collectAsState()
                SnusScreen(state = state, onSubmit = vm::submit)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        bgPlayer = MediaPlayer.create(this, R.raw.bg_music)?.apply {
            isLooping = true
            setVolume(BG_VOLUME, BG_VOLUME)
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            start()
        }
    }

    override fun onStop() {
        super.onStop()
        bgPlayer?.runCatching {
            if (isPlaying) stop()
            reset()
            release()
        }
        bgPlayer = null
    }

    private companion object {
        const val BG_VOLUME = 0.25f
    }
}
