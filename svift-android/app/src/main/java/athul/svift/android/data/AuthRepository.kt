package athul.svift.android.data

import athul.svift.android.data.models.AuthResponse
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val db = FirebaseFirestore.getInstance()
    suspend fun login(userName:String, password:String):AuthResponse?{
        val query = db.collection("users").whereEqualTo("userName",userName.trim()).get().await()
        val pass = query.documents.firstOrNull()?.get("password")?.toString()
        if(pass.equals(password)){
            return AuthResponse(userName,password)
        }
        return null
    }
}