package com.jksalcedo.passvault.importer

import android.content.Context
import android.net.Uri
import app.keemobile.kotpass.database.Credentials
import app.keemobile.kotpass.database.KeePassDatabase
import app.keemobile.kotpass.database.decode
import com.jksalcedo.passvault.data.ImportRecord
import com.jksalcedo.passvault.data.KeepassRecord
import com.jksalcedo.passvault.ui.settings.ImportType
import com.jksalcedo.passvault.utils.Utility.toEpochMillis
import com.jksalcedo.passvault.utils.Utility.toPasswordEntry
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.csv.Csv
import kotlinx.serialization.decodeFromString

@OptIn(ExperimentalSerializationApi::class)
class KeePassImporter(
    private val csv: Csv = Csv {
        hasHeaderRecord = true
        ignoreUnknownColumns = true
        ignoreEmptyLines = true
    },
    private val filePath: Uri? = null,
    private val password: String? = null,
    private val type: ImportType? = null,
    private val context: Context
) : VaultImporter {

    override suspend fun parse(raw: String): List<ImportRecord> {
        return when (type) {
            ImportType.KEEPASS_CSV -> parseCsv(raw)
            ImportType.KEEPASS_KDBX -> parseKdbx()
            else -> emptyList()
        }
    }

    private fun parseCsv(raw: String): List<ImportRecord> {
        return try {
            val parsedRows = csv.decodeFromString<List<KeepassRecord>>(raw)
            parsedRows.mapNotNull { row ->
                val password = row.password.trim()
                if (password.isEmpty()) return@mapNotNull null

                val keepassRecord = KeepassRecord(
                    title = row.title.trim(),
                    username = row.username.trim(),
                    password = password,
                    notes = row.notes.trim(),
                    creationTime = row.creationTime,
                    lastModificationTime = row.lastModificationTime
                )

                ImportRecord(
                    title = keepassRecord.title,
                    username = keepassRecord.username,
                    password = keepassRecord.password,
                    notes = keepassRecord.notes,
                    createdAt = keepassRecord.creationTime.toEpochMillis(),
                    updatedAt = keepassRecord.lastModificationTime.toEpochMillis()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    private fun parseKdbx(): List<ImportRecord> {
        if (filePath == null || password == null) {
            return emptyList()
        }
        return try {
            val db = context.contentResolver.openInputStream(filePath)?.use { inputStream ->
                val credentials = Credentials.from(password.toByteArray())
                KeePassDatabase.decode(inputStream, credentials)
            }
            db?.content?.group?.entries?.mapNotNull { entry ->
                val title = entry.fields.title?.content
                val username = entry.fields.userName?.content
                val password = entry.fields.password?.content
                val notes = entry.fields.notes?.content

                if (title.isNullOrBlank() || password.isNullOrEmpty()) {
                    return@mapNotNull null
                }

                ImportRecord(
                    title = title,
                    username = username,
                    password = password,
                    notes = notes,
                    createdAt = entry.times?.creationTime?.toEpochMilli(),
                    updatedAt = entry.times?.lastModificationTime?.toEpochMilli()
                )
            } ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override fun mapToPasswordEntries(records: List<ImportRecord>) =
        records.map {
            it.toPasswordEntry()
        }
}
