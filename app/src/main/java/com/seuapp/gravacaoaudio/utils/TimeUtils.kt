package com.seuapp.gravacaoaudio.utils

import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val fileNameFormat = SimpleDateFormat("HH'h'mm", Locale.getDefault())

    fun getNextBlock(): Date {
        val now = Date()
        val calendar = Calendar.getInstance().apply { time = now }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val filesPerHour = 2
        val intervalMinutes = 60 / filesPerHour

        val nextMinute = ((minute / intervalMinutes) + 1) * intervalMinutes
        if (nextMinute >= 60) {
            calendar.set(Calendar.HOUR_OF_DAY, (hour + 1) % 24)
            calendar.set(Calendar.MINUTE, 0)
        } else {
            calendar.set(Calendar.MINUTE, nextMinute)
        }
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.time
    }

    fun formatFileName(endTime: Date): String {
        val start = Date(System.currentTimeMillis())
        val startFormatted = dateFormat.format(start) + " – " + fileNameFormat.format(start)
        val endFormatted = fileNameFormat.format(endTime)
        return "$startFormatted à $endFormatted.mp3"
    }

    fun getAudioDir(): File {
        val year = dateFormat.format(Date()).substring(0, 4)
        val month = dateFormat.format(Date())
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
            "GravacoesAudio/$year/$month"
        )
        return dir
    }
}