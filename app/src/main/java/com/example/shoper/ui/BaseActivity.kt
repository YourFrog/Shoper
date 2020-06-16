package com.example.shoper.ui

import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable

/**
 *  Abstrakcyjna klasa wspierająca pracę
 */
abstract class BaseActivity : AppCompatActivity() {

    protected val disposable = CompositeDisposable()

    override fun onDestroy() {
        super.onDestroy()

        disposable.dispose()
    }

    fun handleError(ex: Throwable) {
        val i = 0
    }
}