package athul.svift.android.data.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import athul.svift.android.data.models.SongMetadata

val supportedFormats = listOf(".mp3", ".mp4", ".webm", ".aac")
fun getMp3Metadata(context: Context, file: DocumentFile): SongMetadata? {
    val retriever = MediaMetadataRetriever()
    return try {
        // Obtain a FileDescriptor from the DocumentFile's URI
        val parcelFileDescriptor = context.contentResolver.openFileDescriptor(file.uri, "r")
        parcelFileDescriptor?.fileDescriptor?.let { fileDescriptor ->
            retriever.setDataSource(fileDescriptor) // Set the FileDescriptor as data source
        } ?: throw RuntimeException("Unable to open file descriptor for ${file.uri}")

        // Extract metadata
        var title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
        val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
        val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)

        val embeddedPicture = retriever.embeddedPicture // Retrieve the album art as a byte array
        var bitmap:Bitmap? = null
        if (embeddedPicture != null) {
            bitmap=  BitmapFactory.decodeByteArray(embeddedPicture, 0, embeddedPicture.size) // Convert to Bitmap
        }

        // If title is empty, derive it from the filename
        if (title.isNullOrEmpty()) {
            title = file.name?.let { filename ->
                supportedFormats.firstOrNull { filename.endsWith(it, ignoreCase = true) }
                    ?.let { filename.removeSuffix(it) }
                    ?: filename
            }
        }

        SongMetadata(title, artist, album,bitmap)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    } finally {
        retriever.release()
    }
}

fun Context.getAlbumArt(fileUri: Uri): Bitmap? {
    val retriever = MediaMetadataRetriever()
    try {
        val parcelFileDescriptor = contentResolver.openFileDescriptor(fileUri, "r")
        parcelFileDescriptor?.fileDescriptor?.let { fileDescriptor ->
            retriever.setDataSource(fileDescriptor) // Set the FileDescriptor as data source
        } ?: throw RuntimeException("Unable to open file descriptor for ${fileUri}")

    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        retriever.release() // Release the retriever resources
    }
    return null
}