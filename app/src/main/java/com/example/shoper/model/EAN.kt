package com.example.shoper.model

import java.text.DecimalFormat

/**
 *  Reprezentacja kodu EAN
 */
class EAN (
    val value: String
) {

    /**
     *  Formatowanie kodu EAN
     */
    fun format(): String {
        return value[0] + " " + value.substring(1, 7) + " " + value.substring(8)
    }
}