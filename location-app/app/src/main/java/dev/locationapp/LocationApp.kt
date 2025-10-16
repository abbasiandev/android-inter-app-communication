package dev.locationapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import net.sqlcipher.BuildConfig
import timber.log.Timber

@HiltAndroidApp
class LocationApp : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Timber.i("LocationApp", "Application started")
    }
}