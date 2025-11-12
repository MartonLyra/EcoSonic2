package com.seuapp.gravacaoaudio.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.seuapp.gravacaoaudio.domain.AudioRecorderService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reiniciar gravação se estava ativa antes do reboot
            // (Você pode usar SharedPreferences para armazenar o estado)
            // val prefs = context?.getSharedPreferences("state", Context.MODE_PRIVATE)
            // if (prefs?.getBoolean("isRecording", false) == true) {
            //     val startIntent = Intent(context, AudioRecorderService::class.java)
            //     startIntent.action = AudioRecorderService.ACTION_START
            //     context.startService(startIntent)
            // }
        }
    }
}