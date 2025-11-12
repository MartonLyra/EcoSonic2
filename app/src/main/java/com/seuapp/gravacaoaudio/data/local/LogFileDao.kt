package com.seuapp.gravacaoaudio.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.seuapp.gravacaoaudio.data.models.LogEntry

@Dao
interface LogFileDao {
    @Insert
    suspend fun insert(logEntry: LogEntry)

    @Query("SELECT * FROM log_entries ORDER BY timestamp DESC LIMIT 50")
    suspend fun getRecentLogs(): List<LogEntry>
}