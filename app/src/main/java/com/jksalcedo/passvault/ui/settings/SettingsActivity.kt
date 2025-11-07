package com.jksalcedo.passvault.ui.settings

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.jksalcedo.passvault.R
import com.jksalcedo.passvault.crypto.Encryption
import com.jksalcedo.passvault.data.PasswordEntry
import com.jksalcedo.passvault.utils.Utility
import com.jksalcedo.passvault.viewmodel.PasswordViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private lateinit var viewModel: PasswordViewModel

    // Launcher for creating (exporting) a file
    private val createFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    // Fetch the data once from the ViewModel.
                    viewModel.allEntries.observe(this) { entries ->
                        exportEntries(entries, uri)
                    }
                }
            }
        }

    // Launcher for opening (importing) a file
    private val openFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    importEntries(uri)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Show Preference Fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.settings_fragment_container, SettingsFragment())
            .commit()

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[PasswordViewModel::class.java]
    }

    private fun ensurePasswordExists(onPasskeyReady: () -> Unit) {
        val sharedPrefs = getSharedPreferences("settings", MODE_PRIVATE)
        val storedPasskey = sharedPrefs.getString("passkey", null)

        if (storedPasskey.isNullOrEmpty()) {
            // No passkey found
            val layout = layoutInflater.inflate(R.layout.dialog_passkey, null)
            val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(layout)
                .setCancelable(false)
                .setTitle(R.string.set_passkey)
                .setMessage("Create a password to encrypt your backup.")
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create()

            dialog.setOnShowListener {
                val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                positiveButton.setOnClickListener {
                    val passkeyEditText = layout.findViewById<TextInputEditText>(R.id.etPasskey)
                    val confirmPasskeyEditText =
                        layout.findViewById<TextInputEditText>(R.id.etConfirmPasskey)
                    val newPasskey = passkeyEditText.text.toString()
                    val confirmPasskey = confirmPasskeyEditText.text.toString()

                    when {
                        newPasskey.isBlank() -> {
                            passkeyEditText.error = "Password cannot be empty"
                        }

                        newPasskey != confirmPasskey -> {
                            confirmPasskeyEditText.error = "Password do not match"
                        }

                        else -> {
                            // Passkeys match and not empty, save it
                            sharedPrefs.edit().putString("passkey", newPasskey).apply()
                            Toast.makeText(this, "Password saved", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                            onPasskeyReady() // Proceed
                        }
                    }
                }
            }
            dialog.show()
        } else {
            // Passkey already exists, proceed
            onPasskeyReady()
        }
    }


    fun createFileForExport() {
        ensurePasswordExists {
            val formatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val fileName = "passvault_backup_${formatter.format(Date())}.json"

            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json" // More specific type
                putExtra(Intent.EXTRA_TITLE, fileName)
            }
            createFileLauncher.launch(intent)
        }
    }

    private fun exportEntries(entries: List<PasswordEntry>, uri: Uri) {
        val passkey = getSharedPreferences("settings", MODE_PRIVATE).getString("passkey", null)
        if (passkey.isNullOrEmpty()) {
            Toast.makeText(this, "Export failed: Passkey not found.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val serializedData = Utility.serializeEntries(entries)
                val isEncryptionEnabled = getSharedPreferences("settings", MODE_PRIVATE)
                    .getBoolean("encrypt_backups", true)

                val contentToWrite = if (isEncryptionEnabled) {
                    Encryption.encryptFileContent(serializedData, passkey)
                } else {
                    serializedData
                }

                saveToFile(contentToWrite, uri)
                Toast.makeText(this@SettingsActivity, "Export successful!", Toast.LENGTH_SHORT)
                    .show()
            } catch (e: Exception) {
                Toast.makeText(
                    this@SettingsActivity,
                    "Export failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun openFileForImport() {
        ensurePasswordExists {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*" // Allow selection of any file type
            }
            openFileLauncher.launch(intent)
        }
    }

    private fun importEntries(uri: Uri) {
        val passkey = getSharedPreferences("settings", MODE_PRIVATE).getString("passkey", null)
        if (passkey.isNullOrEmpty()) {
            Toast.makeText(this, "Import failed: Passkey not found.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val fileContent = readFromFile(uri)
                val isEncryptionEnabled = getSharedPreferences("settings", MODE_PRIVATE)
                    .getBoolean("encrypt_backups", true)

                val decryptedJson = if (isEncryptionEnabled) {
                    Encryption.decryptFileContent(fileContent, passkey)
                } else {
                    fileContent
                }

                val entries = Utility.deserializeEntries(decryptedJson)
                entries.forEach { viewModel.insert(it) }

                Toast.makeText(
                    this@SettingsActivity,
                    "${entries.size} entries imported successfully!",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@SettingsActivity,
                    "Import failed: Invalid passkey or corrupt file.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private suspend fun saveToFile(content: String, uri: Uri) = withContext(Dispatchers.IO) {
        try {
            contentResolver.openFileDescriptor(uri, "w")?.use {
                FileOutputStream(it.fileDescriptor).use { stream ->
                    stream.write(content.toByteArray())
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }

    private suspend fun readFromFile(uri: Uri): String = withContext(Dispatchers.IO) {
        val stringBuilder = StringBuilder()
        contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    stringBuilder.append(line)
                    line = reader.readLine()
                }
            }
        }
        stringBuilder.toString()
    }
}
