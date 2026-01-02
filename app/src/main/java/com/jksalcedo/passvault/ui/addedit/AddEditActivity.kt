package com.jksalcedo.passvault.ui.addedit

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.jksalcedo.passvault.R
import com.jksalcedo.passvault.crypto.Encryption
import com.jksalcedo.passvault.data.PasswordEntry
import com.jksalcedo.passvault.databinding.ActivityAddEditBinding
import com.jksalcedo.passvault.ui.base.BaseActivity
import com.jksalcedo.passvault.utils.PasswordStrengthAnalyzer
import com.jksalcedo.passvault.viewmodel.CategoryViewModel
import com.jksalcedo.passvault.viewmodel.PasswordViewModel

/**
 * An activity for adding and editing password entries.
 */
class AddEditActivity : BaseActivity(), PasswordDialogListener {
    private lateinit var binding: ActivityAddEditBinding
    private lateinit var viewModel: PasswordViewModel
    private lateinit var categoryViewModel: CategoryViewModel
    private var currentEntry: PasswordEntry? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel = ViewModelProvider(this)[PasswordViewModel::class.java]
        categoryViewModel = ViewModelProvider(this)[CategoryViewModel::class.java]

        // Load categories for dropdown
        categoryViewModel.allCategories.observe(this) { categories ->
            val categoryNames = categories.mapNotNull { it.name.takeIf(String::isNotBlank) }
            val adapter =
                ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categoryNames)
            binding.etCategory.setAdapter(adapter)

            // Set default category for new entries
            if (currentEntry == null && binding.etCategory.text.isNullOrEmpty()) {
                binding.etCategory.setText(getString(R.string.general), false)
            }
        }

        val entryFromIntent: PasswordEntry? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.setExtrasClassLoader(PasswordEntry::class.java.classLoader)
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

        // Password strength analyzer
        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updatePasswordStrength(s?.toString() ?: "")
            }
        })
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
            Encryption.ensureKeyExists()
            val decryptedPassword = Encryption.decrypt(entry.passwordCipher, entry.passwordIv)
            binding.etPassword.setText(decryptedPassword)
        } catch (_: Exception) {
            binding.etPassword.setText("")
            Toast.makeText(this, "Failed to decrypt password", Toast.LENGTH_SHORT).show()
        }

        binding.etCategory.setText(entry.category ?: getString(R.string.general), false)
        binding.etEmail.setText(entry.email)
        binding.etUrl.setText(entry.url)
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

        if (email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Invalid email format"
            return
        }

        if (url.isNotEmpty() && !android.util.Patterns.WEB_URL.matcher(url).matches()) {
            binding.tilUrl.error = "Invalid URL format"
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
                category = category.ifEmpty { getString(R.string.general) },
                updatedAt = System.currentTimeMillis()
            ) ?: PasswordEntry(
                title = title,
                username = username,
                passwordCipher = cipherText,
                passwordIv = iv,
                notes = notes,
                email = email,
                url = url,
                category = category.ifEmpty { getString(R.string.general) },
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

    /**
     * Updates the password strength indicator based on the current password.
     */
    private fun updatePasswordStrength(password: String) {
        if (password.isEmpty()) {
            binding.layoutPasswordStrength.visibility = View.GONE
            return
        }

        binding.layoutPasswordStrength.visibility = View.VISIBLE
        val result = PasswordStrengthAnalyzer.analyze(password)

        // Update progress bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.progressPasswordStrength.setProgress(result.score, true)
        } else {
            binding.progressPasswordStrength.progress = result.score
        }

        // Update color based on strength level
        val colorResId = when (result.level) {
            PasswordStrengthAnalyzer.StrengthLevel.VERY_WEAK -> R.color.strength_very_weak
            PasswordStrengthAnalyzer.StrengthLevel.WEAK -> R.color.strength_weak
            PasswordStrengthAnalyzer.StrengthLevel.FAIR -> R.color.strength_fair
            PasswordStrengthAnalyzer.StrengthLevel.GOOD -> R.color.strength_good
            PasswordStrengthAnalyzer.StrengthLevel.STRONG -> R.color.strength_strong
        }
        val color = ContextCompat.getColor(this, colorResId)
        binding.progressPasswordStrength.setIndicatorColor(color)

        // Update label
        val strengthLabel = PasswordStrengthAnalyzer.getStrengthLabel(result.level)
        binding.tvPasswordStrength.text = buildString {
            append("Password Strength: ")
            append(strengthLabel)
        }
        binding.tvPasswordStrength.setTextColor(color)

        // Update feedback
        binding.tvStrengthFeedback.text = result.feedback.firstOrNull() ?: ""
        binding.tvStrengthFeedback.setTextColor(color)
    }

    override fun onPasswordGenerated(password: String) {
        binding.etPassword.text =
            Editable.Factory.getInstance().newEditable((password.ifEmpty { "" }))
        updatePasswordStrength(password)
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
