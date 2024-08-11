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
import kotlinx.coroutines.launch

class MainViewModel(val app:Application) : AndroidViewModel(app) {

    fun performLogin(userName:String,password:String){
        viewModelScope.launch {
            val response = AuthRepository().login(userName,password)
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