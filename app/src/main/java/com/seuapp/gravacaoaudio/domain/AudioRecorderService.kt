package com.seuapp.gravacaoaudio.domain

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.documentfile.provider.DocumentFile
import com.seuapp.gravacaoaudio.R
import com.seuapp.gravacaoaudio.ui.MainActivity
import com.seuapp.gravacaoaudio.utils.LogHelper
import com.seuapp.gravacaoaudio.utils.TimeUtils
import com.seuapp.gravacaoaudio.data.local.AppDatabase
import com.seuapp.gravacaoaudio.data.models.AudioFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.os.ParcelFileDescriptor
import java.text.SimpleDateFormat
import java.util.*

class AudioRecorderService : Service() {
    companion object {
        const val ACTION_START = "com.seuapp.gravacaoaudio.START"
        const val ACTION_STOP = "com.seuapp.gravacaoaudio.STOP"
        private const val LOG_TAG = "AudioRecorderService"
    }

    private var recorder: MediaRecorder? = null
    private var isRecording = false
    private var currentFileUri: Uri? = null
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
        val nextBlock = TimeUtils.getNextBlock() // Retorna o Date do próximo bloco
        val duration = nextBlock.time - System.currentTimeMillis()
        val fileName = TimeUtils.formatFileName(System.currentTimeMillis(), nextBlock.time) // Passe início e fim

        // Obter o diretório via TimeUtils
        val dir = TimeUtils.getAudioDir(this) // Passar Context (this)
        if (dir == null) {
            LogHelper.log(this, "Erro: Não foi possível obter o diretório de gravação.")
            stopSelf()
            return
        }

        // Procurar ou criar o arquivo dentro do diretório
        var audioFile = dir.findFile(fileName)
        if (audioFile == null || !audioFile.exists()) {
            audioFile = dir.createFile("audio/mpeg", fileName) // ou "audio/mp4" dependendo do formato
        }

        if (audioFile == null) {
            LogHelper.log(this, "Erro: Não foi possível criar o arquivo de áudio: $fileName")
            stopSelf()
            return
        }

        currentFileUri = audioFile.uri

        // Obter ParcelFileDescriptor para o arquivo via SAF
        val parcelFileDescriptor: ParcelFileDescriptor? = try {
            contentResolver.openFileDescriptor(audioFile.uri, "w") // "w" para escrita
        } catch (e: Exception) {
            LogHelper.log(this, "Erro ao abrir ParcelFileDescriptor para ${audioFile.uri}: ${e.message}")
            null
        }

        if (parcelFileDescriptor == null) {
            LogHelper.log(this, "Erro: ParcelFileDescriptor é nulo para o arquivo: $fileName")
            stopSelf()
            return
        }

        // Salvar no banco
        CoroutineScope(Dispatchers.IO).launch {
            val database = AppDatabase.getDatabase(this@AudioRecorderService)
            val filePathForDB = audioFile.uri.toString() // Salvar o URI como string no banco
            val audioFileEntry = AudioFile(
                fileName = fileName,
                filePath = filePathForDB, // Agora é um URI
                uploadStatus = "pending"
            )
            database.audioFileDao().insert(audioFileEntry)
        }

        LogHelper.log(this, "Iniciando gravação: $fileName")

        try {
            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4) // ou outro compatível
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(parcelFileDescriptor.fileDescriptor) // <-- CORRETO: Usando fileDescriptor do PFD
                prepare()
            }

            recorder?.start()
            isRecording = true

            LogHelper.log(this, "Gravação iniciada com sucesso: $fileName")

            handler.postDelayed({
                stopCurrentRecording()
                try {
                    parcelFileDescriptor.close() // <-- CORRETO: Fechar PFD
                } catch (e: Exception) {
                    LogHelper.log(this, "Erro ao fechar ParcelFileDescriptor: ${e.message}")
                }
                startRecording() // Começa a próxima gravação
            }, duration)
        } catch (e: Exception) {
            LogHelper.log(this, "Erro ao iniciar MediaRecorder para $fileName: ${e.message}")
            try {
                parcelFileDescriptor.close()
            } catch (closeEx: Exception) {
                LogHelper.log(this, "Erro adicional ao fechar PFD após falha no MediaRecorder: ${closeEx.message}")
            }
            stopSelf()
        }
    }

    private fun stopCurrentRecording() {
        if (isRecording) {
            try {
                recorder?.stop()
                LogHelper.log(this, "Gravação parcial concluída: ${currentFileUri?.toString() ?: "URI desconhecido"}")
            } catch (e: Exception) {
                LogHelper.log(this, "Erro ao parar MediaRecorder: ${e.message}")
            } finally {
                recorder?.release()
                recorder = null
                isRecording = false
            }
        }
    }

    private fun stopRecording() {
        if (isRecording) {
            stopCurrentRecording()
            LogHelper.log(this, "Serviço de gravação parado pelo usuário.")
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
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