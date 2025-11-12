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
        LogHelper.log(context,"Fazendo upload para Google Drive: $filePath")
    }
}

class DropboxService(private val context: Context) : CloudStorageService {
    override suspend fun upload(filePath: String) {
        LogHelper.log(context, "Fazendo upload para Dropbox: $filePath")
    }
}

class OneDriveService(private val context: Context) : CloudStorageService {
    override suspend fun upload(filePath: String) {
        LogHelper.log(context,"Fazendo upload para OneDrive: $filePath")
    }
}