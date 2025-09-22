package com.jksalcedo.passvault.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object Encryption {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "my_app_vault_key_v1" // change on key rotation
    private const val AES_MODE = "${KeyProperties.KEY_ALGORITHM_AES}/" +
            "${KeyProperties.BLOCK_MODE_GCM}/" +
            KeyProperties.ENCRYPTION_PADDING_NONE

    private val secureRandom = SecureRandom()

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
        ).run {
            setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            setRandomizedEncryptionRequired(true)
            // If you want to require device auth (PIN/biometric) for every use:
            if (requireUserAuth) {
                setUserAuthenticationRequired(true)
                // setUserAuthenticationValiditySeconds(5) // for short window (optional)
            }
            build()
        }

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
}
