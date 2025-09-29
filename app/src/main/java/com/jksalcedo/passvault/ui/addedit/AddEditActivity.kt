package com.jksalcedo.passvault.ui.addedit

import android.os.Bundle
import android.text.Editable
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.jksalcedo.passvault.crypto.Encryption
import com.jksalcedo.passvault.data.PasswordEntry
import com.jksalcedo.passvault.databinding.ActivityAddEditBinding
import com.jksalcedo.passvault.utils.PasswordGenerator
import com.jksalcedo.passvault.viewmodel.PasswordViewModel

class AddEditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddEditBinding
    private lateinit var viewModel: PasswordViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[PasswordViewModel::class.java]

        binding.btnSave.setOnClickListener {
            val rawPassword = binding.etPassword.text.toString()

            try {
                // Ensure key exists and encrypt password
                Encryption.ensureKeyExists()
                val (cipherText, iv) = Encryption.encrypt(rawPassword)

                val entry = PasswordEntry(
                    title = binding.etTitle.text.toString(),
                    username = binding.etUsername.text.toString(),
                    passwordCipher = cipherText,
                    passwordIv = iv,
                    notes = binding.etNotes.text.toString(),
                    updatedAt = System.currentTimeMillis()
                )

                viewModel.insert(entry)
                finish()
            } catch (_: Exception) {
                Toast.makeText(this, "Encryption failed", Toast.LENGTH_SHORT).show()
            }
        }

        binding.cardGeneratePassword.setOnClickListener {
            val et = binding.etPassword
            et.text = Editable.Factory.getInstance().newEditable(PasswordGenerator.generate())
        }
    }
}
