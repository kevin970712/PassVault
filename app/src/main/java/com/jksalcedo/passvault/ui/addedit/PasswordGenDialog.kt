package com.jksalcedo.passvault.ui.addedit

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.jksalcedo.passvault.databinding.DialogPasswordGenBinding

class PasswordGenDialog : DialogFragment() {

    private var length = 0
    private var hasUppercase: Boolean = false
    private var hasLowercase: Boolean = false
    private var hasNumber: Boolean = false
    private var hasSymbols: Boolean = false
    private lateinit var binding: DialogPasswordGenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogPasswordGenBinding.inflate(layoutInflater)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // TODO("Implement views and logic")

        return super.onCreateView(inflater, container, savedInstanceState)
    }

}