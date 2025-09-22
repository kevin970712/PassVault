package com.jksalcedo.passvault.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.jksalcedo.passvault.data.AppDatabase
import com.jksalcedo.passvault.data.PasswordEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PasswordViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.Companion.getDatabase(app).passwordDao()
    val allEntries: LiveData<List<PasswordEntry>> = dao.getAll()

    fun insert(entry: PasswordEntry) {
        viewModelScope.launch(Dispatchers.IO) { dao.insert(entry) }
    }

    fun update(entry: PasswordEntry) {
        viewModelScope.launch(Dispatchers.IO) { dao.update(entry) }
    }

    fun delete(entry: PasswordEntry) {
        viewModelScope.launch(Dispatchers.IO) { dao.delete(entry) }
    }
}