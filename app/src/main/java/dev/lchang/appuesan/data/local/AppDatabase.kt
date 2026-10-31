package dev.lchang.appuesan.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FavoriteTeamEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun favoriteTeamDao(): FavoriteTeamDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Una sola instancia para toda la app (singleton)
        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "appuesan.db"
                ).build().also { INSTANCE = it }
            }
    }
}