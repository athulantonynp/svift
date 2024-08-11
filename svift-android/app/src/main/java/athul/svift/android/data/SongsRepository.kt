package athul.svift.android.data

import android.app.Application
import android.util.Log
import athul.svift.android.data.models.FetchCallback
import athul.svift.android.data.models.FetchType
import athul.svift.android.data.models.Song
import athul.svift.android.data.models.SongDao
import athul.svift.android.data.models.YoutubeMusicCloudResponse
import athul.svift.android.data.models.YoutubeMusicItem
import athul.svift.android.injection.Injection
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import sviftytd.DownloadCallback
import sviftytd.Sviftytd
import java.io.File

class SongsRepository(private val application: Application,private val songsDao: SongDao) {
    private val db = FirebaseFirestore.getInstance()
    val downloadFlow = MutableStateFlow("")
    suspend fun fetchLatestSongs(callback: FetchCallback){
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
                    callback.onFetchTypeDecided(FetchType.PARTIAL)
                    sendForDownload(newSongs,callback.downloadCallback)
                }else{
                    callback.onFetchTypeDecided(FetchType.NO_FETCH)
                }
            }else{
                callback.onFetchTypeDecided(FetchType.FULL)
                sendForDownload(songs,callback.downloadCallback)
            }
        }
    }

    suspend fun sendForDownload(list: List<YoutubeMusicItem>?,callback: DownloadCallback){
        val response = Sviftytd.downloadAudios(list?.map { it.videoId }?.joinToString(","),getSongsPath(),callback)
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val type = Types.newParameterizedType(List::class.java, Song::class.java)
        val adapter: JsonAdapter<List<Song>> = moshi.adapter(type)
        val songs = adapter.fromJson(response)
        songs?.forEach {song->
            song.time = list?.first { it.videoId == song.id  }?.time
        }
        songs?.let { songsDao.insertAll(it) }
    }


    private  fun fetchLastSongTimeStampInDb(): Long? {
        return songsDao.getAll()
            .maxByOrNull { it.time ?: 0L }
            ?.time
    }

    private fun getSongsPath(): String {
        val songsPath = File(application.filesDir, "songs")
        if (!songsPath.exists()) {
            songsPath.mkdirs()
        }
        return songsPath.absolutePath
    }
}