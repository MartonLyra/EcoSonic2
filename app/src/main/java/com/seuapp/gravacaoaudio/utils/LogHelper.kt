package com.seuapp.gravacaoaudio.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*

object LogHelper {
    private val logFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private const val LOG_TAG = "GravacaoAudioLog"

    fun log(context: Context, message: String) {
        val date = logFormat.format(Date())
        val fileName = "$date - Arquivo de Log.log"

        // 1. Exibir no Logcat
        Log.d(LOG_TAG, message)

        // 2. Salvar no arquivo via SAF (append)
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val treeUriString = prefs.getString("storage_directory_uri", null)

        if (!treeUriString.isNullOrBlank()) {
            val treeUri = Uri.parse(treeUriString)

            // Verificar se temos permissão persistente para este URI
            if (!hasUriPermission(context, treeUri)) {
                Log.e("LogHelper", "Permissão persistente ausente para o URI de log: $treeUriString")
                return // Sai da função se não tiver permissão
            }

            val treeDocument = DocumentFile.fromTreeUri(context, treeUri)
            if (treeDocument != null && treeDocument.exists() && treeDocument.isDirectory) {
                // Caminho: Logs/yyyy/yyyy-MM/
                val year = date.substring(0, 4) // Ex: "2025"
                val month = date.substring(0, 7) // Ex: "2025-11"

                Log.d(LOG_TAG, "Tentando criar/obter diretório Logs em: $year/$month")

                var logsRootDir = treeDocument.findFile("Logs")
                if (logsRootDir == null || !logsRootDir.exists()) {
                    logsRootDir = treeDocument.createDirectory("Logs")
                    Log.d(LOG_TAG, "Diretório Logs criado: ${logsRootDir != null}")
                } else {
                    Log.d(LOG_TAG, "Diretório Logs já existe.") // Log de depuração
                }
                if (logsRootDir == null) {
                    Log.e(LOG_TAG, "Falha ao criar ou obter diretório Logs")
                    return
                }

                var yearDir = logsRootDir.findFile(year)
                if (yearDir == null || !yearDir.exists()) {
                    yearDir = logsRootDir.createDirectory(year)
                    Log.d(LOG_TAG, "Diretório Logs/$year criado: ${yearDir != null}")
                } else {
                    Log.d(LOG_TAG, "Diretório Logs/$year já existe.") // Log de depuração
                }
                if (yearDir == null) {
                    Log.e(LOG_TAG, "Falha ao criar ou obter diretório Logs/$year")
                    return
                }

                var monthDir = yearDir.findFile(month)
                if (monthDir == null || !monthDir.exists()) {
                    monthDir = yearDir.createDirectory(month)
                    Log.d(LOG_TAG, "Diretório Logs/$year/$month criado: ${monthDir != null}")
                } else {
                    Log.d(LOG_TAG, "Diretório Logs/$year/$month já existe.") // Log de depuração
                }
                if (monthDir == null) {
                    Log.e(LOG_TAG, "Falha ao criar ou obter diretório Logs/$year/$month")
                    return
                }

                var logFile = monthDir.findFile(fileName)
                if (logFile == null || !logFile.exists()) {
                    logFile = monthDir.createFile("text/plain", fileName)
                    Log.d(LOG_TAG, "Arquivo de log criado: ${logFile != null}")
                } else {
                    Log.d(LOG_TAG, "Arquivo de log já existe, será usado para append.") // Log de depuração
                }
                if (logFile == null) {
                    Log.e(LOG_TAG, "Falha ao criar ou obter arquivo de log: $fileName")
                    return
                }

                try {
                    // Abrir com "wa" para append
                    val outputStream = context.contentResolver.openOutputStream(logFile.uri, "wa")
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
                Log.e(LOG_TAG, "Diretório base para logs (SAF) não encontrado ou não é um diretório. URI: $treeUriString")
            }
        } else {
            Log.d(LOG_TAG, "[Fallback] $message")
        }
    }

    // Função auxiliar para verificar permissão
    private fun hasUriPermission(context: Context, uri: Uri): Boolean {
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        val contentResolver = context.contentResolver
        val perm = contentResolver.persistedUriPermissions.find { it.uri == uri && it.isReadPermission && it.isWritePermission }
        return perm != null
    }
}