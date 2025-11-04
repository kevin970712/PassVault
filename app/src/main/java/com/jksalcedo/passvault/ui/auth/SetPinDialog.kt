package com.jksalcedo.passvault.ui.auth

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.fragment.app.DialogFragment
import com.jksalcedo.passvault.crypto.Encryption
import com.jksalcedo.passvault.databinding.DialogSetPinBinding
import com.jksalcedo.passvault.utils.Utility

/**
 * DialogFragment to set and store a master PIN.
 */
class SetPinDialog : DialogFragment() {

    interface OnPinSetListener {
        fun onPinSet(pin: String)
    }

    private var listener: OnPinSetListener? = null
    private var _binding: DialogSetPinBinding? = null
    private val binding get() = _binding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = when {
            parentFragment is OnPinSetListener -> parentFragment as OnPinSetListener
            context is OnPinSetListener -> context
            else -> null
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateDialog(
        savedInstanceState: Bundle?
    ): Dialog {
        _binding = DialogSetPinBinding.inflate(layoutInflater)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setCancelable(false)
            .setPositiveButton("Save", null)
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                try {
                    Encryption.ensureKeyExists()

                    val etPin = binding.etNewPin
                    val etConfirm = binding.etConfirmPin
                    val pin = etPin.text.toString()
                    val confirm = etConfirm.text.toString()

                    if (pin.length != 4) {
                        etPin.error = "PIN must be 4 digits!"
                        return@setOnClickListener
                    }

                    if (pin != confirm) {
                        etConfirm.error = "PINs do not match!"
                        return@setOnClickListener
                    }

                    val (cipher, iv) = Encryption.encrypt(pin)
                    val prefs = requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE)
                    prefs.edit {
                        putString("pin_cipher", cipher)
                        putString("pin_iv", iv)
                    }

                    Toast.makeText(requireContext(), "PIN set successfully", Toast.LENGTH_SHORT)
                        .show()
                    listener?.onPinSet(pin)
                    dismiss() // dismiss the dialog
                } catch (_: Exception) {
                    Utility.showToast(requireContext(), "Error setting PIN")
                }
            }
        }

        return dialog
    }

    override fun onDetach() {
        super.onDetach()
        _binding = null
    }
}
