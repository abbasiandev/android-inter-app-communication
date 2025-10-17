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
            Timber.Forest.plant(Timber.DebugTree())
        }

        Timber.Forest.i("LocationApp", "Application started")
    }
}
