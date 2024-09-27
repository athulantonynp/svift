package athul.svift.android.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import athul.svift.android.data.models.FetchCallback
import athul.svift.android.data.models.FetchType
import athul.svift.android.data.models.PlaybackState
import athul.svift.android.data.models.PlaybackStatus
import athul.svift.android.data.models.Song
import athul.svift.android.injection.AuthRepository
import athul.svift.android.injection.Injection
import athul.svift.android.injection.showToast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import sviftytd.DownloadCallback

class MainViewModel(val app:Application) : AndroidViewModel(app),FetchCallback {

    val authFlow = Injection.authRepository.getCurrentUserFlow()
    val fetchStatus = MutableStateFlow<String?>(null)

    val songsListFlow = MutableStateFlow<List<Song>>(emptyList())
    val currentSongFlow = MutableStateFlow<PlaybackState?>(PlaybackState())
    fun performLogin(userName:String,password:String){
        viewModelScope.launch {
            val response = AuthRepository().login(userName,password)
            if(response == null){
                app.showToast("Invalid authentication credentials. Please try again.")
            }
        }
    }

    fun sync(lazy:Boolean=false){
        if(fetchStatus.value==null){
            viewModelScope.launch(Dispatchers.IO) {
                Injection.songsRepository.fetchLatestSongs(this@MainViewModel)
            }
        }else{
            if(!lazy){
                app.applicationContext.showToast("Fetching is already in progress. You may wait.")
            }

        }

    }

    fun startMusicObserver(){
        viewModelScope.launch {
            Injection.database.songDao().getAllFlow().collectLatest {
                songsListFlow.emit(it.shuffled())
            }
        }
    }

    fun onPlayClicked(){
        val currentPlaybackState = currentSongFlow.value
        if(currentPlaybackState?.status == PlaybackStatus.NONE && currentPlaybackState.song==null){
            if(songsListFlow.value.isNullOrEmpty()){
                app.applicationContext.showToast("No songs found. Click to sync")
            }else{
                viewModelScope.launch {
                    currentSongFlow.emit(PlaybackState(PlaybackStatus.PLAYING,songsListFlow.value.first()))
                }
            }
        }

        if(currentPlaybackState?.status == PlaybackStatus.PLAYING || currentPlaybackState?.status == PlaybackStatus.RESUMED){
            viewModelScope.launch { currentSongFlow.emit(currentPlaybackState.copy(status = PlaybackStatus.PAUSED)) }
        }
        if(currentPlaybackState?.status == PlaybackStatus.PAUSED){
            viewModelScope.launch{currentSongFlow.emit(currentPlaybackState.copy(status = PlaybackStatus.RESUMED))}
        }
    }

    fun goToAnotherSong(isNext: Boolean = true) {
        var nextSongToPlay: Song? = null
        val songs = songsListFlow.value

        // Check if the songs list is empty or null
        if (songs.isEmpty()) {
            return // No songs to play, so we exit the function
        }

        val currentIndex = songs.indexOfFirst {
            it.id == currentSongFlow?.value?.song?.id
        }

        if (isNext) {
            if (currentIndex == -1 || currentIndex >= songs.size - 1) {
                // If current song is the last one or not found, wrap around to the first song
                nextSongToPlay = songs.first()
            } else {
                // Move to the next song
                nextSongToPlay = songs[currentIndex + 1]
            }
        } else {
            if (currentIndex == -1 || currentIndex <= 0) {
                // If current song is the first one or not found, wrap around to the last song
                nextSongToPlay = songs.last()
            } else {
                // Move to the previous song
                nextSongToPlay = songs[currentIndex - 1]
            }
        }

        // Update the current song to the new song
        currentSongFlow.value = PlaybackState(status = PlaybackStatus.PLAYING,nextSongToPlay)
    }

    fun cacheAlbumArtWorks(){
        viewModelScope.launch(Dispatchers.IO) {
            val list = Injection.database.songDao().getAll()
            list.forEach {
                Glide.with(app).load(it.thumbnailURL).apply(RequestOptions().diskCacheStrategy(
                    DiskCacheStrategy.ALL).skipMemoryCache(true)).preload()
            }
        }
    }
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                // Get the Application object from extras
                val application = checkNotNull(extras[APPLICATION_KEY])

                return MainViewModel(
                    application,
                ) as T
            }
        }
    }

    override var downloadCallback: DownloadCallback
        get() = DownloadCallback {
            viewModelScope.launch { fetchStatus.emit(it) }
        }
        set(value) {}

    override fun onFetchTypeDecided(fetchType: FetchType) {

    }

    override fun onFetchStarted() {
       viewModelScope.launch {  fetchStatus.emit("Fetch is in progress") }
    }

    override fun onFetchEnd() {
        viewModelScope.launch { fetchStatus.emit(null) }
    }

    override fun onFetchProgress(name: String) {
        viewModelScope.launch {  fetchStatus.emit(name) }
    }
}