package com.example.shoper.utils

fun Double.round(decimals: Int = 2): Double = "%.${decimals}f".format(this).toDouble()