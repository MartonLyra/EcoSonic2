package com.seuapp.gravacaoaudio.utils

import android.content.Context

interface CloudStorageService {
    suspend fun upload(filePath: String)

    companion object {
        fun getInstance(context: Context): CloudStorageService {
            return GoogleDriveService(context)
        }
    }
}

class GoogleDriveService(private val context: Context) : CloudStorageService {
    override suspend fun upload(filePath: String) {
        LogHelper.log("Fazendo upload para Google Drive: $filePath")
    }
}

class DropboxService : CloudStorageService {
    override suspend fun upload(filePath: String) {
        LogHelper.log("Fazendo upload para Dropbox: $filePath")
    }
}

class OneDriveService : CloudStorageService {
    override suspend fun upload(filePath: String) {
        LogHelper.log("Fazendo upload para OneDrive: $filePath")
    }
}