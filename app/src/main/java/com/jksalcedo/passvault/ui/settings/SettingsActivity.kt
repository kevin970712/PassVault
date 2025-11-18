package com.jksalcedo.passvault.ui.settings

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.jksalcedo.passvault.R
import com.jksalcedo.passvault.repositories.PreferenceRepository
import com.jksalcedo.passvault.utils.Utility
import com.jksalcedo.passvault.viewmodel.SettingsModelFactory
import com.jksalcedo.passvault.viewmodel.SettingsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsModelFactory(application = this.application, this)
    }

    private val preferenceRepository by lazy { PreferenceRepository(application) }

    private var password: String? = null

    // Launcher for creating (exporting) a file
    private val createFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    settingsViewModel.exportEntries(uri, password = password!!)
                    password = null
                }
            }
        }

    // Launcher for opening (importing) a file
    private val openFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    ensurePasswordExists(true) { settingsViewModel.importEntries(uri, password!!) }
                    password = null
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setSupportActionBar(findViewById<MaterialToolbar>(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        // Show Preference Fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.settings_fragment_container, SettingsFragment())
            .commit()

        //settingsViewModel = ViewModelProvider(this)[SettingsViewModel::class.java]

        observeViewModelResults()
    }

    private fun observeViewModelResults() {
        // Observe export result
        settingsViewModel.exportResult.observe(this) { result ->
            result.fold(
                onSuccess = {
                    Toast.makeText(this, "Export successful!", Toast.LENGTH_SHORT).show()
                },
                onFailure = { error ->
                    android.util.Log.e("ExportError", "Export failed unexpectedly", error)
                    Toast.makeText(
                        this,
                        "Export failed: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
        }

        settingsViewModel.importResult.observe(this) { result ->
            result.fold(
                onSuccess = { count ->
                    Toast.makeText(
                        this,
                        "$count entries imported successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onFailure = { error ->
                    Toast.makeText(
                        this,
                        "Import failed: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    error.printStackTrace() // For debugging
                }
            )
        }
    }

    private fun ensurePasswordExists(isImporting: Boolean, onPasswordReady: () -> Unit) {
        if (password == null) {
            // No password found
            val layoutResource =
                if (isImporting) R.layout.dialog_enter_password else R.layout.dialog_set_password
            val layout = layoutInflater.inflate(layoutResource, null)
            val title =
                if (isImporting) R.string.enter_backup_file_password else R.string.set_export_password
            val message =
                if (isImporting) "Enter the password to decrypt this backup file." else "Create a password to encrypt your backup."
            val dialog = MaterialAlertDialogBuilder(this)
                .setView(layout)
                .setCancelable(false)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create()

            dialog.setOnShowListener {
                val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                positiveButton.setOnClickListener {
                    val etPassword = layout.findViewById<TextInputEditText>(R.id.et_password)
                    val etConfirmPassword =
                        layout.findViewById(R.id.et_confirm_password) ?: TextInputEditText(this)
                    val newPassword = etPassword.text.toString()
                    val confirmPassword = etConfirmPassword.text ?: ""

                    when {
                        newPassword.isBlank() -> {
                            etPassword.error = "Password cannot be empty"
                        }

                        newPassword.isNotEmpty() && isImporting -> {
                            password = newPassword
                            dialog.dismiss()
                            onPasswordReady()
                        }

                        newPassword != confirmPassword.toString() -> {
                            etConfirmPassword.error = "Password do not match"
                        }

                        else -> {
                            password = newPassword
                            if (!isImporting) Utility.showToast(
                                this,
                                getString(R.string.password_saved)
                            )
                            dialog.dismiss()
                            onPasswordReady() // Proceed
                        }
                    }
                }
            }
            dialog.show()
        } else {
            // Password already exists, proceed
            onPasswordReady()
        }
    }

    fun createFileForExport() {
        ensurePasswordExists(false) {
            val formatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val exportFormat = preferenceRepository.getExportFormat()
            val fileName = "passvault_backup_${formatter.format(Date())}.$exportFormat"

            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
                putExtra(Intent.EXTRA_TITLE, fileName)
            }
            createFileLauncher.launch(intent)
        }
    }

    fun openFileForImport() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*" // Allow selection of any file type for import
        }
        openFileLauncher.launch(intent)

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}