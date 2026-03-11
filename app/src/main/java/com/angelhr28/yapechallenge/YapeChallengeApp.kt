package com.angelhr28.yapechallenge

import android.app.Application
import com.angelhr28.yapechallenge.di.allModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class YapeChallengeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@YapeChallengeApp)
            modules(allModules)
        }
    }
}
