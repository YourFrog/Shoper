package com.example.shoper.utils

import com.example.shoper.entity.Product
import com.example.shoper.model.Category
import com.example.shoper.model.ProductWeightType

/**
 *  Prawidłowe wyświetlenie ilości sztuk w zależności od typu
 */
fun Product.formatAmount(customAmount: Double? = null): String {
    val weightType = ProductWeightType.valueOf(this.weightType)
    val amountToFormat = customAmount ?: this.amount

    return when(weightType) {
        ProductWeightType.LITR,
        ProductWeightType.PIECES,
        ProductWeightType.GRAMS -> amountToFormat.toInt()
        ProductWeightType.KILOGRAMS -> amountToFormat.round(2)
    }.toString()
}

/**
 *  Pobranie faktora dla przedmiotu
 */
fun Product.factor(): Double {
    val weightType = Category.Product.Weight.valueOf(this.weightType)

    return when(weightType) {
        Category.Product.Weight.LITR,
        Category.Product.Weight.PIECES -> {
            1.toDouble()
        }
        Category.Product.Weight.GRAMS -> {
            10.toDouble()
        }
        else -> {
            0.1
        }
    }
}