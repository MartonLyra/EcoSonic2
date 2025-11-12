package com.seuapp.gravacaoaudio.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.seuapp.gravacaoaudio.R
import java.io.File

class LogViewerActivity : AppCompatActivity() {
    private lateinit var tvLogContent: TextView
    private lateinit var btnRefresh: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_viewer)

        initViews()
        loadLogContent()
    }

    private fun initViews() {
        tvLogContent = findViewById(R.id.tvLogContent)
        btnRefresh = findViewById(R.id.btnRefresh)

        btnRefresh.setOnClickListener {
            loadLogContent()
        }
    }

    private fun loadLogContent() {
        val logFile = File("/storage/emulated/0/Documents/GravacoesAudio/Logs/${android.text.format.DateFormat.format("yyyy-MM-dd", java.util.Date())} - Arquivo de Log.log")
        if (logFile.exists()) {
            tvLogContent.text = logFile.readText()
        } else {
            tvLogContent.text = "Nenhum log encontrado."
        }
    }
}