package athul.svift.android.injection

import android.app.Application
import athul.svift.android.data.AuthRepository
import athul.svift.android.data.SongsRepository

object Injection {

    lateinit var songsRepository:SongsRepository
    lateinit var authRepository: AuthRepository

    fun init(app:Application){
        songsRepository = SongsRepository()
        authRepository = AuthRepository()
    }

}

fun SongsRepository() = Injection.songsRepository
fun AuthRepository() = Injection.authRepository