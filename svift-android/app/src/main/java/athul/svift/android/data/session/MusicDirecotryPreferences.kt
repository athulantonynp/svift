package athul.svift.android.data.session

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import athul.svift.android.injection.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val FOLDER_URI_KEY = stringPreferencesKey("mf_uri")

suspend fun Context.saveFolderUri(uri: Uri) {
    dataStore.edit {
        it[FOLDER_URI_KEY] = uri.toString()
    }
}

suspend fun Context.getFolderUri():Uri?{
    val uriString = dataStore.data.map { prefs->
        prefs[FOLDER_URI_KEY]
    }.first()
    uriString?.let {
        return Uri.parse(it)
    }
    return null
}


suspend fun Context.clearFolderUri() {
    dataStore.edit {
        it.remove(FOLDER_URI_KEY)
    }
}