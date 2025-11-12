package com.seuapp.gravacaoaudio.utils

import android.os.Environment
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

object LogHelper {
    private val logFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun log(message: String) {
        val date = logFormat.format(Date())
        val logFile = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "GravacoesAudio/Logs/$date - Arquivo de Log.log"
        )
        logFile.parentFile?.mkdirs()
        val writer = FileWriter(logFile, true)
        writer.appendLine("[${Date()}] $message")
        writer.close()
    }
}