package com.example.shoper.model

import com.example.shoper.R


enum class ProductWeightType(val displayName: Int) {
    LITR(R.string.weight_display_name_litr),
    KILOGRAMS(R.string.weight_display_name_kilograms),
    GRAMS(R.string.weight_display_name_grams),
    PIECES(R.string.weight_display_name_piece)
}