package com.jksalcedo.passvault.repositories

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Repository for managing app preferences.
 * @param context The application context.
 */
class PreferenceRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    /**
     * Sets the export format.
     * @param format The export format to set.
     */
    fun setExportFormat(format: String) {
        prefs.edit { putString("export_format", format) }
    }

    /**
     * Gets the export format.
     * @return The export format.
     */
    fun getExportFormat(): String {
        return prefs.getString("export_format", "json") ?: "json"
    }

    /**
     * Sets whether auto backups are enabled.
     * @param enabled True to enable, false to disable.
     */
    fun setAutoBackups(enabled: Boolean) {
        prefs.edit { putBoolean("auto_backups", enabled) }
    }

    /**
     * Gets whether auto backups are enabled.
     * @return True if enabled, false otherwise.
     */
    fun getAutoBackups(): Boolean {
        return prefs.getBoolean("auto_backups", false)
    }

    /**
     * Updates the last backup time to the current time.
     */
    fun updateLastBackupTime() {
        prefs.edit { putLong("last_backup_time", System.currentTimeMillis()) }
    }

    /**
     * Gets the last backup time.
     * @return The last backup time in milliseconds.
     */
    fun getLastBackupTime(): Long {
        return prefs.getLong("last_backup_time", 0L)
    }

    /**
     * Sets whether authentication is required for export.
     * @param enabled True to enable, false to disable.
     */
    fun setRequireAuthForExport(enabled: Boolean) {
        prefs.edit { putBoolean("require_auth_export", enabled) }
    }

    /**
     * Gets whether authentication is required for export.
     * @return True if enabled, false otherwise.
     */
    fun getRequireAuthForExport(): Boolean {
        return prefs.getBoolean("require_auth_export", true)
    }

    /**
     * Sets whether backups should be encrypted.
     * @param enabled True to enable, false to disable.
     */
    fun setEncryptBackups(enabled: Boolean) {
        prefs.edit { putBoolean("encrypt_backups", enabled) }
    }

    /**
     * Gets whether backups should be encrypted.
     * @return True if enabled, false otherwise.
     */
    fun getEncryptBackups(): Boolean {
        return prefs.getBoolean("encrypt_backups", true)
    }

    /**
     * Gets the password for auto backups.
     * @return The password, or null if not set.
     */
    fun getPasswordForAutoBackups(): String? {
        return prefs.getString("auto_backup_password", null)
    }

    /**
     * Sets the password for auto backups.
     * @param password The password to set.
     */
    fun setPasswordForAutoBackups(password: String) {
        prefs.edit { putString("auto_backup_password", password) }
    }

    /**
     * Clears all preferences.
     */
    fun clear() {
        prefs.edit { clear().apply() }
    }
}