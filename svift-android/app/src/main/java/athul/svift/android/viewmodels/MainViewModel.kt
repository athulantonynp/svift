package athul.svift.android.viewmodels

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import athul.svift.android.data.models.PageState
import athul.svift.android.data.models.PlaybackState
import athul.svift.android.data.models.PlaybackStatus
import athul.svift.android.data.models.Song
import athul.svift.android.data.session.FOLDER_URI_KEY
import athul.svift.android.data.session.clearFolderUri
import athul.svift.android.data.session.saveFolderUri
import athul.svift.android.data.utils.getMp3Metadata
import athul.svift.android.data.utils.supportedFormats
import athul.svift.android.injection.observeKeyValueChange
import athul.svift.android.injection.showToast
import com.anggrayudi.storage.file.DocumentFileCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class MainViewModel(val app:Application) : AndroidViewModel(app) {

    val pageFlow = MutableStateFlow<PageState?>(PageState.PERMISSIONS)
    val currentSongFlow = MutableStateFlow<PlaybackState?>(PlaybackState())
    val songsListFlow = MutableStateFlow<List<Song>>(emptyList())
    val musicBrowserLoading = MutableStateFlow(false)


    @OptIn(ExperimentalCoroutinesApi::class)
    val currentSongMetaFlow = currentSongFlow.flatMapLatest {
        flow {
            emit(it?.song?.file?.let { it1 -> getMp3Metadata(app, it1) })
        }.flowOn(Dispatchers.IO)
    }

    private fun sync(){
        viewModelScope.launch {
            app.observeKeyValueChange(FOLDER_URI_KEY).collectLatest {
                if (it.isNullOrEmpty()){
                    pageFlow.emit(PageState.EMPTY_NO_FOLDER)
                }else{
                    pageFlow.emit(PageState.MUSIC_PLAYER)
                    browseMusic(Uri.parse(it))
                }
            }

        }
    }

    fun onClickSelectFolder(){
        viewModelScope.launch {
            app.clearFolderUri()
        }
    }
    private suspend fun browseMusic(uri: Uri){
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val savedFolder = DocumentFileCompat.fromUri(app,uri)
                if (savedFolder != null && savedFolder.exists()) {
                    readMediaFiles(savedFolder)
                } else {
                    toast("The folder is no longer accessible.")
                    pageFlow.emit(PageState.EMPTY_NO_FOLDER)
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }

    private fun toast(message:String){
        viewModelScope.launch(Dispatchers.Main) {
            app.showToast(message)
        }
    }

    fun onPermissionsGranted(){
        sync()
    }

    private suspend fun readMediaFiles(folder: DocumentFile) {
        musicBrowserLoading.emit(true)
        val mediaFiles = folder.listFiles().filter { file ->
            file.isFile &&
                    !file.name.isNullOrEmpty() &&
                    !file.name!!.startsWith("._") && // Exclude macOS metadata files
                    supportedFormats.any { format ->
                        file.name!!.endsWith(format, ignoreCase = true)
                    }
        }

        if (mediaFiles.isEmpty()) {
            toast("No media files found. Select a different folder.")
            pageFlow.emit(PageState.EMPTY_NO_FOLDER)
            musicBrowserLoading.emit(false)
        } else {
            val songs = arrayListOf<Song>()
            mediaFiles.forEachIndexed { index, mediaFile ->
                Log.d("MediaFile", "Found: ${mediaFile.name}")
                songs.add(Song(id = index.toString(), file = mediaFile,metadata = null))
            }
            songsListFlow.emit(songs.shuffled())
            musicBrowserLoading.emit(false)
        }
    }

    fun onFolderSelected(uri: Uri){
        viewModelScope.launch {
            app.saveFolderUri(uri)
        }
    }

    fun onPlayClicked(){
        val currentPlaybackState = currentSongFlow.value
        if(currentPlaybackState?.status == PlaybackStatus.NONE && currentPlaybackState.song==null){
            if(songsListFlow.value.isEmpty()){
                toast("No songs found")
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
}