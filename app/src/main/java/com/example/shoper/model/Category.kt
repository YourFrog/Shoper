package com.example.shoper.model

import com.example.shoper.R

data class Category (
    val id: Int?,
    val name: String,
    val description: String?,
    var products: List<Product> = emptyList()
) {
    data class Product (
        val name: String,
        val weight: Weight, // Gramatura
        val amount: String,     // Ilość
        var status: Status = Status.WAITING
    ) {
        enum class Weight(val displayName: Int, val displayShort: Int) {
            LITR(R.string.weight_display_name_litr, R.string.weight_display_short_litr),
            KILOGRAMS(R.string.weight_display_name_kilograms, R.string.weight_display_short_kilograms),
            GRAMS(R.string.weight_display_name_grams, R.string.weight_display_short_grams),
            PIECES(R.string.weight_display_name_piece, R.string.weight_display_short_piece)
        }

        enum class Status {
            BOUGHT, PART, WAITING, NO_FOUND
        }
    }
}