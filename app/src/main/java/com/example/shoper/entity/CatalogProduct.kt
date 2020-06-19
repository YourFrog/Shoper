package com.example.shoper.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="catalog_product")
data class CatalogProduct (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="id_catalog_product")
    val id: Long? = null,

    @ColumnInfo(name="name")
    val name: String,

    @ColumnInfo(name="ean")
    val ean: String?,

    @ColumnInfo(name="weight_type")
    var weightType: String
) : AbstractEntity() {
    override fun getID(): Long? {
        return id
    }
}