package dev.internetapp

import android.app.Application
import dev.internetapp.core.common.di.commonModule
import dev.internetapp.feature.commandsender.di.commandSenderModule
import dev.internetapp.feature.responsedisplay.presentation.di.responseDisplayModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber

class InternetApp : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Timber.i("InternetApp started")

        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.NONE)
            androidContext(this@InternetApp)
            modules(
                commonModule,
                commandSenderModule,
                responseDisplayModule,
            )
        }

        Timber.i("Koin initialized successfully")
    }
}
