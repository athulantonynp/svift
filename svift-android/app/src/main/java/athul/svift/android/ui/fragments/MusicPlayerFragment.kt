package athul.svift.android.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import athul.svift.android.R
import athul.svift.android.data.models.PlaybackStatus
import athul.svift.android.viewmodels.MainViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MusicPlayerFragment : Fragment() {

    private val viewModel by activityViewModels<MainViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_music_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        viewModel.sync(true)
        startPlayer()
    }

    private fun setupViews(){
        view?.findViewById<ImageButton>(R.id.iv_sync)?.setOnClickListener {
            viewModel.sync()
        }
        val statusView = view?.findViewById<TextView>(R.id.tv_fetch_status)
        lifecycleScope.launch {
            viewModel.fetchStatus.collectLatest {
                statusView?.isVisible = !it.isNullOrEmpty()
                statusView?.text = it
            }
        }
    }

    private fun startPlayer(){
        viewModel.startMusicObserver()
        val previous = view?.findViewById<ImageView>(R.id.iv_previous)
        val albumArt = view?.findViewById<ImageView>(R.id.iv_album)
        val songName = view?.findViewById<TextView>(R.id.tv_song_name)
        val author = view?.findViewById<TextView>(R.id.tv_author)
        val play = view?.findViewById<ImageView>(R.id.iv_play)
        val next = view?.findViewById<ImageView>(R.id.iv_next)
        val seekbar = view?.findViewById<AppCompatSeekBar>(R.id.sk_progress)

        play?.setOnClickListener {
            viewModel.onPlayClicked()
        }

        next?.setOnClickListener {
            viewModel.goToAnotherSong()
        }

        previous?.setOnClickListener {
            viewModel.goToAnotherSong(false)
        }

        lifecycleScope.launch {
            viewModel.currentSongFlow.collectLatest {
                if(it?.status != PlaybackStatus.NONE && it?.song!=null){
                    if (albumArt != null) {
                        Glide.with(this@MusicPlayerFragment).load(it.song.thumbnailURL).diskCacheStrategy(
                            DiskCacheStrategy.ALL).error(R.drawable.sample_album_art).placeholder(R.drawable.sample_album_art).into(albumArt)
                    }
                    songName?.text = it.song.title
                    author?.text = it.song.author
                    when(it.status){
                        PlaybackStatus.PLAYING ->{
                            play?.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_pause))
                        }
                        else -> {
                            play?.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_play))
                        }
                    }
                }else{
                    if (albumArt != null) {
                        Glide.with(this@MusicPlayerFragment).load(R.drawable.sample_album_art).into(albumArt)
                    }
                    songName?.text = ""
                    author?.text = ""
                    seekbar?.progress = 0
                    play?.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_play))
                }

            }
        }
    }

}