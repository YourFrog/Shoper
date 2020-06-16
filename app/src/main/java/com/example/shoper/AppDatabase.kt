package com.example.shoper

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.shoper.entity.Category
import com.example.shoper.entity.Product
import com.example.shoper.repository.CategoryDao
import com.example.shoper.repository.ProductDao

/**
 *  Klasa obsługująca bazę danych
 */
@Database(entities = [Category::class, Product::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao

    abstract fun productDao(): ProductDao

    companion object {
        lateinit var instance: AppDatabase

        /**
         *  Zwraca instancje obiektu obsługującego bazę danych
         */
        fun getInstance(applicationContext: Context): AppDatabase {

            if( !this::instance.isInitialized ) {
                instance = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "shoper")
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .fallbackToDestructiveMigration()
                    .build()
            }

            return instance
        }
    }
}