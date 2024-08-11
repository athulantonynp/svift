package athul.svift.android.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import athul.svift.android.injection.AuthRepository
import athul.svift.android.injection.Injection
import athul.svift.android.injection.showToast
import kotlinx.coroutines.launch

class MainViewModel(val app:Application) : AndroidViewModel(app) {

    val authFlow = Injection.authRepository.getCurrentUserFlow()
    fun performLogin(userName:String,password:String){
        viewModelScope.launch {
            val response = AuthRepository().login(userName,password)
            if(response == null){
                app.showToast("Invalid authentication credentials. Please try again.")
            }
        }
    }

    fun sync(){
        viewModelScope.launch {
            Injection.songsRepository.fetchLatestSongs()
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
}