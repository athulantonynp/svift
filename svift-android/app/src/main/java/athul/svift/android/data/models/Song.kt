package athul.svift.android.data.models

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import com.squareup.moshi.JsonClass
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "songs")
@JsonClass(generateAdapter = true)
data class Song(
    @PrimaryKey
    val id:String,
    @ColumnInfo(name = "provider") val provider: String?,
    @ColumnInfo(name = "thumbnailURL") val thumbnailURL: String?,
    @ColumnInfo(name = "time") var time: Long?,
    @ColumnInfo(name = "title") val title: String?,
    @ColumnInfo(name = "author") val author: String?,
    @ColumnInfo(name = "fileName") val fileName: String?,
    @ColumnInfo(name = "filePath") val filePath: String?,
)


@Dao
interface SongDao{
    @Query("SELECT * FROM songs")
    fun getAllFlow(): Flow<List<Song>>

    @Query("SELECT * FROM songs")
    fun getAll(): List<Song>
    @Query("SELECT * FROM songs WHERE id IN (:ids)")
    fun loadAllByIds(ids: IntArray): List<Song>

    @Insert
    fun insertAll(songs: List<Song>)

    @Delete
    fun delete(song: Song)
}