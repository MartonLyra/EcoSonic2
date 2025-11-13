package com.seuapp.gravacaoaudio.utils

import android.content.Context

/**
 * Provedor de contexto global para uso em classes utilitárias que não são Activities ou Services.
 * Deve ser inicializado no Application.onCreate().
 */
object AppContextProvider {
    private var context: Context? = null

    fun init(context: Context) {
        AppContextProvider.context = context.applicationContext
    }

    fun getContext(): Context? = context
}