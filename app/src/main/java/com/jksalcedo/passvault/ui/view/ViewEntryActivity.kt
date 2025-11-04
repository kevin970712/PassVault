package com.jksalcedo.passvault.ui.view

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.jksalcedo.passvault.R
import com.jksalcedo.passvault.crypto.Encryption
import com.jksalcedo.passvault.data.PasswordEntry
import com.jksalcedo.passvault.databinding.ActivityViewEntryBinding
import com.jksalcedo.passvault.ui.addedit.AddEditActivity
import com.jksalcedo.passvault.utils.Utility

class ViewEntryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewEntryBinding
    private var currentEntry: PasswordEntry? = null

    private var revealed = false
    private var plainPassword: String = ""

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val entryId = intent.getLongExtra(EXTRA_ID, -1)
        val title = intent.getStringExtra(EXTRA_TITLE)
        val username = intent.getStringExtra(EXTRA_USERNAME)
        val passwordCipher = intent.getStringExtra(EXTRA_PASSWORD_CIPHER)
        val passwordIv = intent.getStringExtra(EXTRA_PASSWORD_IV)
        val notes = intent.getStringExtra(EXTRA_NOTES)

        if (title.isNullOrEmpty() || passwordCipher.isNullOrEmpty() || passwordIv.isNullOrEmpty()) {
            Toast.makeText(this, "Missing entry data", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        currentEntry = PasswordEntry(
            id = entryId,
            title = title,
            username = username,
            passwordCipher = passwordCipher,
            passwordIv = passwordIv,
            notes = notes
        )

        // Decrypt using Encryption
        plainPassword = try {
            Encryption.ensureKeyExists()
            Encryption.decrypt(passwordCipher, passwordIv)
        } catch (_: Exception) {
            ""
        }

        // Bind data
        binding.tvTitle.text = title
        binding.tvUsername.text = username.orEmpty()
        binding.tvPassword.text = MASKED_PASSWORD
        binding.tvNotes.text = notes.orEmpty()

        binding.btnReveal.setOnClickListener {
            revealed = !revealed
            if (revealed) {
                binding.tvPassword.text = plainPassword
                binding.btnReveal.text = getString(R.string.hide)
            } else {
                binding.tvPassword.text = MASKED_PASSWORD
                binding.btnReveal.text = getString(R.string.reveal)
            }
        }

        binding.btnCopyPassword.setOnClickListener {
            if (plainPassword.isNotEmpty()) {
                Utility.copyToClipboard(this, "password", plainPassword)
                Toast.makeText(this, "Password copied", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No password to copy", Toast.LENGTH_SHORT).show()
            }
        }

        binding.fabEdit.setOnClickListener {
            currentEntry?.let {
                startActivity(AddEditActivity.createIntent(this, it))
                finish()
            }
        }
    }

    companion object {
        const val EXTRA_ID = "extra_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_USERNAME = "extra_username"
        const val EXTRA_PASSWORD_CIPHER = "extra_password_cipher"
        const val EXTRA_PASSWORD_IV = "extra_password_iv"
        const val EXTRA_NOTES = "extra_notes"

        private const val MASKED_PASSWORD = "••••••••"

        fun createIntent(context: Context, entry: PasswordEntry): Intent {
            return Intent(context, ViewEntryActivity::class.java).apply {
                putExtra(EXTRA_ID, entry.id)
                putExtra(EXTRA_TITLE, entry.title)
                putExtra(EXTRA_USERNAME, entry.username)
                putExtra(EXTRA_PASSWORD_CIPHER, entry.passwordCipher)
                putExtra(EXTRA_PASSWORD_IV, entry.passwordIv)
                putExtra(EXTRA_NOTES, entry.notes)
            }
        }
    }
}
