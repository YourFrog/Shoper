package com.example.shoper.repository

import com.example.shoper.model.Category

/**
 *  Repozytorium obsługujące kategorie zadań
 */
class CategoryRepository
{
    private val categories: ArrayList<Category> by lazy {
        arrayListOf(
            Category(id = 1, name = "Test 1", description = "Opis 1"),
            Category(id = 2, name = "Test 2", description = null),
            Category(id = 3, name = "Test 3", description = "Opis 3"),
            Category(id = 4, name = "Test 4", description = "Opis 4"),
            Category(id = 5, name = "Test 5", description = "Opis 5", products = arrayListOf(
                Category.Product(name = "Mleko 2%", amount = "4", weight = Category.Product.Weight.LITR),
                Category.Product(name = "Ziemniaki", amount = "2", weight = Category.Product.Weight.KILOGRAMS),
                Category.Product(name = "Masło", amount = "6", weight = Category.Product.Weight.PIECES),


                Category.Product(name = "Ser", amount = "4", weight = Category.Product.Weight.PIECES, status = Category.Product.Status.NO_FOUND),
                Category.Product(name = "Lody", amount = "1", weight = Category.Product.Weight.PIECES, status = Category.Product.Status.NO_FOUND),

                Category.Product(name = "Wędlina", amount = "1", weight = Category.Product.Weight.PIECES, status = Category.Product.Status.BOUGHT),
                Category.Product(name = "Chleb", amount = "1", weight = Category.Product.Weight.PIECES, status = Category.Product.Status.BOUGHT),

                Category.Product(name = "śmietana 18%", amount = "3", weight = Category.Product.Weight.PIECES, status = Category.Product.Status.PART)
            ))
        )
    }

    /**
     *  Usunięcie kategorii z listy
     */
    fun remove(category: Category) {
        categories.remove(category)
    }

    /**
     *  Pobranie wszystkich kategorii
     */
    fun list(): List<Category> {

        return categories
    }
}