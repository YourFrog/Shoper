package com.example.shoper.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName="category")
data class Category (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="id_category")
    val id: Long? = null,

    @ColumnInfo(name="name")
    val name: String,

    @ColumnInfo(name="description")
    val description: String?

) : AbstractEntity() {
    override fun getID(): Long? {
        return id
    }
}