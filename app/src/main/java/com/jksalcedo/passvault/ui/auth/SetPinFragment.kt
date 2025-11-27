package com.jksalcedo.passvault.ui.auth

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.jksalcedo.passvault.R
import com.jksalcedo.passvault.crypto.Encryption
import com.jksalcedo.passvault.databinding.FragmentSetPinBinding
import com.jksalcedo.passvault.utils.Utility

/**
 * A fragment for setting the master PIN.
 */
class SetPinFragment : Fragment() {

    /**
     * A listener for when the PIN is set.
     */
    interface OnPinSetListener {
        /**
         * Called when the PIN is set.
         * @param pin The PIN that was set.
         */
        fun onPinSet(pin: String)
    }

    private var listener: OnPinSetListener? = null
    private var _binding: FragmentSetPinBinding? = null
    private val binding get() = _binding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = when {
            parentFragment is OnPinSetListener -> parentFragment as OnPinSetListener
            context is OnPinSetListener -> context
            else -> null
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSetPinBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.apply {
            setTitle(R.string.set_master_pin)
        }

        binding.tvMessage.text =
            buildString {
                append("This PIN protects your app and all its data.\n\n")
                append("• Required every time you open the app\n")
                append("• Cannot be recovered if forgotten\n\n")
                append("Choose carefully.")
            }

        binding.btnSave.setOnClickListener {
            validateAndSavePin()
        }
    }

    /**
     * Validates and saves the PIN.
     */
    private fun validateAndSavePin() {
        try {
            Encryption.ensureKeyExists()

            val pin = binding.etNewPin.text.toString()
            val confirm = binding.etConfirmPin.text.toString()

            binding.til1.error = null
            binding.til2.error = null

            when {
                pin.isEmpty() -> binding.til1.error = "PIN cannot be empty!"
                pin.length != 4 -> binding.til1.error = "PIN must be exactly 4 digits!"
                !pin.all { it.isDigit() } -> binding.til1.error = "Only digits allowed!"
                pin != confirm -> binding.til2.error = "PINs do not match!"
                else -> {
                    val (cipher, iv) = Encryption.encrypt(pin)
                    requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE).edit {
                        putString("pin_cipher", cipher)
                        putString("pin_iv", iv)
                    }
                    Toast.makeText(requireContext(), "PIN set successfully!", Toast.LENGTH_SHORT)
                        .show()

                    // Hide keyboard
                    val imm =
                        requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.etConfirmPin.windowToken, 0)

                    listener?.onPinSet(pin)
                }
            }
        } catch (_: Exception) {
            Utility.showToast(requireContext(), "Error setting PIN")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}