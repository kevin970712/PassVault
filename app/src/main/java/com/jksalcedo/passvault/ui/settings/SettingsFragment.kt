package com.jksalcedo.passvault.ui.settings

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.os.Build
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.jksalcedo.passvault.R
import com.jksalcedo.passvault.utils.Utility

class SettingsFragment : PreferenceFragmentCompat() {

    private var settingsActivity: SettingsActivity? = null
    private lateinit var prefs: SharedPreferences

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is SettingsActivity) {
            settingsActivity = context
        } else {
            throw IllegalStateException("The  activity must be a SettingsActivity.")
        }
    }

    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?
    ) {
        setPreferencesFromResource(R.xml.preference, rootKey)

        prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)

        // General
        setupAboutPreference()

        // Backup & Restore
        findPreference<Preference>("export_data")?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener { _ ->
                settingsActivity?.createFileForExport()
                true
            }
        findPreference<Preference>("import_data")?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener { _ ->
                settingsActivity?.openFileForImport()
                true
            }

        findPreference<ListPreference>("export_format")?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener { _ ->
                Utility.showToast(requireContext(), "Not implemented yet")
                true
            }
        findPreference<SwitchPreferenceCompat>("auto_backups")?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, _ ->
                Utility.showToast(requireContext(), "Not implemented yet")
                true
            }
        findPreference<Preference>("last_backup")?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener { _ ->
                Utility.showToast(requireContext(), "Not implemented yet")
                true
            }

        // Data Management
        findPreference<Preference>("storage_info")?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener { _ ->
                Utility.showToast(requireContext(), "Not implemented yet")
                true
            }
        findPreference<Preference>("clear_data")?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener { _ ->
                Utility.showToast(requireContext(), "Not implemented yet")
                true
            }

        // Security
        findPreference<SwitchPreferenceCompat>("require_auth_export")?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener { _ ->
                Utility.showToast(requireContext(), "Not implemented yet")
                true
            }

        findPreference<SwitchPreferenceCompat>("encrypt_backups")?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                //Log.d("Prefs", encryptChecked.toString())
                prefs.edit().putBoolean("encrypt_backups", newValue as Boolean).apply()
                true
            }

    }

    private fun setupAboutPreference() {
        findPreference<Preference>("about_app")?.summary = try {
            val packageName = requireContext().packageName
            val packageInfo: PackageInfo =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requireContext().packageManager.getPackageInfo(packageName, 0)
                } else {
                    @Suppress("DEPRECATION")
                    requireContext().packageManager.getPackageInfo(packageName, 0)
                }
            // versionName from PackageInfo
            "PassVault ${packageInfo.versionName ?: "Unknown"}"
        } catch (_: Exception) {
            "N/A"
        }
    }

    override fun onDetach() {
        super.onDetach()
        settingsActivity = null
    }
}