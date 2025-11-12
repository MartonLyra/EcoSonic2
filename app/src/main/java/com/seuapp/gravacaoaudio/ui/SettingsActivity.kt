package com.seuapp.gravacaoaudio.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.seuapp.gravacaoaudio.R
import com.seuapp.gravacaoaudio.utils.RootUtils

class SettingsActivity : AppCompatActivity() {
    private lateinit var spFilesPerHour: Spinner
    private lateinit var spAudioFormat: Spinner
    private lateinit var spAudioQuality: Spinner
    private lateinit var etStoragePath: EditText
    private lateinit var btnRequestPermissions: Button
    private lateinit var btnRequestRoot: Button
    private lateinit var btnCloudSettings: Button
    private lateinit var btnRootStatus: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        initViews()
        setupSpinners()
    }

    private fun initViews() {
        spFilesPerHour = findViewById(R.id.spFilesPerHour)
        spAudioFormat = findViewById(R.id.spAudioFormat)
        spAudioQuality = findViewById(R.id.spAudioQuality)
        etStoragePath = findViewById(R.id.etStoragePath)
        btnRequestPermissions = findViewById(R.id.btnRequestPermissions)
        btnRequestRoot = findViewById(R.id.btnRequestRoot)
        btnCloudSettings = findViewById(R.id.btnCloudSettings)
        btnRootStatus = findViewById(R.id.btnRootStatus)

        btnRequestPermissions.setOnClickListener {
            requestPermissions()
        }

        btnRequestRoot.setOnClickListener {
            requestRootPermission()
        }

        btnCloudSettings.setOnClickListener {
            startActivity(Intent(this, CloudSettingsActivity::class.java))
        }

        btnRootStatus.setOnClickListener {
            startActivity(Intent(this, RootStatusActivity::class.java))
        }
    }

    private fun setupSpinners() {
        val filesPerHour = arrayOf("1", "2", "3", "4", "6", "12")
        val formats = arrayOf("MP3", "WAV", "OGG")
        val qualities = arrayOf("Alta", "Média", "Baixa")

        spFilesPerHour.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filesPerHour)
        spAudioFormat.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, formats)
        spAudioQuality.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, qualities)
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        ActivityCompat.requestPermissions(this, permissions, 100)
    }

    private fun requestRootPermission() {
        if (!RootUtils.isDeviceRooted()) {
            Toast.makeText(this, "O dispositivo não está rooted.", Toast.LENGTH_LONG).show()
            return
        }

        if (RootUtils.hasRootPermission()) {
            Toast.makeText(this, "O app já tem permissão root.", Toast.LENGTH_SHORT).show()
        } else {
            val success = RootUtils.executeRootCommand("echo 'Testing root access'")
            if (success) {
                Toast.makeText(this, "Permissão root concedida com sucesso!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Falha ao obter permissão root. O usuário negou ou ocorreu um erro.", Toast.LENGTH_LONG).show()
            }
        }
    }
}