package com.example.shoper.utils

import java.math.BigDecimal
import java.math.RoundingMode

fun Double.round(decimals: Int = 2): Double {
    return BigDecimal(this).setScale(decimals, RoundingMode.HALF_EVEN).toDouble()
}