package com.jksalcedo.passvault.utils

object PasswordGenerator {
    fun generate(length: Int = 16): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#\$%^&*()"
        return (1..length).map { chars.random() }.joinToString("")
    }
}
