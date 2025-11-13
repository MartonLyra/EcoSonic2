package com.seuapp.gravacaoaudio.ui

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.seuapp.gravacaoaudio.R
import com.seuapp.gravacaoaudio.utils.RootUtils

class SettingsActivity : AppCompatActivity() {
    private lateinit var spFilesPerHour: Spinner
    private lateinit var spAudioFormat: Spinner
    private lateinit var spAudioQuality: Spinner
    private lateinit var btnSelectStoragePath: Button
    private lateinit var tvSelectedPath: TextView
    private lateinit var btnRequestRoot: Button
    private lateinit var btnCloudSettings: Button
    private lateinit var btnRootStatus: Button

    private lateinit var sharedPreferences: SharedPreferences
    private val REQUEST_CODE_OPEN_DOCUMENT_TREE = 1001

    private val openDocumentTreeLauncher = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
        if (uri != null) {
            // Solicitar permissão persistente
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            try {
                contentResolver.takePersistableUriPermission(uri, takeFlags)
                // Salvar o URI no SharedPreferences
                sharedPreferences.edit {
                    putString("storage_directory_uri", uri.toString())
                }
                // Atualizar a TextView com o caminho
                updateSelectedPathText(uri.toString())
                Toast.makeText(this, "Pasta selecionada e permissão persistente concedida com sucesso!", Toast.LENGTH_LONG).show()
            } catch (e: SecurityException) {
                Toast.makeText(this, "Falha ao obter permissão persistente: ${e.message}", Toast.LENGTH_LONG).show()
                // Mesmo sem permissão persistente, você pode salvar o URI, mas ele não funcionará após o app fechar
                // Talvez mostrar um aviso ou impedir o salvamento
                sharedPreferences.edit {
                    putString("storage_directory_uri", uri.toString())
                }
                updateSelectedPathText(uri.toString())
                Toast.makeText(this, "Pasta selecionada, mas permissão pode não persistir após fechar o app.", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Nenhuma pasta selecionada.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sharedPreferences = getSharedPreferences("app_settings", MODE_PRIVATE)

        initViews()
        setupSpinners()
        loadSavedSettings()
    }

    private fun initViews() {
        spFilesPerHour = findViewById(R.id.spFilesPerHour)
        spAudioFormat = findViewById(R.id.spAudioFormat)
        spAudioQuality = findViewById(R.id.spAudioQuality)
        btnSelectStoragePath = findViewById(R.id.btnSelectStoragePath)
        tvSelectedPath = findViewById(R.id.tvSelectedPath)
        btnRequestRoot = findViewById(R.id.btnRequestRoot)
        btnCloudSettings = findViewById(R.id.btnCloudSettings)
        btnRootStatus = findViewById(R.id.btnRootStatus)

        btnSelectStoragePath.setOnClickListener {
            openDocumentTreeLauncher.launch(null)
        }

        // Adicionando listeners para salvar as seleções
        spFilesPerHour.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedValue = parent?.getItemAtPosition(position).toString()
                sharedPreferences.edit { putString("files_per_hour", selectedValue) }
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        spAudioFormat.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedValue = parent?.getItemAtPosition(position).toString()
                sharedPreferences.edit { putString("audio_format", selectedValue) }
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        spAudioQuality.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedValue = parent?.getItemAtPosition(position).toString()
                sharedPreferences.edit { putString("audio_quality", selectedValue) }
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
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
        val filesPerHour = arrayOf("1", "2", "3", "4", "6", "12", "15", "20", "30")
        val formats = arrayOf("MP3", "WAV", "OGG")
        val qualities = arrayOf("Alta", "Média", "Baixa")

        spFilesPerHour.adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, filesPerHour)
        spAudioFormat.adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, formats)
        spAudioQuality.adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, qualities)
    }

    private fun loadSavedSettings() {
        val savedFilesPerHour = sharedPreferences.getString("files_per_hour", "2") ?: "2"
        val savedFormat = sharedPreferences.getString("audio_format", "MP3") ?: "MP3"
        val savedQuality = sharedPreferences.getString("audio_quality", "Média") ?: "Média"
        val savedStorageUri = sharedPreferences.getString("storage_directory_uri", null)

        spFilesPerHour.setSelection(getIndex(spFilesPerHour, savedFilesPerHour))
        spAudioFormat.setSelection(getIndex(spAudioFormat, savedFormat))
        spAudioQuality.setSelection(getIndex(spAudioQuality, savedQuality))

        updateSelectedPathText(savedStorageUri)
    }

    private fun updateSelectedPathText(uriString: String?) {
        if (!uriString.isNullOrBlank()) {
            val uri = Uri.parse(uriString)
            tvSelectedPath.text = "Pasta selecionada: ${uri.path}"
        } else {
            tvSelectedPath.text = "Nenhuma pasta selecionada"
        }
    }

    private fun getIndex(spinner: Spinner, value: String): Int {
        val adapter = spinner.adapter
        for (i in 0 until adapter.count) {
            if (adapter.getItem(i).toString() == value) {
                return i
            }
        }
        return 0
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