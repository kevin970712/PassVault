package com.jksalcedo.passvault.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.jksalcedo.passvault.data.PasswordEntry
import com.jksalcedo.passvault.data.SortOption
import com.jksalcedo.passvault.repositories.PasswordRepository
import com.jksalcedo.passvault.repositories.PreferenceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PasswordViewModel(app: Application) : AndroidViewModel(app) {
    val passwordRepository = PasswordRepository(app.applicationContext)
    val preferenceRepository = PreferenceRepository(app.applicationContext)
    val allEntries: LiveData<List<PasswordEntry>> = passwordRepository.getAll()

    private val _currentCategory = MutableLiveData<String?>(null)

    private val _currentSortOption = MutableLiveData<SortOption>()

    val filteredEntries: LiveData<List<PasswordEntry>> =
        _currentCategory.switchMap { category ->
            _currentSortOption.switchMap { sortOption ->
                val baseEntries = if (category.isNullOrEmpty()) {
                    allEntries
                } else {
                    passwordRepository.getEntriesByCategory(category)
                }

                baseEntries.map { list -> applySorting(list, sortOption) }
            }
        }

    init {
        val savedSort = preferenceRepository.getSortOption()
        _currentSortOption.value = SortOption.fromString(savedSort)
    }

    fun setSortOption(option: SortOption) {
        _currentSortOption.value = option
        preferenceRepository.setSortOption(option.name)
    }

    private fun applySorting(list: List<PasswordEntry>, option: SortOption): List<PasswordEntry> {
        return when (option) {
            SortOption.NAME_ASC -> list.sortedBy { it.title.lowercase() }
            SortOption.NAME_DESC -> list.sortedByDescending { it.title.lowercase() }
            SortOption.DATE_CREATED_DESC -> list.sortedByDescending { it.createdAt }
            SortOption.DATE_CREATED_ASC -> list.sortedBy { it.createdAt }
            SortOption.DATE_MODIFIED_DESC -> list.sortedByDescending { it.updatedAt }
            SortOption.DATE_MODIFIED_ASC -> list.sortedBy { it.updatedAt }
            SortOption.CATEGORY_ASC -> list.sortedBy { it.category ?: "" }
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
