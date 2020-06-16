package com.example.shoper.repository

import androidx.room.*
import com.example.shoper.entity.Category
import com.example.shoper.entity.Product
import io.reactivex.Single

@Dao
interface ProductDao : AbstractDao<Product> {
    @Transaction
    @Query("SELECT * FROM product")
    fun all(): Single<List<Product>>
}