package com.seuapp.gravacaoaudio.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audio_files")
data class AudioFile(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val fileName: String,
    val filePath: String,
    val uploadStatus: String = "pending",
    val createdAt: Long = System.currentTimeMillis()
)