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
     * Gets the current sort option for the password list.
     * @return The current sort option as a String.
     */
    fun getSortOption(): String {
        return prefs.getString("sort_option", "NAME_ASC") ?: "NAME_ASC"
    }
    
    fun setSortOption(sortOption: String) {
        prefs.edit { putString("sort_option", sortOption) }
    }

    /**
     * Sets whether screenshots should be blocked.
     * @param enabled True to block, false to allow.
     */
    fun setBlockScreenshots(enabled: Boolean) {
        prefs.edit { putBoolean("block_screenshots", enabled) }
    }

    /**
     * Gets whether screenshots should be blocked.
     * @return True if screenshots are blocked, false otherwise.
     */
    fun getBlockScreenshots(): Boolean {
        return prefs.getBoolean("block_screenshots", true)
    }

    /**
     * Sets the backup location URI string.
     * @param uri The URI string of the backup directory.
     */
    fun setBackupLocation(uri: String?) {
        prefs.edit { putString("backup_location", uri) }
    }

    /**
     * Gets the backup location URI string.
     * @return The URI string, or null if not set (default internal storage).
     */
    fun getBackupLocation(): String? {
        return prefs.getString("backup_location", null)
    }

    /**
     * Sets the max number of backups to keep.
     * @param count The number of backups. -1 for unlimited.
     */
    fun setBackupRetention(count: Int) {
        prefs.edit { putInt("backup_retention", count) }
    }

    /**
     * Gets the max number of backups to keep.
     * @return The number of backups. Default is 10.
     */
    fun getBackupRetention(): Int {
        // Retrieve as string first because ListPreference stores values as strings
        val value = prefs.getString("backup_retention", "10")
        return try {
            value?.toInt() ?: 10
        } catch (e: NumberFormatException) {
            10
        }
    }

    /**
     * Clears all preferences.
     */
    fun clear() {
        prefs.edit { clear().apply() }
    }
}