package com.seuapp.gravacaoaudio.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.seuapp.gravacaoaudio.R
import com.seuapp.gravacaoaudio.domain.AudioRecorderService

class MainActivity : AppCompatActivity() {

    private lateinit var btnToggleRecording: Button
    private lateinit var btnForceUpload: Button
    private lateinit var btnViewLogs: Button
    private lateinit var btnSettings: Button
    private lateinit var tvStatus: TextView

    private var isRecording = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        checkPermissions()
        updateUI()
    }

    private fun initViews() {
        btnToggleRecording = findViewById(R.id.btnToggleRecording)
        btnForceUpload = findViewById(R.id.btnForceUpload)
        btnViewLogs = findViewById(R.id.btnViewLogs)
        btnSettings = findViewById(R.id.btnSettings)
        tvStatus = findViewById(R.id.tvStatus)

        btnToggleRecording.setOnClickListener {
            toggleRecording()
        }

        btnForceUpload.setOnClickListener {
            // TODO: Chamar UploadWorker
        }

        btnViewLogs.setOnClickListener {
            startActivity(Intent(this, LogViewerActivity::class.java))
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun toggleRecording() {
        if (!isRecording) {
            startRecording()
        } else {
            stopRecording()
        }
        isRecording = !isRecording
        updateUI()
    }

    private fun startRecording() {
        val intent = Intent(this, AudioRecorderService::class.java)
        intent.action = AudioRecorderService.ACTION_START
        startService(intent)
    }

    private fun stopRecording() {
        val intent = Intent(this, AudioRecorderService::class.java)
        intent.action = AudioRecorderService.ACTION_STOP
        startService(intent)
    }

    private fun updateUI() {
        btnToggleRecording.text = if (isRecording) getString(R.string.stop_recording) else getString(R.string.start_recording)
        tvStatus.text = if (isRecording) getString(R.string.recording_active) else getString(R.string.recording_inactive)
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        val neededPermissions = permissions.filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        }

        if (neededPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, neededPermissions.toTypedArray(), 100)
        }
    }
}