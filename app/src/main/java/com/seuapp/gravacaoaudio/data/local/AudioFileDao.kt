package com.seuapp.gravacaoaudio.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.seuapp.gravacaoaudio.data.models.AudioFile

@Dao
interface AudioFileDao {
    @Insert
    suspend fun insert(audioFile: AudioFile)

    @Update
    suspend fun update(audioFile: AudioFile)

    @Query("SELECT * FROM audio_files WHERE uploadStatus = 'pending' OR uploadStatus = 'failed'")
    suspend fun getPendingFiles(): List<AudioFile>

    @Query("SELECT * FROM audio_files ORDER BY createdAt DESC LIMIT 10")
    suspend fun getRecentFiles(): List<AudioFile>
}