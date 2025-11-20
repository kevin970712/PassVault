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
    val csv: Csv = Csv {
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
        val parsedRows = csv.decodeFromString<List<KeepassRecord>>(raw)

        // Get and decode the database file
        val db = context.contentResolver.openInputStream(filePath!!)?.use { inputStream ->
            KeePassDatabase.decode(inputStream, Credentials.from(password!!.toByteArray()))
        }

        return if (type == ImportType.KEEPASS_CSV) {
            parsedRows.mapNotNull { row ->
                val password = row.password.trim()
                if (password.isEmpty()) return@mapNotNull null

                val keepassRecord = KeepassRecord(
                    title = row.title.trim(),
                    username = row.username.trim(),
                    password = password.trim(),
                    notes = row.notes.trim(),
                    creationTime = row.creationTime,
                    lastModificationTime = row.lastModificationTime
                )

                ImportRecord(
                    keepassRecord.title,
                    username = keepassRecord.username,
                    password = keepassRecord.password,
                    notes = keepassRecord.notes,
                    createdAt = keepassRecord.creationTime.toEpochMillis(),
                    updatedAt = keepassRecord.lastModificationTime.toEpochMillis()
                )
            }
        } else {
            db?.content!!.group.entries.map { entry ->
                ImportRecord(
                    title = entry.fields.title!!.content,
                    username = entry.fields.userName!!.content,
                    password = entry.fields.password!!.content,
                    notes = entry.fields.notes!!.content,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

            }
        }
    }

    override fun mapToPasswordEntries(records: List<ImportRecord>) =
        records.map {
            it.toPasswordEntry()
        }
}
