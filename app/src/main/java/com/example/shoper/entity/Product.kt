package com.example.shoper.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.shoper.model.ProductStatus

@Entity(tableName="product")
data class Product(
    @PrimaryKey(autoGenerate=true)
    @ColumnInfo(name="id_product")
    val id: Long? = null,

    @ColumnInfo(name="name")
    var name: String,

    @ColumnInfo(name="amount")
    var amount: Double,

    @ColumnInfo(name="weight_type")
    var weightType: String,

    @ColumnInfo(name="status")
    var status: String = ProductStatus.WAITING.toString(),

    @ColumnInfo(name="ean")
    var ean: String? = null,

    @ColumnInfo(name="id_category")
    var categoryID: Long? = null
) : AbstractEntity() {
    override fun getID(): Long? {
        return id
    }
}