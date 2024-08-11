package athul.svift.android.injection

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import athul.svift.android.data.AuthRepository
import athul.svift.android.data.SongsRepository
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object Injection {

    lateinit var songsRepository:SongsRepository
    lateinit var authRepository: AuthRepository

    fun init(app:Application){
        songsRepository = SongsRepository(app)
        authRepository = AuthRepository(app)
    }

}

fun SongsRepository() = Injection.songsRepository
fun AuthRepository() = Injection.authRepository

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_data")


inline fun <reified T> getAdapter(): JsonAdapter<T> {
    return Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
        .adapter(T::class.java)
}

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}