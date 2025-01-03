package athul.svift.android.injection

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object Injection {


    fun init(app: Application){
    }

}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_data")

fun Context.observeKeyValueChange(key: Preferences.Key<String>): Flow<String?> {
    return dataStore.data.map {
        it[key]
    }
}


inline fun <reified T> getAdapter(): JsonAdapter<T> {
    return Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
        .adapter(T::class.java)
}

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}