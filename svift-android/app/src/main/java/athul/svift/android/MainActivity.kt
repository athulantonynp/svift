package athul.svift.android

import android.content.Context
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import athul.svift.android.ui.fragments.LoginFragment
import athul.svift.android.ui.fragments.MusicPlayerFragment
import athul.svift.android.viewmodels.MainViewModel
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<MainViewModel> { MainViewModel.Factory  }
    val exoPlayer: ExoPlayer by lazy { ExoPlayer.Builder(this).build() }

    private lateinit var audioManager: AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null
    private var hasAudioFocus = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupAudioFocus()
    }

    private fun setupAudioFocus() {
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val focusRequest: AudioFocusRequest =
            AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setOnAudioFocusChangeListener { focusChange -> onAudioFocusChange(focusChange) }
                .build()

        audioFocusRequest = focusRequest

        val result =  audioManager.requestAudioFocus(focusRequest)

        hasAudioFocus = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                // Permanent loss of audio focus, stop playback and release resources
                if (exoPlayer.isPlaying) {
                    exoPlayer.pause()
                }
                hasAudioFocus = false
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // Temporary loss of audio focus, pause playback
                if (exoPlayer.isPlaying) {
                    exoPlayer.pause()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // Lower the volume, but keep playing
                exoPlayer.volume = 0.2f
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                // Resume playback or restore the volume
                if (hasAudioFocus && !exoPlayer.isPlaying) {
                    exoPlayer.play()
                }
                exoPlayer.volume = 1.0f
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            viewModel.authFlow.collectLatest {
                if(it !=null){
                    loadFragment(MusicPlayerFragment())
                }else{
                    loadFragment(LoginFragment())
                }
            }
        }
    }


    private fun loadFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        audioFocusRequest?.let {
            audioManager.abandonAudioFocusRequest(it)
        }
        exoPlayer.release()
    }
}