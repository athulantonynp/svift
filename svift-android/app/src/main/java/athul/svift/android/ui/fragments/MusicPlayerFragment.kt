package athul.svift.android.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import athul.svift.android.MainActivity
import athul.svift.android.R
import athul.svift.android.SviftApp
import athul.svift.android.data.models.PlaybackStatus
import athul.svift.android.data.models.Song
import athul.svift.android.data.models.getDisplayableData
import athul.svift.android.ui.dialogs.MusicPlayerMenuDialog
import athul.svift.android.ui.dialogs.MusicPlayerMenuDialogCallbacks
import athul.svift.android.viewmodels.MainViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.slider.Slider
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MusicPlayerFragment : Fragment(), SeekBar.OnSeekBarChangeListener, Slider.OnChangeListener,MusicPlayerMenuDialogCallbacks {

    private val viewModel by activityViewModels<MainViewModel>()
    private val handler = Handler(Looper.getMainLooper())
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_music_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        startPlayer()
    }

    private fun setupViews(){
        view?.findViewById<ImageView>(R.id.iv_menu)?.setOnClickListener {
            this.context?.let { it1 -> MusicPlayerMenuDialog(this).show(it1) }
        }

        lifecycleScope.launch {
            viewModel.musicBrowserLoading.collectLatest {
                if (it){
                    view?.findViewById<LinearProgressIndicator>(R.id.lpi)?.visibility = View.VISIBLE
                }else{
                    view?.findViewById<LinearProgressIndicator>(R.id.lpi)?.visibility = View.GONE
                }
            }
        }
    }

    private fun getExoplayer() = (activity as? MainActivity)?.exoPlayer

    private fun isFromBackground(): Boolean {
        return (this@MusicPlayerFragment.activity?.application as SviftApp).isInBackground
    }

    private fun playSong(song: Song){
        getExoplayer()?.let {
            val mediaItem = MediaItem.fromUri(song.file.uri)
            it.setMediaItem(mediaItem)
            it.prepare()
            it.play()
        }
    }

    private fun resumeSong(song: Song){
        getExoplayer()?.let {
            it.seekTo(it.currentPosition)
            it.play()
        }
    }

    private fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    private fun setPlayerPlaying(isPlaying:Boolean){
        val play = view?.findViewById<FloatingActionButton>(R.id.iv_play)
        play?.setImageDrawable(ContextCompat.getDrawable(requireContext(),if(isPlaying)
            R.drawable.pause else R.drawable.play))
    }

    private fun startPlayer(){
        val previous = view?.findViewById<FloatingActionButton>(R.id.iv_previous)
        val albumArt = view?.findViewById<ImageView>(R.id.iv_album)
        val bgArt = view?.findViewById<ImageView>(R.id.iv_bg_art)
        val songName = view?.findViewById<TextView>(R.id.tv_song_name)
        val author = view?.findViewById<TextView>(R.id.tv_author)
        val play = view?.findViewById<FloatingActionButton>(R.id.iv_play)
        val next = view?.findViewById<FloatingActionButton>(R.id.iv_next)
        val seekbar = view?.findViewById<Slider>(R.id.sk_progress)
        val timeStamp = view?.findViewById<TextView>(R.id.tv_timestamp)
        seekbar?.addOnChangeListener(this)

        play?.setOnClickListener {
            viewModel.onPlayClicked()
        }

        next?.setOnClickListener {
            viewModel.goToAnotherSong()
        }

        previous?.setOnClickListener {
            viewModel.goToAnotherSong(false)
        }

        getExoplayer()?.addListener(object : Player.Listener{
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if(playbackState == Player.STATE_ENDED){
                    viewModel.goToAnotherSong(true)
                }
            }
        })

        handler.post(object : Runnable {
            override fun run() {
                getExoplayer()?.let {player->
                    val durationSeconds = if(player.duration<=0){
                        0
                    }else{
                        (player.duration / 1000).toInt()
                    }
                    val currentPositionSeconds = (player.currentPosition / 1000).toInt()

                    // Update SeekBar
                    seekbar?.valueFrom = 0.0f
                    if (durationSeconds<=0){
                        seekbar?.valueTo = 1f
                    }else{
                        seekbar?.valueTo = durationSeconds.toFloat()
                    }

                    seekbar?.value = currentPositionSeconds.toFloat()

                    // Format the elapsed time and total time
                    val elapsedTime = formatTime(currentPositionSeconds)
                    val totalTime = formatTime(durationSeconds)

                    // Update the TextView with the formatted timestamp
                    val formattedTime = "$elapsedTime : $totalTime"
                    timeStamp?.text = formattedTime

                    setPlayerPlaying(player.isPlaying)

                    // Re-run this Runnable every second
                    handler.postDelayed(this, 1000)
                }
            }
        })

        lifecycleScope.launch {
            viewModel.currentSongMetaFlow.collectLatest {meta->
                val thumbnail = meta?.albumArt
                if (albumArt != null) {
                    Glide.with(this@MusicPlayerFragment)
                        .load(thumbnail)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .error(R.drawable.sample_album_art)
                        .placeholder(R.drawable.sample_album_art)
                        .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(24)))
                        .into(albumArt)
                }
                if (bgArt != null) {
                    Glide.with(this@MusicPlayerFragment)
                        .load(thumbnail)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .error(R.drawable.black_background)
                        .placeholder(R.drawable.black_background)
                        .transition(DrawableTransitionOptions.withCrossFade(400))
                        .apply(RequestOptions.bitmapTransform(BlurTransformation(40, 10)))
                        .into(bgArt)
                }
                if(meta != null){
                    songName?.text = meta.title
                    author?.text = meta.getDisplayableData()
                }else{
                    songName?.text = ""
                    author?.text = ""
                }
            }
        }

        lifecycleScope.launch {
            viewModel.currentSongFlow.collectLatest {
                if(it?.status != PlaybackStatus.NONE && it?.song!=null){
                    setPlayerPlaying(it.status === PlaybackStatus.PLAYING)

                    if(isFromBackground() && getExoplayer()?.isPlaying == true){
                        (this@MusicPlayerFragment.activity?.application as SviftApp).isInBackground = false
                        return@collectLatest
                    }

                    when(it.status){
                        PlaybackStatus.PLAYING ->{
                            playSong(it.song)
                        }
                        PlaybackStatus.RESUMED -> {
                            resumeSong(it.song)
                        }
                        PlaybackStatus.PAUSED -> {
                            getExoplayer()?.pause()
                        }
                        else -> {
                            play?.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.play))
                        }
                    }
                }else{
                    if (albumArt != null) {
                        Glide.with(this@MusicPlayerFragment).load(R.drawable.sample_album_art).into(albumArt)
                    }
                    songName?.text = ""
                    author?.text = ""
                    seekbar?.value = 0.0f
                    play?.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.play))
                }

            }
        }
    }


    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            // If the user changes the position, seek to that position
            val seekPosition = progress * 1000L // Convert progress (seconds) to milliseconds
            getExoplayer()?.seekTo(seekPosition)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {

    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {

    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    override fun onValueChange(slider: Slider, value: Float, fromUser: Boolean) {
        if (fromUser) {
            // If the user changes the position, seek to that position
            val seekPosition = value * 1000L // Convert progress (seconds) to milliseconds
            getExoplayer()?.seekTo(seekPosition.toLong())
        }
    }

    override fun onChangeLocation() {
        viewModel.onClickSelectFolder()
    }

}