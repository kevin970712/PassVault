package com.jksalcedo.passvault.ui.base

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.jksalcedo.passvault.repositories.PreferenceRepository

/**
 * Base activity that applies common security settings like screenshot blocking.
 * All activities in the app should extend this class.
 */
abstract class BaseActivity : AppCompatActivity() {

    private val preferenceRepository by lazy { PreferenceRepository(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()
        applySecuritySettings()
    }

    private fun applyTheme() {
        val theme = preferenceRepository.getTheme()
        val mode = when (theme) {
            "light" -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
            "dark" -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
            else -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(mode)
    }

    /**
     * Applies security settings like screenshot blocking based on user preferences.
     */
    private fun applySecuritySettings() {
        if (preferenceRepository.getBlockScreenshots()) {
            // Block screenshots and screen recording
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        } else {
            // Allow screenshots
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}
