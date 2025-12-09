package com.jksalcedo.passvault.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jksalcedo.passvault.data.Category

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY isDefault DESC, name ASC")
    fun getAllCategories(): LiveData<List<Category>>

    @Query("SELECT * FROM categories ORDER BY isDefault DESC, name ASC")
    suspend fun getAllCategoriesSync(): List<Category>

    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    suspend fun getCategoryByName(name: String): Category?

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(category: Category): Long

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertAll(categories: List<Category>)

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)

    @Query("DELETE FROM categories WHERE isDefault = 0 AND name = :name")
    suspend fun deleteCustomCategory(name: String)

    @Query("SELECT COUNT(*) FROM categories WHERE isDefault = 1")
    suspend fun getDefaultCategoriesCount(): Int
}