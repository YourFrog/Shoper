package com.example.shoper.repository

import androidx.room.*
import com.example.shoper.entity.AbstractEntity
import io.reactivex.Observable
import io.reactivex.Single

/**
 *  Abstrakcyjna klasa wspierająca pracę przy DAO
 */
@Dao
interface AbstractDao<T: AbstractEntity>
{
    /* Zapisanie encji w bazie */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(item: T): Single<Long>

    /* Zapisanie encji w bazie */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertAll(vararg items: T)

    /**
     *  Zaaktualizowanie encji w bazie
     */
    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(item: T): Single<Int>

    @Delete
    fun remove(item: T): Single<Int>

    @Delete
    fun remove(item: List<T>): Single<Int>
}

/**
 *  Podpięcie do wszystkoch DAO możliwości wykonania merg'a
 */
fun <T : AbstractEntity> AbstractDao<T>.merge(item: T): Single<Long> {
    val currentID = item.getID()

    return if( currentID == null ) {
        insert(item)
    } else {
        update(item).map { it.toLong() }
    }
}