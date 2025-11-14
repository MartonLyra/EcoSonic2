package com.seuapp.gravacaoaudio.domain

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.DocumentsContract
import android.util.Log // Importação necessária
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
import com.seuapp.gravacaoaudio.utils.AppContextProvider
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
    private var currentFileName: String? = null
    private var currentFileUri: Uri? = null // Armazena o URI do arquivo atual para renomear
    private var currentStartTime: Date? = null // Armazena o DateTime de início
    private var currentEndTimeExpected: Date? = null // Armazena o DateTime final previsto
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
        val nextBlock = TimeUtils.getNextBlock() // Retorna o Date do próximo bloco a partir de *agora*
        val duration = nextBlock.time - System.currentTimeMillis()
        val startTime = System.currentTimeMillis() // Captura o tempo real de início da gravação
        val startTimeDate = Date(startTime)
        val endTimeExpectedDate = nextBlock // O Date retornado por getNextBlock é o final previsto

        Log.d(LOG_TAG, "startRecording: startTime=${startTimeDate}, nextBlock=${endTimeExpectedDate}, duration=${duration}ms")

        // Armazena DateTime de início e final previsto
        currentStartTime = startTimeDate
        currentEndTimeExpected = endTimeExpectedDate

        // Gera o nome do arquivo usando a nova função
        val fileName = generateFileName(currentStartTime!!, currentEndTimeExpected!!)

        // Armazena o nome do arquivo para uso nos logs e renomeação
        currentFileName = fileName

        // Obter o diretório via TimeUtils
        val dir = TimeUtils.getAudioDir(this)
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

        currentFileUri = audioFile.uri // Armazena o URI para renomear depois

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
            val filePathForDB = audioFile.uri.toString()
            val audioFileEntry = AudioFile(
                fileName = fileName,
                filePath = filePathForDB,
                uploadStatus = "pending"
            )
            database.audioFileDao().insert(audioFileEntry)
        }

        LogHelper.log(this, "Iniciando gravação do arquivo: $fileName")

        try {
            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(parcelFileDescriptor.fileDescriptor)
                prepare()
            }

            recorder?.start()
            isRecording = true

            LogHelper.log(this, "Gravação do arquivo iniciada com sucesso: $fileName")

            // Agendar a parada e início da próxima gravação
            handler.postDelayed({
                // Ao concluir a gravação, chama stopCurrentRecording
                stopCurrentRecording()
                try {
                    parcelFileDescriptor.close()
                } catch (e: Exception) {
                    LogHelper.log(this, "Erro ao fechar ParcelFileDescriptor para $fileName: ${e.message}")
                }
                // Chama startRecording novamente para o próximo bloco
                startRecording()
            }, duration)
        } catch (e: Exception) {
            LogHelper.log(this, "Erro ao iniciar MediaRecorder para $fileName: ${e.message}")
            try {
                parcelFileDescriptor.close()
            } catch (closeEx: Exception) {
                LogHelper.log(this, "Erro adicional ao fechar PFD após falha no MediaRecorder para $fileName: ${closeEx.message}")
            }
            stopSelf()
        }
    }

    private fun stopCurrentRecording() {
        if (isRecording) {
            try {
                recorder?.stop()
            } catch (e: Exception) {
                LogHelper.log(this, "Erro ao parar MediaRecorder para ${currentFileName ?: "Nome desconhecido"}: ${e.message}")
            } finally {
                recorder?.release()
                recorder = null
                isRecording = false
                // currentFileName e currentFileUri são limpos após a gravação
                // currentFileName = null // Não limpe aqui, pois renameAudioFile precisa
                // currentFileUri = null   // Não limpe aqui, pois renameAudioFile precisa
            }

            try {
                // Registra o horário real de término
                val realEndTime = Date() // Horário real de término
                Log.d(LOG_TAG, "Gravação real concluída em: $realEndTime")

                // Gera o nome do arquivo com o horário real de término
                val realEndTimeTruncated = truncateToMinutes(realEndTime) // Remove segundos e milissegundos
                val realEndTimeExpectedTruncated = truncateToMinutes(currentEndTimeExpected!!) // Remove segundos e milissegundos do previsto

                // Verifica se o término real é diferente do previsto
                if (realEndTimeTruncated != realEndTimeExpectedTruncated) {
                    val newFileName = generateFileName(currentStartTime!!, realEndTimeTruncated)
                    Log.d(LOG_TAG, "Término real difere do previsto. Previsto: $realEndTimeExpectedTruncated, Real: $realEndTimeTruncated. Novo nome: $newFileName")
                    // Renomeia o arquivo se o nome for diferente
                    if (newFileName != currentFileName) {
                        renameAudioFile(newFileName)
                        LogHelper.log(this, "Gravação do arquivo concluída e renomeada de '${currentFileName}' para '$newFileName'")
                    } else {
                        LogHelper.log(this, "Gravação do arquivo concluída (interrompida antes do tempo, mas nome já correto): ${currentFileName ?: "Nome desconhecido"}")
                    }
                } else {
                    LogHelper.log(this, "Gravação do arquivo concluída no horário previsto: ${currentFileName ?: "Nome desconhecido"}")
                }

            } catch (e: Exception) {

                // Mesmo com erro, tente registrar o término e renomear se necessário
                val realEndTime = Date()
                val realEndTimeTruncated = truncateToMinutes(realEndTime)
                val realEndTimeExpectedTruncated = truncateToMinutes(currentEndTimeExpected!!)
                if (realEndTimeTruncated != realEndTimeExpectedTruncated) {
                    val newFileName = generateFileName(currentStartTime!!, realEndTimeTruncated)
                    if (newFileName != currentFileName) {
                        renameAudioFile(newFileName)
                        LogHelper.log(this, "Gravação do arquivo concluída com erro e renomeada de '${currentFileName}' para '$newFileName'")
                    } else {
                        LogHelper.log(this, "Gravação do arquivo concluída com erro (mas nome já correto): ${currentFileName ?: "Nome desconhecido"}")
                    }
                } else {
                    LogHelper.log(this, "Gravação do arquivo concluída com erro no horário previsto: ${currentFileName ?: "Nome desconhecido"}")
                }
            } finally {
                // recorder?.release()
                // recorder = null
                // isRecording = false
                // currentFileName e currentFileUri são limpos após a gravação
                // currentFileName = null // Não limpe aqui, pois renameAudioFile precisa
                // currentFileUri = null   // Não limpe aqui, pois renameAudioFile precisa
            }
        }
    }

    private fun stopRecording() {
        if (isRecording) {
            stopCurrentRecording()
            // Após stopCurrentRecording, currentEndTimeExpected e currentFileUri estarão definidos
            // Tente renomear se necessário (caso o handler.postDelayed ainda não tenha sido executado)
            val realEndTime = Date() // Horário de término quando o usuário para
            val realEndTimeTruncated = truncateToMinutes(realEndTime)
            val realEndTimeExpectedTruncated = truncateToMinutes(currentEndTimeExpected!!)
            if (realEndTimeTruncated != realEndTimeExpectedTruncated) {
                val newFileName = generateFileName(currentStartTime!!, realEndTimeTruncated)
                if (newFileName != currentFileName) {
                    renameAudioFile(newFileName)
                    LogHelper.log(this, "Serviço de gravação parado pelo usuário, arquivo renomeado de '${currentFileName}' para '$newFileName'")
                } else {
                    LogHelper.log(this, "Serviço de gravação parado pelo usuário, arquivo finalizado no horário previsto: ${currentFileName ?: "Nome desconhecido"}")
                }
            } else {
                LogHelper.log(this, "Serviço de gravação parado pelo usuário, arquivo finalizado no horário previsto: ${currentFileName ?: "Nome desconhecido"}")
            }
        }
        currentFileName = null // Limpa o nome ao parar o serviço
        currentFileUri = null  // Limpa o URI ao parar o serviço
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    // Função para gerar o nome do arquivo com base nos DateTime
    private fun generateFileName(startTime: Date, endTime: Date): String {
        // Exemplo: original = "2025-11-12 - 20h45 to 20h50.mp3"
        val startDatePart = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(startTime)
        val startTimePart = SimpleDateFormat("HH'h'mm", Locale.getDefault()).format(startTime) // Corrigido para minutos (mm)
        val endTimePart = SimpleDateFormat("HH'h'mm", Locale.getDefault()).format(endTime)   // Corrigido para minutos (mm)

        // Obter o formato de áudio das preferências
        val context = AppContextProvider.getContext()
        val prefs = context?.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val audioFormat = prefs?.getString("audio_format", "MP3") ?: "MP3"
        val fileExtension = when (audioFormat.lowercase()) {
            "wav" -> ".wav"
            "ogg" -> ".ogg"
            else -> ".mp3" // Padrão para MP3 se não for WAV ou OGG
        }

        val fileName = "$startDatePart - $startTimePart to $endTimePart$fileExtension"
        Log.d(LOG_TAG, "generateFileName: Início=${startTime}, Fim=${endTime}")
        Log.d(LOG_TAG, "generateFileName: startTimePart=${startTimePart}, endTimePart=${endTimePart}")
        Log.d(LOG_TAG, "generateFileName: Nome Gerado=${fileName}") // Log de depuração

        return fileName
    }

    // Função para truncar Date para minutos (remove segundos e milissegundos)
    private fun truncateToMinutes(date: Date): Date {
        val calendar = Calendar.getInstance().apply { time = date }
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    // Função para renomear o arquivo via SAF
    private fun renameAudioFile(newName: String) {
        val uri = currentFileUri
        if (uri != null) {
            try {
                val renamedUri = DocumentsContract.renameDocument(contentResolver, uri, newName)
                if (renamedUri != null) {
                    LogHelper.log(this, "Arquivo renomeado com sucesso para: $newName")
                    // Atualizar o URI e o nome armazenado
                    currentFileUri = renamedUri
                    currentFileName = newName
                    // Talvez atualizar o banco de dados também, se armazenar o nome lá
                    // Exemplo (você precisaria adaptar para seu DAO e modelo):
                    // CoroutineScope(Dispatchers.IO).launch {
                    //     val database = AppDatabase.getDatabase(this@AudioRecorderService)
                    //     database.audioFileDao().updateFileName(uri.toString(), newName)
                    // }
                } else {
                    LogHelper.log(this, "Falha ao renomear arquivo via SAF: $newName")
                }
            } catch (e: Exception) {
                LogHelper.log(this, "Erro ao renomear arquivo: ${e.message}")
            }
        } else {
            LogHelper.log(this, "URI do arquivo atual é nulo, não é possível renomear.")
        }
    }

    // Muda o tipo de retorno para Unit (função que não retorna nada)
    private fun createNotification() {
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

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Gravando áudio...")
            .setContentText("Serviço em execução")
            .setSmallIcon(R.drawable.ic_mic) // ou android.R.drawable.ic_dialog_info
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build() // <-- Chamada de .build() permanece aqui

        startForeground(1, notification) // <-- Chamada de startForeground permanece aqui
        // Não há mais 'return notification'
    }

    override fun onBind(intent: Intent?): IBinder? = null
}