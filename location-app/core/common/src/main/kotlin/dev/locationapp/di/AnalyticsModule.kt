package dev.locationapp.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.abbasian.protocol.data.analytics.MockAnalyticsService
import dev.abbasian.protocol.domain.analytics.IAnalyticsService
import dev.abbasian.protocol.domain.logger.AppLogger
import dev.locationapp.analytics.LocationAppAnalytics
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AnalyticsModule {
    @Provides
    @Singleton
    fun provideAnalyticsService(logger: AppLogger): IAnalyticsService =
        MockAnalyticsService(
            logger = logger,
            enableLogging = true,
        )

    @Provides
    @Singleton
    fun provideLocationAppAnalytics(analyticsService: IAnalyticsService): LocationAppAnalytics = LocationAppAnalytics(analyticsService)
}
