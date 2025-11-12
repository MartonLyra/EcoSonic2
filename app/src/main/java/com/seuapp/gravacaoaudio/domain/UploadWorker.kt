package com.seuapp.gravacaoaudio.domain

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.seuapp.gravacaoaudio.data.local.AppDatabase
import com.seuapp.gravacaoaudio.utils.LogHelper
import com.seuapp.gravacaoaudio.utils.CloudStorageService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UploadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val database = AppDatabase.getDatabase(applicationContext)
                val dao = database.audioFileDao()
                val pendingFiles = dao.getPendingFiles()

                val service = CloudStorageService.getInstance(applicationContext)

                for (file in pendingFiles) {
                    try {
                        LogHelper.log("Iniciando upload de: ${file.fileName}")
                        dao.update(file.copy(uploadStatus = "uploading"))
                        service.upload(file.filePath)
                        dao.update(file.copy(uploadStatus = "success"))
                        LogHelper.log("Upload concluído: ${file.fileName}")
                    } catch (e: Exception) {
                        LogHelper.log("Falha no upload: ${file.fileName}, Erro: ${e.message}")
                        dao.update(file.copy(uploadStatus = "failed"))
                        return@withContext Result.failure()
                    }
                }
                Result.success()
            } catch (e: Exception) {
                LogHelper.log("Erro no worker: ${e.message}")
                Result.failure()
            }
        }
    }
}