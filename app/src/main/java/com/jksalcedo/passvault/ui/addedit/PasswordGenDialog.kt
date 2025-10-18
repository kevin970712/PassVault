package com.jksalcedo.passvault.ui.addedit

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.jksalcedo.passvault.databinding.DialogPasswordGenBinding
import com.jksalcedo.passvault.utils.PasswordGenerator

class PasswordGenDialog : DialogFragment() {

    private var length = 0
    private var hasUppercase: Boolean = false
    private var hasLowercase: Boolean = false
    private var hasNumber: Boolean = false
    private var hasSymbols: Boolean = false

    private var password: String = ""
    private lateinit var binding: DialogPasswordGenBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogPasswordGenBinding.inflate(layoutInflater)

        length = binding.sbLength.progress
        binding.tvLength.text = binding.sbLength.progress.toString()
        hasUppercase = binding.sbUppercase.isChecked
        hasLowercase = binding.sbLowercase.isChecked
        hasNumber = binding.sbNumbers.isChecked
        hasSymbols = binding.cbSymbols.isChecked
        binding.tvPassword.text = password

        password = PasswordGenerator.generate(
            length = length,
            hasUppercase = hasUppercase,
            hasLowercase = hasLowercase,
            hasNumber = hasNumber,
            hasSymbols = hasSymbols
        )
        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setPositiveButton("Generate") { _, _ ->
                val activity = AddEditActivity()
                activity.password = password
            }.setNegativeButton("Cancel", null)
            .create()

    }
}