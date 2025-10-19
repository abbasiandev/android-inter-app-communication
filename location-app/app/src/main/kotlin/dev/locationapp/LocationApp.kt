package dev.locationapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import dev.locationapp.analytics.LocationAppAnalytics
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class LocationApp : Application() {
    @Inject
    lateinit var analytics: LocationAppAnalytics

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        analytics.trackAppLaunched()

        Timber.d("LocationApp initialized")
    }
}
