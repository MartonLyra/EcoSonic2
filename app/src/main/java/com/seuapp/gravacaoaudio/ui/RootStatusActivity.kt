package com.seuapp.gravacaoaudio.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.seuapp.gravacaoaudio.R
import com.seuapp.gravacaoaudio.utils.RootUtils

class RootStatusActivity : AppCompatActivity() {
    private lateinit var tvRootStatus: TextView
    private lateinit var btnCheckRoot: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_root_status)

        initViews()
        checkRootStatus()
    }

    private fun initViews() {
        tvRootStatus = findViewById(R.id.tvRootStatus)
        btnCheckRoot = findViewById(R.id.btnCheckRoot)

        btnCheckRoot.setOnClickListener {
            checkRootStatus()
        }
    }

    private fun checkRootStatus() {
        val isRooted = RootUtils.isDeviceRooted()
        val hasPermission = RootUtils.hasRootPermission()

        val statusText = if (isRooted) {
            if (hasPermission) {
                "✅ Dispositivo está rooted e app tem permissão."
            } else {
                "⚠️ Dispositivo está rooted, mas app NÃO tem permissão."
            }
        } else {
            "❌ Dispositivo NÃO está rooted."
        }

        tvRootStatus.text = statusText
    }
}