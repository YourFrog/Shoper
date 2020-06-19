package com.example.shoper

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.shoper.entity.CatalogProduct
import com.example.shoper.entity.Category
import com.example.shoper.entity.Product
import com.example.shoper.repository.CatalogProductDao
import com.example.shoper.repository.CategoryDao
import com.example.shoper.repository.ProductDao

/**
 *  Klasa obsługująca bazę danych
 */
@Database(entities = [Category::class, Product::class, CatalogProduct::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao

    abstract fun productDao(): ProductDao

    abstract fun catalogProductDao(): CatalogProductDao

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