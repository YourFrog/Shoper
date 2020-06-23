package com.example.shoper.model

import com.example.shoper.R


enum class ProductWeightType(val value: Int, val displayName: Int) {
    LITR(0, R.string.weight_display_name_litr),
    KILOGRAMS(1, R.string.weight_display_name_kilograms),
    GRAMS(2, R.string.weight_display_name_grams),
    PIECES(3, R.string.weight_display_name_piece)
}