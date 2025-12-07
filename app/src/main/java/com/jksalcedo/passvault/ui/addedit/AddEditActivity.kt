package com.jksalcedo.passvault.ui.addedit

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable

import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.jksalcedo.passvault.R
import com.jksalcedo.passvault.crypto.Encryption
import com.jksalcedo.passvault.data.PasswordEntry
import com.jksalcedo.passvault.databinding.ActivityAddEditBinding
import com.jksalcedo.passvault.viewmodel.PasswordViewModel

/**
 * An activity for adding and editing password entries.
 */
class AddEditActivity : AppCompatActivity(), PasswordDialogListener {
    private lateinit var binding: ActivityAddEditBinding
    private lateinit var viewModel: PasswordViewModel
    private var currentEntry: PasswordEntry? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel = ViewModelProvider(this)[PasswordViewModel::class.java]

        val entryFromIntent: PasswordEntry? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(EXTRA_ENTRY, PasswordEntry::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(EXTRA_ENTRY)
            }

        if (entryFromIntent != null) {
            // If the full object is passed, use it directly
            currentEntry = entryFromIntent
            populateUi(entryFromIntent)
            binding.toolbar.title = "Edit Password"
        } else if (intent.hasExtra(EXTRA_ID)) {
            // Fallback for when only the ID is provided.
            val id = intent.getLongExtra(EXTRA_ID, -1)
            viewModel.getEntryById(id).observe(this) { entry ->
                entry?.let {
                    currentEntry = it
                    populateUi(it)
                }
            }
            binding.toolbar.title = "Edit Password"
        }

        binding.btnSave.setOnClickListener {
            saveEntry()
        }

        // Password generator button
        binding.btnGeneratePassword.setOnClickListener {
            PasswordGenDialog().apply {
                isCancelable = false
                show(supportFragmentManager, "PasswordGenDialog")
            }
        }
    }

    /**
     * Populates the UI with the given password entry.
     * @param entry The password entry to populate the UI with.
     */
    private fun populateUi(entry: PasswordEntry) {
        binding.etTitle.setText(entry.title)
        binding.etUsername.setText(entry.username)
        binding.etNotes.setText(entry.notes)

        // Decrypt and set password
        try {
            val decryptedPassword = Encryption.decrypt(entry.passwordCipher, entry.passwordIv)
            binding.etPassword.setText(decryptedPassword)
        } catch (_: Exception) {
            binding.etPassword.setText("")
            Toast.makeText(this, "Failed to decrypt password", Toast.LENGTH_SHORT).show()
        }

        val categories = resources.getStringArray(R.array.category_options)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)

        binding.etCategory.setAdapter(adapter)

        binding.etCategory.setText(entry.category ?: "General", false)
    }

    /**
     * Saves the current password entry.
     */
    private fun saveEntry() {
        // Clear previous errors
        binding.tilTitle.error = null
        binding.tilUsername.error = null
        binding.tilPassword.error = null

        val title = binding.etTitle.text.toString()
        val username = binding.etUsername.text.toString()
        val rawPassword = binding.etPassword.text.toString()
        val notes = binding.etNotes.text.toString()
        val category = binding.etCategory.text.toString()
        val email = binding.etEmail.text.toString()
        val url = binding.etUrl.text.toString()

        if (title.isEmpty()) {
            binding.tilTitle.error = "Title cannot be empty!"
            return
        }
        if (rawPassword.isEmpty()) {
            binding.tilPassword.error = "Password cannot be empty!"
            return
        }
        if (username.isEmpty()) {
            binding.tilUsername.error = "Username cannot be empty!"
            return
        }

        try {
            Encryption.ensureKeyExists()
            val (cipherText, iv) = Encryption.encrypt(rawPassword)

            val entry = currentEntry?.copy(
                title = title,
                username = username,
                passwordCipher = cipherText,
                passwordIv = iv,
                notes = notes,
                email = email,
                url = url,
                category = category,
                updatedAt = System.currentTimeMillis()
            ) ?: PasswordEntry(
                title = title,
                username = username,
                passwordCipher = cipherText,
                passwordIv = iv,
                notes = notes,
                email = email,
                url = url,
                category = category,
                updatedAt = System.currentTimeMillis()
            )

            if (currentEntry == null) {
                viewModel.insert(entry)
            } else {
                viewModel.update(entry)
            }
            finish()
        } catch (_: Exception) {
            Toast.makeText(this, "Encryption failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPasswordGenerated(password: String) {
        binding.etPassword.text =
            Editable.Factory.getInstance().newEditable((password.ifEmpty { "" }))
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    companion object {
        const val EXTRA_ID = "extra_id"
        const val EXTRA_ENTRY = "extra_entry"

        /**
         * Creates an intent to start [AddEditActivity].
         * @param context The context.
         * @param entry The password entry to edit.
         * @return An intent to start [AddEditActivity].
         */
        fun createIntent(context: Context, entry: PasswordEntry? = null): Intent {
            return Intent(context, AddEditActivity::class.java).apply {
                entry?.let {
                    putExtra(EXTRA_ID, it.id)
                    putExtra(EXTRA_ENTRY, it)
                }
            }
        }
    }
}
