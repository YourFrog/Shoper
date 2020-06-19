package com.example.shoper

import android.app.Application
import com.example.shoper.module.repositoryModule
import org.koin.core.context.startKoin

/**
 *  Customowa klasa aplikacji
 */
class AppApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            modules(listOf(
                repositoryModule
            ))
        }
    }
}