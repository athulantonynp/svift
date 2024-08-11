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
import athul.svift.android.injection.AuthRepository
import athul.svift.android.injection.Injection
import athul.svift.android.injection.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import sviftytd.DownloadCallback

class MainViewModel(val app:Application) : AndroidViewModel(app),FetchCallback {

    val authFlow = Injection.authRepository.getCurrentUserFlow()
    val fetchStatus = MutableStateFlow("")
    val lastFetchType = MutableStateFlow(FetchType.NO_FETCH)
    fun performLogin(userName:String,password:String){
        viewModelScope.launch {
            val response = AuthRepository().login(userName,password)
            if(response == null){
                app.showToast("Invalid authentication credentials. Please try again.")
            }
        }
    }

    fun sync(){
        viewModelScope.launch(Dispatchers.IO) {
            Injection.songsRepository.fetchLatestSongs(this@MainViewModel)
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
        viewModelScope.launch { lastFetchType.emit(fetchType) }
    }
}