package athul.svift.android

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import athul.svift.android.data.models.PageState
import athul.svift.android.ui.fragments.MusicPlayerFragment
import athul.svift.android.ui.fragments.SelectFolderFragment
import athul.svift.android.viewmodels.MainViewModel
import com.anggrayudi.storage.SimpleStorageHelper
import com.google.android.exoplayer2.ExoPlayer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<MainViewModel> { MainViewModel.Factory  }
    val exoPlayer: ExoPlayer by lazy { ExoPlayer.Builder(this).build() }
    val storageHelper = SimpleStorageHelper(this)


    private lateinit var audioManager: AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null
    private var hasAudioFocus = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try{
            window.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }catch (e:Exception){
            e.printStackTrace()
        }
        setContentView(R.layout.activity_main)
        setupAudioFocus()
        storageHelper.onFolderSelected = {requestCode, folder ->
            lifecycleScope.launch {
                this@MainActivity.viewModel.onFolderSelected(folder.uri)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            viewModel.pageFlow.collectLatest {
                if(it === PageState.EMPTY_NO_FOLDER){
                    loadFragment(SelectFolderFragment())
                }
                if(it === PageState.MUSIC_PLAYER){
                    loadFragment(MusicPlayerFragment())
                }
                if(it === PageState.PERMISSIONS){
                    requestForPermissions()
                }
            }
        }
    }

    private fun  requestForPermissions(){
        var permissions = listOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            permissions = listOf(
                android.Manifest.permission.READ_MEDIA_VIDEO,
                android.Manifest.permission.READ_MEDIA_IMAGES,
                android.Manifest.permission.READ_MEDIA_AUDIO
            )
        }
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            viewModel.onPermissionsGranted()
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val deniedPermissions = permissions.filterValues { !it }
        if (deniedPermissions.isEmpty()) {
            viewModel.onPermissionsGranted()
        } else {
            Toast.makeText(this, "Permissions denied: $deniedPermissions", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        storageHelper.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        storageHelper.onRestoreInstanceState(savedInstanceState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        storageHelper.storage.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        storageHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
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
