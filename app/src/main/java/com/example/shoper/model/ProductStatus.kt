package com.example.shoper.model


enum class ProductStatus(val value: Int, order: Int) {
    BOUGHT(0, 10),
    WAITING(2, 0),
    NO_FOUND(3, 20)
}