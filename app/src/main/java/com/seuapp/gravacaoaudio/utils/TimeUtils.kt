package com.seuapp.gravacaoaudio.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {
    // Formato para a data no nome do arquivo: yyyy-MM-dd
    private val fileNameDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Formato para ano e ano-mês (para pastas)
    private val yearMonthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    private val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())

    fun getNextBlock(): Date {
        val now = Date() // Sempre pega o horário atual
        val calendar = Calendar.getInstance().apply { time = now }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        // Obter o número de arquivos por hora das preferências
        val context = AppContextProvider.getContext() // Obter o contexto global
        val prefs = context?.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        // Validação adicional
        val filesPerHourString = prefs?.getString("files_per_hour", "2")
        val filesPerHour = filesPerHourString?.toIntOrNull() ?: 2
        val intervalMinutes = 60 / filesPerHour

        val nextMinute = ((minute / intervalMinutes) + 1) * intervalMinutes
        if (nextMinute >= 60) {
            calendar.set(Calendar.HOUR_OF_DAY, (hour + 1) % 24) // Avança 1 hora se passar de 59
            calendar.set(Calendar.MINUTE, 0)
        } else {
            calendar.set(Calendar.MINUTE, nextMinute) // Define para o próximo bloco
        }
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        Log.d("TimeUtils", "getNextBlock: Agora=${now}, Próximo Bloco=${calendar.time}") // Log de depuração

        return calendar.time
    }

    // Modificado para receber startTime e endTime em millis e formatar corretamente
    fun formatFileName(startTimeMillis: Long, endTimeMillis: Long): String {
        val start = Date(startTimeMillis)
        val end = Date(endTimeMillis)

        // Instanciar os formatadores dentro da função para garantir isolamento
        val startDatePart = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(start)
        val startTimePart = SimpleDateFormat("HH'h'mm", Locale.getDefault()).format(start) // Formato da hora de início - Corrigido para 'mm'
        val endTimePart = SimpleDateFormat("HH'h'mm", Locale.getDefault()).format(end)   // Formato da hora de término - Corrigido para 'mm'

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

        Log.d("TimeUtils", "formatFileName: Início=${start}, Fim=${end}")
        Log.d("TimeUtils", "formatFileName: startTimePart=${startTimePart}, endTimePart=${endTimePart}")
        Log.d("TimeUtils", "formatFileName: Nome Gerado=${fileName}") // Log de depuração

        // Exemplo: "2025-11-12 - 14h58 to 15h00.mp3"
        // A data usada é a do início, apenas a hora do fim é adicionada.
        return fileName
    }

    // Modificado para usar Context e SharedPreferences para obter o URI do diretório base
    fun getAudioDir(context: Context): DocumentFile? {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val treeUriString = prefs.getString("storage_directory_uri", null)

        if (!treeUriString.isNullOrBlank()) {
            val treeUri = Uri.parse(treeUriString)

            // Verificar se temos permissão persistente para este URI
            if (!hasUriPermission(context, treeUri)) {
                Log.e("TimeUtils", "Permissão persistente ausente para o URI: $treeUriString")
                return null
            }

            val treeDocument = DocumentFile.fromTreeUri(context, treeUri)
            if (treeDocument != null && treeDocument.exists() && treeDocument.isDirectory) {
                val currentYearMonth = yearMonthFormat.format(Date()) // Ex: "2025-11"
                val currentYear = yearFormat.format(Date()) // Ex: "2025"

                Log.d("TimeUtils", "Criando/obtendo pasta áudio em: $currentYear/$currentYearMonth")

                // Criar pasta "yyyy/yyyy-MM/"
                var yearDir = treeDocument.findFile(currentYear)
                if (yearDir == null || !yearDir.exists()) {
                    yearDir = treeDocument.createDirectory(currentYear)
                    if (yearDir == null) {
                        Log.e("TimeUtils", "Falha ao criar diretório do ano: $currentYear")
                        return null
                    }
                } else {
                    Log.d("TimeUtils", "Diretório do ano já existe: $currentYear") // Log de depuração
                }

                var monthDir = yearDir.findFile(currentYearMonth)
                if (monthDir == null || !monthDir.exists()) {
                    monthDir = yearDir.createDirectory(currentYearMonth)
                    if (monthDir == null) {
                        Log.e("TimeUtils", "Falha ao criar diretório do mês: $currentYearMonth")
                        return null
                    }
                } else {
                    Log.d("TimeUtils", "Diretório do mês já existe: $currentYearMonth") // Log de depuração
                }

                return monthDir
            } else {
                Log.e("TimeUtils", "Diretório base do SAF não encontrado ou não é diretório. URI: $treeUriString")
            }
        } else {
            Log.e("TimeUtils", "URI do diretório de armazenamento não está definido nas preferências.")
        }
        return null
    }

    // Função para verificar permissão persistente
    private fun hasUriPermission(context: Context, uri: Uri): Boolean {
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        val contentResolver = context.contentResolver
        val perm = contentResolver.persistedUriPermissions.find { it.uri == uri && it.isReadPermission && it.isWritePermission }
        return perm != null
    }
}