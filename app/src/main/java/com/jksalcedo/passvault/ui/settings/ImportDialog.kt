package com.jksalcedo.passvault.ui.settings

import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jksalcedo.passvault.databinding.DialogImportBinding
import com.jksalcedo.passvault.utils.Utility
import com.jksalcedo.passvault.viewmodel.SettingsModelFactory
import com.jksalcedo.passvault.viewmodel.SettingsViewModel
import kotlinx.serialization.ExperimentalSerializationApi

class ImportDialog : BottomSheetDialogFragment() {

    private var _binding: DialogImportBinding? = null
    private val binding get() = _binding!!
    private var settingsActivity: SettingsActivity? = null
    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsModelFactory(
            application = requireActivity().application,
            activity = requireActivity()
        )
    }

    private lateinit var type: ImportType
    private var _kdbxPassword: String? = null // Temporary storage for KDBX password

    companion object {
        const val TAG = "ImportDialog"
    }

    @OptIn(ExperimentalSerializationApi::class)
    private val openFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    if (type == ImportType.KEEPASS_KDBX && _kdbxPassword != null) {
                        settingsViewModel.startImport(uri, type, _kdbxPassword!!)
                        _kdbxPassword = null // Reset after use
                    } else {
                        settingsActivity?.ensurePasswordExists(true) { passwordFromActivity ->
                            settingsViewModel.startImport(
                                uri,
                                type,
                                passwordFromActivity
                            )
                        }
                    }
                }
            }
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SettingsActivity) {
            settingsActivity = context
        } else {
            throw IllegalStateException("The activity must be SettingsActivity.")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogImportBinding.inflate(layoutInflater)
        val dialog = BottomSheetDialog(this.requireContext())
        dialog.apply {
            setTitle("Import from file")
            setContentView(binding.root)
            setOnShowListener {
                // Set the default import type
                updatePasswordVisibility(binding.mrbBitwardenJson.id) // Initial visibility
                prepareImport(ImportType.BITWARDEN_JSON)
                val rg = binding.radioGroup
                rg.setOnCheckedChangeListener { _, checkedId ->
                    updatePasswordVisibility(checkedId) // Update visibility on change
                    when (checkedId) {
                        binding.mrbBitwardenJson.id -> prepareImport(ImportType.BITWARDEN_JSON)
                        binding.mrbKeepassCsv.id -> prepareImport(ImportType.KEEPASS_CSV)
                        binding.mrbKeepassKdbx.id -> prepareImport(ImportType.KEEPASS_KDBX)
                        binding.mrbPassvaultJson.id -> prepareImport(ImportType.PASSVAULT_JSON)
                    }
                }
            }
        }

        observeImportUiState()

        return dialog
    }

    private fun updatePasswordVisibility(checkedId: Int) {
        if (checkedId == binding.mrbKeepassKdbx.id) {
            binding.tilPassword.visibility = View.VISIBLE
        } else {
            binding.tilPassword.visibility = View.GONE
        }
    }

    private fun prepareImport(importType: ImportType) {
        binding.btProceed.setOnClickListener {
            type = importType

            // If KDBX, get password directly from input field
            if (type == ImportType.KEEPASS_KDBX) {
                _kdbxPassword = binding.etPassword.text.toString()
                if (_kdbxPassword.isNullOrBlank()) {
                    Utility.showToast(requireContext(), "KeePass KDBX files require a password.")
                    return@setOnClickListener
                }
                openFileForImport()
            } else {
                _kdbxPassword = null
                openFileForImport()
            }
        }
    }

    private fun openFileForImport() {
        val mimeType = when (type) {
            ImportType.BITWARDEN_JSON, ImportType.PASSVAULT_JSON -> "application/json"
            ImportType.KEEPASS_CSV -> "text/csv"
            ImportType.KEEPASS_KDBX -> "application/octet-stream" // Standard MIME type for arbitrary binary data
        }

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mimeType
        }
        openFileLauncher.launch(intent)
    }

    private fun observeImportUiState() {
        settingsViewModel.importUiState.observe(this) { state ->
            when (state) {
                is ImportUiState.Loading -> {
                    binding.progressImporter.visibility = View.VISIBLE
                    binding.btProceed.isEnabled = false
                    for (i in 0 until binding.radioGroup.childCount) {
                        binding.radioGroup.getChildAt(i).isEnabled = false
                    }
                }

                is ImportUiState.Success -> {
                    binding.progressImporter.visibility = View.GONE
                    Utility.showToast(
                        requireContext(),
                        "Successfully imported ${state.count} entries"
                    )
                    dismiss()
                }

                is ImportUiState.Error -> {
                    binding.progressImporter.visibility = View.GONE
                    binding.btProceed.isEnabled = true
                    for (i in 0 until binding.radioGroup.childCount) {
                        binding.radioGroup.getChildAt(i).isEnabled = true
                    }
                    Utility.showToast(requireContext(), "Error: ${state.exception.message}")
                }

                is ImportUiState.Idle -> {
                    // Do nothing
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}

enum class ImportType {
    BITWARDEN_JSON,
    KEEPASS_CSV,
    KEEPASS_KDBX,
    PASSVAULT_JSON
}