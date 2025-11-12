package com.seuapp.gravacaoaudio.domain

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.seuapp.gravacaoaudio.R
import com.seuapp.gravacaoaudio.ui.MainActivity
import com.seuapp.gravacaoaudio.utils.LogHelper
import com.seuapp.gravacaoaudio.utils.TimeUtils
import com.seuapp.gravacaoaudio.data.local.AppDatabase
import com.seuapp.gravacaoaudio.data.models.AudioFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class AudioRecorderService : Service() {
    companion object {
        const val ACTION_START = "com.seuapp.gravacaoaudio.START"
        const val ACTION_STOP = "com.seuapp.gravacaoaudio.STOP"
    }

    private var recorder: MediaRecorder? = null
    private var isRecording = false
    private var currentFile: File? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startRecording()
            ACTION_STOP -> stopRecording()
        }
        return START_STICKY
    }

    private fun startRecording() {
        createNotification()
        val nextBlock = TimeUtils.getNextBlock()
        val duration = nextBlock.time - System.currentTimeMillis()
        val fileName = TimeUtils.formatFileName(nextBlock)

        val dir = TimeUtils.getAudioDir()
        dir.mkdirs()

        currentFile = File(dir, fileName)

        // Salvar no banco
        CoroutineScope(Dispatchers.IO).launch {
            val database = AppDatabase.getDatabase(this@AudioRecorderService)
            val audioFile = AudioFile(
                fileName = fileName,
                filePath = currentFile?.absolutePath ?: "",
                uploadStatus = "pending"
            )
            database.audioFileDao().insert(audioFile)
        }

        LogHelper.log("Iniciando gravação: ${currentFile?.name}")

        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(currentFile?.absolutePath)
            prepare()
        }

        recorder?.start()
        isRecording = true

        handler.postDelayed({
            stopCurrentRecording()
            startRecording()
        }, duration)
    }

    private fun stopCurrentRecording() {
        if (isRecording) {
            recorder?.stop()
            recorder?.release()
            recorder = null
            isRecording = false
            LogHelper.log("Gravação concluída: ${currentFile?.name}")
        }
    }

    private fun stopRecording() {
        if (isRecording) {
            stopCurrentRecording()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun createNotification(): NotificationCompat.Builder {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val channelId = "AudioRecorderChannel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Serviço de Gravação",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Gravando áudio...")
            .setContentText("Serviço em execução")
            .setSmallIcon(R.drawable.ic_mic) // ou android.R.drawable.ic_dialog_info
            .setContentIntent(pendingIntent)
            .setOngoing(true)

        startForeground(1, notification.build()) // <-- Chama .build() aqui
        return notification
    }

    override fun onBind(intent: Intent?): IBinder? = null
}