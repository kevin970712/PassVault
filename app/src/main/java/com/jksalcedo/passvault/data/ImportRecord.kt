package com.jksalcedo.passvault.data

import kotlinx.serialization.Serializable

@Serializable
data class ImportRecord(
    val title: String,
    val username: String?,
    val password: String,
    val notes: String?,
    val createdAt: Long?,
    val updatedAt: Long?
)