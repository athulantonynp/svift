package athul.svift.android.data

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import athul.svift.android.data.models.AuthResponse
import athul.svift.android.injection.dataStore
import athul.svift.android.injection.getAdapter
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class AuthRepository(private val application: Application) {

    private val db = FirebaseFirestore.getInstance()
    private val AUTH_RESPONSE_KEY = stringPreferencesKey("auth")
    suspend fun login(userName:String, password:String):AuthResponse?{
        val query = db.collection("users").whereEqualTo("userName",userName.trim()).get().await()
        val pass = query.documents.firstOrNull()?.get("password")?.toString()
        if(pass.equals(password)){
            val response = AuthResponse(userName,password)
            val adapter = getAdapter<AuthResponse>()
            application.applicationContext.dataStore.edit {
                it[AUTH_RESPONSE_KEY] = adapter.toJson(response)
            }
            return response
        }
        return null
    }

     fun getCurrentUserFlow():Flow<AuthResponse?>{
        val data = application.applicationContext.dataStore.data.map {
            val json = it[AUTH_RESPONSE_KEY]
            if(!json.isNullOrEmpty()){
                getAdapter<AuthResponse>().fromJson(json)
            }else{
                null
            }
        }
        return data
    }
}