package com.jksalcedo.passvault.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.jksalcedo.passvault.data.PasswordEntry
import com.jksalcedo.passvault.repositories.PasswordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PasswordViewModel(app: Application) : AndroidViewModel(app) {
    val passwordRepository = PasswordRepository(app.applicationContext)
    val allEntries: LiveData<List<PasswordEntry>> = passwordRepository.getAll()

    private val _currentCategory = MutableLiveData<String?>(null)
    val filteredEntries: LiveData<List<PasswordEntry>> = _currentCategory.switchMap { category ->
        if (category.isNullOrEmpty() || category == "All") {
            allEntries
        } else {
            passwordRepository.getEntriesByCategory(category)
        }
    }

    fun filterByCategory(category: String?) {
        _currentCategory.value = category
    }

    fun getEntryById(id: Long): LiveData<PasswordEntry> {
        return passwordRepository.getEntryById(id)
    }

    fun insert(entry: PasswordEntry) {
        viewModelScope.launch(Dispatchers.IO) { passwordRepository.insert(entry) }
    }

    fun update(entry: PasswordEntry) {
        viewModelScope.launch(Dispatchers.IO) { passwordRepository.update(entry) }
    }

    fun delete(entry: PasswordEntry) {
        viewModelScope.launch(Dispatchers.IO) { passwordRepository.delete(entry) }
    }

    suspend fun search(query: String): List<PasswordEntry> {
        return passwordRepository.search(query)
    }
}
