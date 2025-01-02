package athul.svift.android.data.models

import android.graphics.Bitmap
import android.net.Uri
import androidx.documentfile.provider.DocumentFile

data class Song(
    val id:String,
    val file:DocumentFile,
    val metadata: SongMetadata?,
)

data class SongMetadata(
    val title: String?,
    val artist: String?,
    val album: String?,
    val albumArt:Bitmap?
)

fun SongMetadata.getDisplayableData():String{
    if(this.artist?.isEmpty() == false){
        return this.artist
    }
    if(this.album?.isEmpty() == false){
        return this.album
    }

    return "Unknown"
}