package dev.locationapp.feature.location.presentation

import android.os.Bundle
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import dev.abbasian.protocol.domain.analytics.IAnalyticsService
import dev.shared.debug.presentation.DebugActivity

class LocationDebugActivity : DebugActivity() {
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface LocationDebugActivityEntryPoint {
        fun analyticsService(): IAnalyticsService
    }

    private lateinit var analytics: IAnalyticsService

    override fun onCreate(savedInstanceState: Bundle?) {
        val entryPoint =
            EntryPointAccessors.fromApplication(
                applicationContext,
                LocationDebugActivityEntryPoint::class.java,
            )
        analytics = entryPoint.analyticsService()

        super.onCreate(savedInstanceState)
    }

    override fun getAnalyticsService(): IAnalyticsService = analytics

    override fun getAppName(): String = "Location App"
}
