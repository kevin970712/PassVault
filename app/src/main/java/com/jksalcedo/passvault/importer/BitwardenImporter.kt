package com.jksalcedo.passvault.importer

import com.jksalcedo.passvault.data.BitwardenExport
import com.jksalcedo.passvault.data.ImportRecord
import com.jksalcedo.passvault.utils.Utility.toEpochMillis
import com.jksalcedo.passvault.utils.Utility.toPasswordEntry
import kotlinx.serialization.json.Json


class BitwardenImporter(
    private val json: Json = Json { ignoreUnknownKeys = true }
) : VaultImporter {

    override suspend fun parse(raw: String): List<ImportRecord> {
        val export = json.decodeFromString<BitwardenExport>(raw)
        return export.items
            .filter { it.type == 1 && it.login?.password?.isNotBlank() == true }
            .map { item ->
                ImportRecord(
                    title = item.name,
                    username = item.login?.username,
                    password = item.login?.password.orEmpty(),
                    notes = item.notes,
                    createdAt = item.creationDate?.toEpochMillis(),
                    updatedAt = item.revisionDate?.toEpochMillis()
                )
            }
    }

    override fun mapToPasswordEntries(records: List<ImportRecord>) =
        records.map { it.toPasswordEntry() }
}