package com.jksalcedo.passvault.data

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.jksalcedo.passvault.dao.CategoryDao
import com.jksalcedo.passvault.dao.PasswordDao

@Database(
    entities = [PasswordEntry::class, Category::class],
    version = 3,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun passwordDao(): PasswordDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "passvault_db"
                ).build().also { INSTANCE = it }
            }
        }

        @VisibleForTesting
        fun initializeForTesting(instance: AppDatabase) {
            INSTANCE = instance
        }
    }
}
