package com.seuapp.gravacaoaudio

import android.app.Application
import com.seuapp.gravacaoaudio.utils.AppContextProvider

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializa o provedor de contexto global
        AppContextProvider.init(this)
    }
}