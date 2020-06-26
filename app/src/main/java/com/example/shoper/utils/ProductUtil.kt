package com.example.shoper.utils

import com.example.shoper.entity.Product
import com.example.shoper.model.Category
import com.example.shoper.model.ProductWeightType

/**
 *  Prawidłowe wyświetlenie ilości sztuk w zależności od typu
 */
fun Product.formatAmount(customAmount: Double? = null): String {
    val weightType = Category.Product.Weight.valueOf(this.weightType)
    val amountToFormat = customAmount ?: this.amount

    return weightType.format(amountToFormat)
}

/**
 *  Pobranie faktora dla przedmiotu
 */
fun Product.factor(): Double {
    val weightType = Category.Product.Weight.valueOf(this.weightType)
    return weightType.factor()
}

/**
 *  Pobranie faktora dla przedmiotu
 */
fun Category.Product.Weight.factor(): Double {

    return when(this) {
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

fun Category.Product.Weight.format(value: Double): String {
    return when(this) {
        Category.Product.Weight.LITR,
        Category.Product.Weight.PIECES,
        Category.Product.Weight.GRAMS -> value.toInt()
        Category.Product.Weight.KILOGRAMS -> {
            val correctFormat = value.toString().replace(",", ".")
            correctFormat.toDouble().round(2)
        }
    }.toString()
}