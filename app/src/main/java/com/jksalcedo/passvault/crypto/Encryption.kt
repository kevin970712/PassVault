package com.jksalcedo.passvault.crypto

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object Encryption {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "passvault_key_v1"
    private const val AES_MODE = "${KeyProperties.KEY_ALGORITHM_AES}/" +
            "${KeyProperties.BLOCK_MODE_GCM}/" +
            KeyProperties.ENCRYPTION_PADDING_NONE

    // For File Encryption
    private const val FILE_ENCRYPTION_ALGORITHM = "AES/GCM/NoPadding"
    private const val KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val SALT_SIZE_BYTES = 16
    private const val ITERATION_COUNT = 65536 // Standard recommendation
    private const val KEY_LENGTH_BITS = 256
    private const val TAG_LENGTH_BITS = 128
    private const val IV_SIZE_BYTES = 12

    private fun getKeystore(): KeyStore {
        return KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
    }

    // Create key if absent
    fun ensureKeyExists(requireUserAuth: Boolean = false) {
        val ks = getKeystore()
        if (ks.containsAlias(KEY_ALIAS)) return

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )

        val builder = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).apply {
            setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            setRandomizedEncryptionRequired(true)
            if (requireUserAuth) {
                setUserAuthenticationRequired(true)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    setUserAuthenticationParameters(
                        0,
                        KeyProperties.AUTH_BIOMETRIC_STRONG or KeyProperties.AUTH_DEVICE_CREDENTIAL
                    )
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    setIsStrongBoxBacked(true)
                }
            }
        }.build()

        keyGenerator.init(builder)
        keyGenerator.generateKey()
    }

    private fun getSecretKey(): SecretKey {
        val ks = getKeystore()
        val entry = ks.getEntry(KEY_ALIAS, null)
        require(entry is KeyStore.SecretKeyEntry)
        return entry.secretKey
    }

    fun encrypt(plainText: String): Pair<String /*cipherBase64*/, String /*ivBase64*/> {
        val cipher = Cipher.getInstance(AES_MODE)
        val key = getSecretKey()
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv // 12 bytes recommended
        val ciphertext = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        val cipherB64 = Base64.encodeToString(ciphertext, Base64.NO_WRAP)
        val ivB64 = Base64.encodeToString(iv, Base64.NO_WRAP)
        return Pair(cipherB64, ivB64)
    }

    fun decrypt(cipherBase64: String, ivBase64: String): String {
        val cipher = Cipher.getInstance(AES_MODE)
        val key = getSecretKey()
        val iv = Base64.decode(ivBase64, Base64.NO_WRAP)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        val ciphertext = Base64.decode(cipherBase64, Base64.NO_WRAP)
        val plain = cipher.doFinal(ciphertext)
        return String(plain, Charsets.UTF_8)
    }

    fun encryptFileContent(plainText: String, password: String): String {
        // Generate a random salt
        val salt = ByteArray(SALT_SIZE_BYTES).apply { SecureRandom().nextBytes(this) }

        val keyFactory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM)
        val keySpec = PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH_BITS)
        val secretKey = SecretKeySpec(keyFactory.generateSecret(keySpec).encoded, "AES")

        // Encrypt the data
        val cipher = Cipher.getInstance(FILE_ENCRYPTION_ALGORITHM)
        val iv = ByteArray(IV_SIZE_BYTES).apply { SecureRandom().nextBytes(this) }
        val gcmSpec = GCMParameterSpec(TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)
        val ciphertext = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        // Combine salt, IV, and ciphertext into one array for easy storage
        val combined = salt + iv + ciphertext

        // Return as a single Base64 string
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    fun decryptFileContent(encryptedDataB64: String, password: String): String {
        val combined = Base64.decode(encryptedDataB64, Base64.NO_WRAP)
        val salt = combined.copyOfRange(0, SALT_SIZE_BYTES)
        val iv = combined.copyOfRange(SALT_SIZE_BYTES, SALT_SIZE_BYTES + IV_SIZE_BYTES)
        val ciphertext = combined.copyOfRange(SALT_SIZE_BYTES + IV_SIZE_BYTES, combined.size)

        val keyFactory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM)
        val keySpec = PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH_BITS)
        val secretKey = SecretKeySpec(keyFactory.generateSecret(keySpec).encoded, "AES")

        // Decrypt the data
        val cipher = Cipher.getInstance(FILE_ENCRYPTION_ALGORITHM)
        val gcmSpec = GCMParameterSpec(TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
        val plain = cipher.doFinal(ciphertext)

        // Return the decrypted string
        return String(plain, Charsets.UTF_8)
    }
}
