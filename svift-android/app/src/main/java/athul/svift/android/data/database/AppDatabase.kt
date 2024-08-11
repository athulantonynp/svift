package athul.svift.android.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import athul.svift.android.data.models.Song
import athul.svift.android.data.models.SongDao

@Database(entities = [Song::class], version = 1)
abstract class AppDatabase:RoomDatabase() {

    abstract fun songDao():SongDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "svift_db"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}