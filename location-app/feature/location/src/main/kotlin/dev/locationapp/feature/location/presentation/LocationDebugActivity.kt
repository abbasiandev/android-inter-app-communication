package dev.locationapp.feature.location.presentation

import dagger.hilt.android.AndroidEntryPoint
import dev.abbasian.protocol.domain.analytics.IAnalyticsService
import dev.shared.debug.presentation.DebugActivity
import javax.inject.Inject

@AndroidEntryPoint
class LocationDebugActivity : DebugActivity() {
    @Inject
    lateinit var analytics: IAnalyticsService

    override fun getAnalyticsService(): IAnalyticsService = analytics

    override fun getAppName(): String = "Location App"
}
