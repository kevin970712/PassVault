package com.jksalcedo.passvault.importer

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class BitwardenImporterTest {

    @Test
    fun `parse function with valid Bitwarden JSON`() = runBlocking {
        val json = """
            {
              "encrypted": false,
              "folders": [],
              "items": [
                {
                  "id": "1",
                  "type": 1,
                  "name": "GitHub",
                  "notes": "Some notes",
                  "favorite": false,
                  "login": {
                    "username": "alice",
                    "password": "secret123"
                  },
                  "collectionIds": []
                }
              ]
            }
        """.trimIndent()

        val importer = BitwardenImporter()
        val records = importer.parse(json)

        assertEquals(1, records.size)
        assertEquals("GitHub", records[0].title)
        assertEquals("alice", records[0].username)
        assertEquals("secret123", records[0].password)
    }

    @Test
    fun `parse function with empty password but valid title`() = runBlocking {
        val json = """
            {
              "encrypted": false,
              "folders": [],
              "items": [
                {
                  "id": "1",
                  "type": 1,
                  "name": "Secure Note",
                  "notes": "This is a note without a password",
                  "favorite": false,
                  "login": {
                    "username": "bob",
                    "password": ""
                  },
                  "collectionIds": []
                }
              ]
            }
        """.trimIndent()

        val importer = BitwardenImporter()
        val records = importer.parse(json)

        assertEquals(1, records.size)
        assertEquals("Secure Note", records[0].title)
        assertEquals("bob", records[0].username)
        assertEquals("", records[0].password)
        assertEquals("This is a note without a password", records[0].notes)
    }

    @Test
    fun `parse function with missing password field`() = runBlocking {
        val json = """
            {
              "encrypted": false,
              "folders": [],
              "items": [
                {
                  "id": "1",
                  "type": 1,
                  "name": "Missing Password Field",
                  "notes": null,
                  "favorite": false,
                  "login": {
                    "username": "charlie"
                  },
                  "collectionIds": []
                }
              ]
            }
        """.trimIndent()

        val importer = BitwardenImporter()
        val records = importer.parse(json)

        assertEquals(1, records.size)
        assertEquals("Missing Password Field", records[0].title)
        assertEquals("charlie", records[0].username)
        assertEquals("", records[0].password)
    }
}
