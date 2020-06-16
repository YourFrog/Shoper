package com.example.shoper.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.shoper.entity.Category
import com.example.shoper.entity.Product

data class ShopList(
    @Embedded
    val category: Category,

    @Relation(
        parentColumn="id_category",
        entityColumn="id_category"
    )
    val products: List<Product>
)