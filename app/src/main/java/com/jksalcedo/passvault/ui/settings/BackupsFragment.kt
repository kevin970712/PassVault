package com.jksalcedo.passvault.ui.settings

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jksalcedo.passvault.R
import com.jksalcedo.passvault.adapter.BackupAdapter
import com.jksalcedo.passvault.databinding.FragmentBackupsBinding
import com.jksalcedo.passvault.utils.Utility
import com.jksalcedo.passvault.viewmodel.SettingsModelFactory
import com.jksalcedo.passvault.viewmodel.SettingsViewModel
import java.io.File

/**
 * A fragment for managing backups.
 */
class BackupsFragment : Fragment() {

    private var _binding: FragmentBackupsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: BackupAdapter
    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsModelFactory(
            application = requireActivity().application
        )
    }
    private lateinit var backupFile: File

    private val openFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    try {
                        settingsViewModel.copyBackupToUri(backupFile, uri)
                        Utility.showToast(requireContext(), "Backup file copied successfully!.")
                    } catch (_: Exception) {
                        Utility.showToast(requireContext(), "Process failed.")
                    }
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBackupsBinding.inflate(layoutInflater)

        adapter = BackupAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this.requireContext())
        binding.recyclerView.adapter = adapter

        (requireActivity() as AppCompatActivity).supportActionBar?.setTitle(R.string.manage_backups)

        // get backups
        val backups = settingsViewModel.getInternalBackups()
        adapter.setBackups(backups.sortedByDescending { it.lastModified() })

        adapter.backupItems.observe(this.requireActivity()) { backupItem ->
            binding.tvNoBackup.visibility = if (backupItem.isEmpty()) View.VISIBLE else View.GONE
        }

        adapter.onItemClick = { backupItem ->
            backupFile = backupItem
            var selectedPosition = -1
            MaterialAlertDialogBuilder(this.requireContext())
                .setTitle(backupItem.nameWithoutExtension)
                .setSingleChoiceItems(R.array.backup_options, 0) { _, which ->
                    selectedPosition = which
                }
                .setPositiveButton("Proceed") { _, _ ->
                    when (selectedPosition) {
                        // Restore
                        0 -> {
                            //settingsViewModel.restoreBackup(backupItem)
                        }
                        // Save & Copy
                        1 -> {
                            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                            openFileLauncher.launch(intent)
                        }
                        // Delete
                        2 -> {
                            try {
                                settingsViewModel.deleteBackup(backupItem)
                                Utility.showToast(requireContext(), "Backup file deleted.")
                            } catch (e: Exception) {
                                Utility.showToast(
                                    requireContext(),
                                    "Error deleting backup file: $e."
                                )
                            }
                            // Refresh the list after deleting
                            adapter.setBackups(
                                settingsViewModel.getInternalBackups()
                                    .sortedByDescending { it.lastModified() }
                            )
                        }
                        // Share
                        3 -> {
                            try {
                                val backupFile =
                                    File(
                                        requireActivity().application.getExternalFilesDir(null),
                                        "backups/" + backupItem.name
                                    )
                                val authority = "${requireContext().packageName}.provider"
                                val contentUri = FileProvider.getUriForFile(
                                    requireContext(),
                                    authority,
                                    backupFile
                                )

                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    putExtra(Intent.EXTRA_STREAM, contentUri)
                                    type = "*/*"
                                }
                                startActivity(intent)
                            } catch (e: Exception) {
                                Utility.showToast(requireContext(), "Sharing failed: $e")
                                Log.e(this.toString(), e.toString())
                            }
                        }
                    }
                }
                .show()
        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        (requireActivity() as AppCompatActivity).supportActionBar?.setTitle(R.string.action_settings)
    }
}