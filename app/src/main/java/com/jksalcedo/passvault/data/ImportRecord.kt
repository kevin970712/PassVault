package com.jksalcedo.passvault.data

data class ImportRecord(
    val title: String,
    val username: String?,
    val password: String,
    val notes: String?,
    val createdAt: Long?,
    val updatedAt: Long?
)