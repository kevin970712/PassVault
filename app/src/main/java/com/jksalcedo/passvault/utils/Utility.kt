package com.jksalcedo.passvault.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import com.jksalcedo.passvault.data.PasswordEntry
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object Utility {
    fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
    }

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun serializeEntries(list: List<PasswordEntry>): String {
        return Json.encodeToString(list)
    }

    fun deserializeEntries(serializedString: String): List<PasswordEntry> {
        return Json.decodeFromString(serializedString)
    }
}
