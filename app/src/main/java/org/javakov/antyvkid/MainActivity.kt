package org.javakov.antyvkid

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import org.javakov.antyvkid.ui.SnusScreen
import org.javakov.antyvkid.ui.SnusStatus
import org.javakov.antyvkid.ui.SnusViewModel
import org.javakov.antyvkid.ui.theme.AntyVkidTheme

class MainActivity : ComponentActivity() {

    private var bgPlayer: MediaPlayer? = null
    private var soundtrackRawId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AntyVkidTheme {
                val vm: SnusViewModel = viewModel(factory = SnusViewModel.Factory(this))
                val state by vm.state.collectAsState()
                LaunchedEffect(state.status) {
                    updateSoundtrack(state.status)
                }
                SnusScreen(state = state, onSubmit = vm::submit)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val vm = ViewModelProvider(this, SnusViewModel.Factory(this))[SnusViewModel::class.java]
        updateSoundtrack(vm.state.value.status)
    }

    override fun onStop() {
        super.onStop()
        releaseSoundtrack()
    }

    private fun rawResForStatus(status: SnusStatus): Int = when (status) {
        SnusStatus.Waiting -> R.raw.bg_music
        SnusStatus.AlreadyUsed, SnusStatus.Blocked -> R.raw.bg_status_next
        SnusStatus.CanSubmit -> R.raw.bg_status_open
    }

    private fun updateSoundtrack(status: SnusStatus) {
        val resId = rawResForStatus(status)
        if (resId == soundtrackRawId && bgPlayer != null) return
        releaseSoundtrackPlayerOnly()
        val player = MediaPlayer.create(this, resId)?.apply {
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
        bgPlayer = player
        soundtrackRawId = if (player != null) resId else null
    }

    private fun releaseSoundtrackPlayerOnly() {
        bgPlayer?.runCatching {
            if (isPlaying) stop()
            reset()
            release()
        }
        bgPlayer = null
    }

    private fun releaseSoundtrack() {
        releaseSoundtrackPlayerOnly()
        soundtrackRawId = null
    }

    private companion object {
        const val BG_VOLUME = 0.25f
    }
}
