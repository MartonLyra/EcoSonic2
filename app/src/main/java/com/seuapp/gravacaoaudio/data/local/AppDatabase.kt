package com.seuapp.gravacaoaudio.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.seuapp.gravacaoaudio.data.models.AudioFile
import com.seuapp.gravacaoaudio.data.models.LogEntry

@Database(
    entities = [AudioFile::class, LogEntry::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun audioFileDao(): AudioFileDao
    abstract fun logFileDao(): LogFileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "audio_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}