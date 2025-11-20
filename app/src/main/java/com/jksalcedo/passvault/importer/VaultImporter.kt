package com.jksalcedo.passvault.importer

import com.jksalcedo.passvault.data.ImportRecord
import com.jksalcedo.passvault.data.PasswordEntry

interface VaultImporter {
    suspend fun parse(raw: String): List<ImportRecord>
    fun mapToPasswordEntries(records: List<ImportRecord>): List<PasswordEntry>
}