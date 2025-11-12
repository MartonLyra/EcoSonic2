package com.seuapp.gravacaoaudio.ui

import android.os.Bundle
import android.widget.Button
import android.widget.Spinner
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.seuapp.gravacaoaudio.R
import com.seuapp.gravacaoaudio.utils.LogHelper

class CloudSettingsActivity : AppCompatActivity() {
    private lateinit var spCloudService: Spinner
    private lateinit var btnAuth: Button
    private lateinit var btnTestUpload: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cloud_settings)

        initViews()
        setupSpinner()
    }

    private fun initViews() {
        spCloudService = findViewById(R.id.spCloudService)
        btnAuth = findViewById(R.id.btnAuth)
        btnTestUpload = findViewById(R.id.btnTestUpload)

        btnAuth.setOnClickListener {
            authenticate()
        }

        btnTestUpload.setOnClickListener {
            testUpload()
        }
    }

    private fun setupSpinner() {
        val services = arrayOf("Google Drive", "Dropbox", "OneDrive")
        spCloudService.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, services)
    }

    private fun authenticate() {
        val selected = spCloudService.selectedItem.toString()
        LogHelper.log("Tentando autenticar com: $selected")
    }

    private fun testUpload() {
        LogHelper.log("Testando upload...")
    }
}