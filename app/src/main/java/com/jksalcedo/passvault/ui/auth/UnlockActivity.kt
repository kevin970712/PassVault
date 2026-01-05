package com.jksalcedo.passvault.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.biometric.BiometricManager
import com.jksalcedo.passvault.R
import com.jksalcedo.passvault.crypto.Encryption
import com.jksalcedo.passvault.databinding.ActivityUnlockBinding
import com.jksalcedo.passvault.repositories.PreferenceRepository
import com.jksalcedo.passvault.ui.base.BaseActivity
import com.jksalcedo.passvault.ui.main.MainActivity

/**
 * An activity for unlocking the app.
 */
class UnlockActivity : BaseActivity(), SetPinFragment.OnPinSetListener {

    lateinit var binding: ActivityUnlockBinding
    private val biometricAuthenticator = BiometricAuthenticator()
    private val prefsRepository by lazy {
        PreferenceRepository(
            this
        )
    }
    private var lockoutTimer: android.os.CountDownTimer? = null

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
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                },
                onFailure = { _, errString ->
                    Toast.makeText(this, errString, Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Check for lockout
        if (prefsRepository.isPinLockedOut()) {
            startLockoutTimer()
        } else {
            // Setup biometrics if available and a PIN exists
            setupBiometricIfAvailable(!initialCipher.isNullOrEmpty())
        }

        binding.btnUnlock.setOnClickListener {
            if (prefsRepository.isPinLockedOut()) return@setOnClickListener

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
                prefsRepository.setFailedPinAttempts(0)
                prefsRepository.setPinLockoutEndTime(0)
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                val newCount = prefsRepository.incrementFailedPinAttempts()
                val maxAttempts = prefsRepository.getMaxFailedAttempts()
                if (newCount >= maxAttempts) {
                    val lockoutDuration = prefsRepository.getLockoutDuration()
                    prefsRepository.setPinLockoutEndTime(System.currentTimeMillis() + lockoutDuration)
                    prefsRepository.setFailedPinAttempts(0) // Reset attempts for next cycle
                    startLockoutTimer()
                    Toast.makeText(
                        this,
                        "Too many failed attempts. App locked.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val remaining = maxAttempts - newCount
                    binding.mtPinMessage.text =
                        buildString {
                            append(getString(R.string.pin_incorrect))
                            append(". $remaining attempts remaining.")
                        }
                }
            }
        }
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
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
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

    private fun startLockoutTimer() {
        // Disable UI
        binding.etPin.isEnabled = false
        binding.btnUnlock.isEnabled = false
        binding.btnUseBiometric.visibility = View.GONE

        val remainingTime = prefsRepository.getRemainingLockoutTime()

        lockoutTimer?.cancel()
        lockoutTimer = object : android.os.CountDownTimer(remainingTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                binding.mtPinMessage.text = buildString {
                    append("Try again in ")
                    append(seconds)
                    append("s")
                }
            }

            override fun onFinish() {
                binding.etPin.isEnabled = true
                binding.btnUnlock.isEnabled = true
                binding.mtPinMessage.text = null
                binding.etPin.text = null

                // Re-enable biometrics if applicable
                val prefs = getSharedPreferences("auth", MODE_PRIVATE)
                val initialCipher = prefs.getString("pin_cipher", null)
                setupBiometricIfAvailable(!initialCipher.isNullOrEmpty())
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        lockoutTimer?.cancel()
    }
}

