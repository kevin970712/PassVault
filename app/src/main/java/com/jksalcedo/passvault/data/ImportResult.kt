package com.jksalcedo.passvault.data

data class ImportResult(
    val title: String,
    val isSuccess: Boolean,
    val errorMessage: String? = null
)
