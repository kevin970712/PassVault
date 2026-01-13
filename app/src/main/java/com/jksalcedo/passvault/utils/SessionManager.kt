package com.jksalcedo.passvault.utils

object SessionManager {
    var isUnlocked: Boolean = false

    fun setUnlocked() {
        isUnlocked = true
    }
}