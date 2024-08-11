package athul.svift.android.injection

import android.app.Application
import athul.svift.android.data.SongsRepository

object Injection {

    lateinit var songsRepository:SongsRepository

    fun init(app:Application){
        songsRepository = SongsRepository()
    }

}

fun SongsRepository() = Injection.songsRepository