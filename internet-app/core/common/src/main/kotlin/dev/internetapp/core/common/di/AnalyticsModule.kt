package dev.internetapp.core.common.di

import dev.abbasian.protocol.data.analytics.MockAnalyticsService
import dev.abbasian.protocol.domain.analytics.IAnalyticsService
import dev.internetapp.core.common.analytics.InternetAppAnalytics
import org.koin.dsl.module

val analyticsModule =
    module {
        single<IAnalyticsService> {
            MockAnalyticsService(
                logger = get(),
                enableLogging = true,
            )
        }

        single {
            InternetAppAnalytics(
                analyticsService = get(),
            )
        }
    }
