package com.jksalcedo.passvault.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorHex: String,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
