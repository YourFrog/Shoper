package com.example.shoper.entity

/**
 *  Abstrakcyjna encja
 */
abstract class AbstractEntity {

    /**
     *  Pobranie głównego klucza
     */
    abstract fun getID(): Long?;
}