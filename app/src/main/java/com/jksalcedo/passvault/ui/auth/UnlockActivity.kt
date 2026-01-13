package com.jksalcedo.passvault.ui.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import com.jksalcedo.passvault.R
import com.jksalcedo.passvault.crypto.Encryption
import com.jksalcedo.passvault.databinding.ActivityUnlockBinding
import com.jksalcedo.passvault.ui.main.MainActivity
import com.jksalcedo.passvault.utils.SessionManager

/**
 * An activity for unlocking the app.
 */
class UnlockActivity : AppCompatActivity(), SetPinFragment.OnPinSetListener {

    lateinit var binding: ActivityUnlockBinding
    private val biometricAuthenticator = BiometricAuthenticator()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUnlockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ensure encryption key exists early in the session
        try {
            Encryption.ensureKeyExists()
        } catch (_: Exception) {
        }

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)

        val initialCipher = prefs.getString("pin_cipher", null)
        val initialIv = prefs.getString("pin_iv", null)

        if (initialCipher.isNullOrEmpty() || initialIv.isNullOrBlank()) {
            val setPinFragment = SetPinFragment()
            binding.clMain.visibility = View.GONE
            binding.fragmentContainer.visibility = View.VISIBLE
            this.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, setPinFragment)
                .commitNow()
        } else {
            binding.clMain.visibility = View.VISIBLE
            binding.fragmentContainer.visibility = View.GONE
            biometricAuthenticator.showBiometricPrompt(
                activity = this,
                onSuccess = {
                    navigateToNextScreen()
                },
                onFailure = { _, errString ->
                    Toast.makeText(this, errString, Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Setup biometrics if available and a PIN exists
        setupBiometricIfAvailable(!initialCipher.isNullOrEmpty())

        binding.btnUnlock.setOnClickListener {
            // Get the input
            val input = binding.etPin.text?.toString()?.trim().orEmpty()

            val cipher = prefs.getString("pin_cipher", null)
            val iv = prefs.getString("pin_iv", null)

            // Decrypt the stored cipher and iv
            val storedPin = try {
                if (cipher != null && iv != null) {
                    Encryption.decrypt(cipherBase64 = cipher, iv)
                } else {
                    "" // No PIN is set
                }
            } catch (_: Exception) {
                "" // Decryption failed
            }

            // Compare the decrypted pin to the input pin
            if (input.isNotEmpty() && storedPin == input) {
                navigateToNextScreen()
            } else {
                Toast.makeText(this, getString(R.string.pin_incorrect), Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("UnsafeIntentLaunch")
    private fun navigateToNextScreen() {
        SessionManager.setUnlocked()

        // Check if there is a pending intent passed from BaseActivity
        val redirectIntent =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra("EXTRA_REDIRECT_INTENT", Intent::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra("EXTRA_REDIRECT_INTENT")
            }

        if (redirectIntent != null) {
            // Shortcut intent/task
            redirectIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(redirectIntent)
        } else {
            // No pending task,
            val homeIntent = Intent(this, MainActivity::class.java)
            homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(homeIntent)
        }

        finish()
    }

    /**
     * Sets up the biometric authentication if it is available.
     * @param hasPin True if a PIN has been set, false otherwise.
     */
    private fun setupBiometricIfAvailable(hasPin: Boolean) {
        if (!hasPin) {
            binding.btnUseBiometric.visibility = View.GONE
            return
        }
        val biometricManager = BiometricManager.from(this)
        val authenticators =
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
        val canAuth = biometricManager.canAuthenticate(authenticators)
        if (canAuth == BiometricManager.BIOMETRIC_SUCCESS) {
            binding.btnUseBiometric.visibility = View.VISIBLE
            binding.btnUseBiometric.setOnClickListener {
                biometricAuthenticator.showBiometricPrompt(
                    activity = this,
                    onSuccess = {
                        navigateToNextScreen()
                    },
                    onFailure = { _, errString ->
                        Toast.makeText(this, errString, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        } else {
            binding.btnUseBiometric.visibility = View.GONE
        }
    }

    override fun onRestart() {
        super.onRestart()
        binding.clMain.visibility = View.VISIBLE
        binding.fragmentContainer.visibility = View.GONE
    }

    override fun onPinSet(pin: String) {
        // After setting PIN, re-evaluate biometric availability
        supportFragmentManager.popBackStack()
        binding.clMain.visibility = View.VISIBLE
        binding.fragmentContainer.visibility = View.GONE
        setupBiometricIfAvailable(true)
    }
}

