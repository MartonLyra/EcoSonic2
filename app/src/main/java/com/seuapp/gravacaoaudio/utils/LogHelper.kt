package com.seuapp.gravacaoaudio.utils

import android.os.Environment
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import java.io.OutputStreamWriter
import java.util.*

object LogHelper {
    private val logFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private const val LOG_TAG = "LogHelper"

    // Recebe o contexto para acessar SharedPreferences e DocumentFile
    fun log(context: Context, message: String) {
        val date = logFormat.format(Date())
        val fileName = "$date - Arquivo de Log.log"

        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val treeUriString = prefs.getString("storage_directory_uri", null)

        if (!treeUriString.isNullOrBlank()) {
            val treeUri = Uri.parse(treeUriString)
            val treeDocument = DocumentFile.fromTreeUri(context, treeUri)
            if (treeDocument != null && treeDocument.exists() && treeDocument.isDirectory) {
                // Caminho: Logs/yyyy/yyyy-MM/
                val year = date.substring(0, 4)
                val month = date.substring(0, 7) // yyyy-MM

                var logsRootDir = treeDocument.findFile("Logs")
                if (logsRootDir == null || !logsRootDir.exists()) {
                    logsRootDir = treeDocument.createDirectory("Logs")
                }
                if (logsRootDir == null) {
                    Log.e(LOG_TAG, "Falha ao criar diretório Logs")
                    return
                }

                var yearDir = logsRootDir.findFile(year)
                if (yearDir == null || !yearDir.exists()) {
                    yearDir = logsRootDir.createDirectory(year)
                }
                if (yearDir == null) {
                    Log.e(LOG_TAG, "Falha ao criar diretório Logs/$year")
                    return
                }

                var monthDir = yearDir.findFile("$year-$month")
                if (monthDir == null || !monthDir.exists()) {
                    monthDir = yearDir.createDirectory("$year-$month")
                }
                if (monthDir == null) {
                    Log.e(LOG_TAG, "Falha ao criar diretório Logs/$year/$year-$month")
                    return
                }

                var logFile = monthDir.findFile(fileName)
                if (logFile == null || !logFile.exists()) {
                    logFile = monthDir.createFile("text/plain", fileName)
                }
                if (logFile == null) {
                    Log.e(LOG_TAG, "Falha ao criar arquivo de log: $fileName")
                    return
                }

                try {
                    val outputStream = context.contentResolver.openOutputStream(logFile.uri, "wa") // "wa" para append
                    if (outputStream != null) {
                        val writer = OutputStreamWriter(outputStream)
                        writer.appendLine("[${Date()}] $message")
                        writer.close()
                        Log.d(LOG_TAG, "Log escrito em: ${logFile.uri}")
                    } else {
                        Log.e(LOG_TAG, "OutputStream nula ao tentar escrever log em: ${logFile.uri}")
                    }
                } catch (e: Exception) {
                    Log.e(LOG_TAG, "Erro ao escrever log em ${logFile.uri}: ${e.message}", e)
                }
            } else {
                Log.e(LOG_TAG, "Diretório base para logs não encontrado ou não é um diretório.")
            }
        } else {
            // Fallback para log interno ou console
            Log.d(LOG_TAG, "[Fallback] $message")
        }
    }
}