package com.jksalcedo.passvault.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.jksalcedo.passvault.data.AppDatabase
import com.jksalcedo.passvault.data.Category
import com.jksalcedo.passvault.data.CategoryRepository
import kotlinx.coroutines.launch

class CategoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CategoryRepository
    val allCategories: LiveData<List<Category>>

    init {
        val categoryDao = AppDatabase.getDatabase(application).categoryDao()
        repository = CategoryRepository(categoryDao)
        allCategories = repository.allCategories

        // Initialize default categories on first run
        viewModelScope.launch {
            repository.initializeDefaultCategories()
        }
    }

    suspend fun getAllCategoriesSync(): List<Category> {
        return repository.getAllCategoriesSync()
    }

    suspend fun getCategoryByName(name: String): Category? {
        return repository.getCategoryByName(name)
    }

    fun insertCategory(category: Category) = viewModelScope.launch {
        repository.insert(category)
    }

    fun updateCategory(category: Category) = viewModelScope.launch {
        repository.update(category)
    }

    fun deleteCategory(category: Category) = viewModelScope.launch {
        repository.delete(category)
    }

    fun deleteCustomCategory(name: String) = viewModelScope.launch {
        repository.deleteCustomCategory(name)
    }
}
