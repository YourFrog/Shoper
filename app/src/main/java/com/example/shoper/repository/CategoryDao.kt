package com.example.shoper.repository

import androidx.room.*
import com.example.shoper.entity.Category
import com.example.shoper.model.ShopList
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface CategoryDao: AbstractDao<Category> {
    @Transaction
    @Query("SELECT * FROM category")
    fun all(): Single<List<ShopList>>

    @Transaction
    @Query("SELECT * FROM category WHERE id_category = :categoryID")
    fun one(categoryID: Long): Single<ShopList>
}