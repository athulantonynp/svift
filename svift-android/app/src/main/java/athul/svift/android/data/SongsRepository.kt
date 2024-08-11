package athul.svift.android.data

import android.app.Application
import android.util.Log
import athul.svift.android.data.models.YoutubeMusicCloudResponse
import athul.svift.android.data.models.YoutubeMusicItem
import athul.svift.android.injection.Injection
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

class SongsRepository(private val application: Application) {
    private val db = FirebaseFirestore.getInstance()
    suspend fun fetchLatestSongs(){
        val lastSongTimeStamp = fetchLastSongTimeStampInDb()
        val userName = Injection.authRepository.getCurrentUserFlow().first()?.userName
        if (!userName.isNullOrEmpty()){
            val ref = db.collection("songs").whereEqualTo("userName",userName)
            val docs = ref.get().await()
            val songs = docs.toObjects(YoutubeMusicCloudResponse::class.java).firstOrNull()?.ym


            if(lastSongTimeStamp!=null && lastSongTimeStamp>0){
                val newSongs = songs?.filter {
                    it.time>lastSongTimeStamp
                }
                if(newSongs?.size!! >0){
                    sendForDownload(newSongs)
                }
            }else{
                sendForDownload(songs)
            }
        }
    }

    suspend fun sendForDownload(list: List<YoutubeMusicItem>?){
        Log.e("DOWNLOAD",list.toString())
    }

    private suspend fun fetchLastSongTimeStampInDb():Long?{
        return null
    }
}