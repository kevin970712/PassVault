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

/**
 * Imports passwords from KeePass CSV or KDBX files.
 *
 * @param csv The Csv instance to use for parsing.
 * @param filePath The path to the KDBX file.
 * @param password The password for the KDBX file.
 * @param type The type of import.
 * @param context The context to use for content resolving.
 */
@OptIn
    (ExperimentalSerializationApi::class)
class KeePassImporter(
    private val csv: Csv = Csv {
        hasHeaderRecord = true
        ignoreUnknownColumns = true
        ignoreEmptyLines = true
    },
    private val filePath: Uri? = null,
    private val password: String? = null,
    private val type: ImportType = ImportType.KEEPASS_CSV,
    private val context: Context? = null
) : VaultImporter {

    /**
     * Parses the raw input string.
     *
     * @param raw The raw input string to parse.
     * @return A list of [ImportRecord].
     * @throws [Exception] if parsing fails.
     */
    override suspend fun parse(raw: String): List<ImportRecord> {
        return when (type) {
            ImportType.KEEPASS_CSV -> parseCsv(raw)
            ImportType.KEEPASS_KDBX -> parseKdbx()
            else -> emptyList()
        }
    }

    /**
     * Parses a CSV string.
     *
     * @param raw The CSV string to parse.
     * @return A list of [ImportRecord].
     * @throws [Exception] if parsing fails.
     */
    private fun parseCsv(raw: String): List<ImportRecord> {
        return try {
            val parsedRows = csv.decodeFromString<List<KeepassRecord>>(raw)
            parsedRows.mapNotNull { row ->
                val password = row.password.trim()
                if (password.isEmpty() && row.title.isBlank()) return@mapNotNull null

                val keepassRecord = KeepassRecord(
                    title = row.title.trim(),
                    username = row.username.trim(),
                    password = password,
                    url = row.url,
                    notes = row.notes.trim(),
                    creationTime = row.creationTime,
                    lastModificationTime = row.lastModificationTime
                )

                ImportRecord(
                    title = keepassRecord.title,
                    username = keepassRecord.username,
                    password = keepassRecord.password,
                    url = keepassRecord.url,
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

    /**
     * Parses a KDBX file.
     *
     * @return A list of [ImportRecord].
     * @throws [IllegalStateException] if context is null.
     * @throws [Exception] if parsing fails.
     */
    private fun parseKdbx(): List<ImportRecord> {
        if (filePath == null || password == null) {
            return emptyList()
        }
        val resolverContext = context
            ?: throw IllegalStateException("Context is required to import KeePass KDBX files")
        return try {
            val db = resolverContext.contentResolver.openInputStream(filePath)?.use { inputStream ->
                val credentials = Credentials.from(password.toByteArray())
                KeePassDatabase.decode(inputStream, credentials)
            }
            db?.content?.group?.entries?.mapNotNull { entry ->
                val title = entry.fields.title?.content
                val username = entry.fields.userName?.content
                val password = entry.fields.password?.content
                val url = entry.fields.url?.content
                val notes = entry.fields.notes?.content

                // Skip entries that have both empty title AND empty password
                if (title.isNullOrBlank() && password.isNullOrEmpty()) {
                    return@mapNotNull null
                }

                ImportRecord(
                    title = title.orEmpty(),
                    username = username,
                    password = password.orEmpty(),
                    url = url,
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

    /**
     * Maps a list of [ImportRecord] to a list of PasswordEntry.
     *
     * @param records The list of [ImportRecord] to map.
     * @return A list of PasswordEntry.
     */
    override fun mapToPasswordEntries(records: List<ImportRecord>) =
        records.map {
            it.toPasswordEntry()
        }
}
