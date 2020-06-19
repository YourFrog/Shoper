package com.example.shoper.repository

import androidx.room.*
import com.example.shoper.entity.CatalogProduct
import com.example.shoper.entity.Category
import com.example.shoper.model.ShopList
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface CatalogProductDao: AbstractDao<CatalogProduct> {
    @Transaction
    @Query("SELECT * FROM catalog_product WHERE ean = :ean")
    fun oneByEan(ean: String): Maybe<CatalogProduct>
}