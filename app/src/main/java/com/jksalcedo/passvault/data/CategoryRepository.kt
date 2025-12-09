package com.jksalcedo.passvault.data

import androidx.lifecycle.LiveData
import com.jksalcedo.passvault.dao.CategoryDao

class CategoryRepository(private val categoryDao: CategoryDao) {

    val allCategories: LiveData<List<Category>> = categoryDao.getAllCategories()

    suspend fun getAllCategoriesSync(): List<Category> {
        return categoryDao.getAllCategoriesSync()
    }

    suspend fun getCategoryByName(name: String): Category? {
        return categoryDao.getCategoryByName(name)
    }

    suspend fun insert(category: Category): Long {
        return categoryDao.insert(category)
    }

    suspend fun insertAll(categories: List<Category>) {
        categoryDao.insertAll(categories)
    }

    suspend fun update(category: Category) {
        categoryDao.update(category)
    }

    suspend fun delete(category: Category) {
        categoryDao.delete(category)
    }

    suspend fun deleteCustomCategory(name: String) {
        categoryDao.deleteCustomCategory(name)
    }

    suspend fun initializeDefaultCategories() {
        if (categoryDao.getDefaultCategoriesCount() == 0) {
            val defaultCategories = listOf(
                Category(name = "General", colorHex = "#9E9E9E", isDefault = true),
                Category(name = "Social", colorHex = "#2196F3", isDefault = true),
                Category(name = "Work", colorHex = "#FF9800", isDefault = true),
                Category(name = "Personal", colorHex = "#9C27B0", isDefault = true),
                Category(name = "Finance", colorHex = "#4CAF50", isDefault = true),
                Category(name = "Entertainment", colorHex = "#F44336", isDefault = true)
            )
            insertAll(defaultCategories)
        }
    }
}
