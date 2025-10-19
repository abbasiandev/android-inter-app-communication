package dev.internetapp.feature.responsedisplay.presentation

import dev.abbasian.protocol.domain.analytics.IAnalyticsService
import dev.shared.debug.presentation.DebugActivity
import org.koin.android.ext.android.inject

class InternetDebugActivity : DebugActivity() {
    private val analytics: IAnalyticsService by inject()

    override fun getAnalyticsService(): IAnalyticsService = analytics

    override fun getAppName(): String = "Internet App"
}
