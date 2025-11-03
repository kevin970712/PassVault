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
import com.jksalcedo.passvault.utils.Utility

class ViewEntryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewEntryBinding

    private var titleText: String? = null
    private var usernameText: String? = null
    private var passwordCipher: String? = null
    private var passwordIv: String? = null
    private var notesText: String? = null

    private var revealed = false
    private var plainPassword: String = ""

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Read extras
        titleText = intent.getStringExtra(EXTRA_TITLE)
        usernameText = intent.getStringExtra(EXTRA_USERNAME)
        passwordCipher = intent.getStringExtra(EXTRA_PASSWORD_CIPHER)
        passwordIv = intent.getStringExtra(EXTRA_PASSWORD_IV)
        notesText = intent.getStringExtra(EXTRA_NOTES)

        if (titleText.isNullOrEmpty() || passwordCipher.isNullOrEmpty()) {
            Toast.makeText(this, "Missing entry data", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Decrypt using Encryption
        plainPassword = try {
            Encryption.ensureKeyExists()
            val cipher = passwordCipher!!
            val iv = passwordIv
            if (iv.isNullOrEmpty()) {
                ""
            } else {
                Encryption.decrypt(cipher, iv)
            }
        } catch (_: Exception) {
            ""
        }

        // Bind data
        binding.tvTitle.text = titleText
        binding.tvUsername.text = usernameText.orEmpty()
        binding.tvPassword.text = MASKED_PASSWORD
        binding.tvNotes.text = notesText.orEmpty()

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

        binding.btnCopy.setOnClickListener {
            if (plainPassword.isNotEmpty()) {
                Utility.copyToClipboard(this, "password", plainPassword)
                Toast.makeText(this, "Password copied", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No password to copy", Toast.LENGTH_SHORT).show()
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