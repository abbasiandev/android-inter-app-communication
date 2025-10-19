package dev.locationapp.analytics

import dev.abbasian.protocol.analytics.AnalyticsEvent
import dev.abbasian.protocol.analytics.PerformanceMetric
import dev.abbasian.protocol.domain.analytics.IAnalyticsService
import dev.abbasian.protocol.domain.model.LocationData

/**
 * Location App specific analytics wrapper
 */
class LocationAppAnalytics(
    private val analyticsService: IAnalyticsService,
) {
    private var serviceStartTime: Long = 0
    private var lastLocationTime: Long = 0

    fun trackAppLaunched() {
        analyticsService.trackEvent(
            AnalyticsEvent.AppLaunched("LocationApp"),
        )
        analyticsService.setUserProperty("app_name", "LocationApp")
        analyticsService.setUserProperty("app_type", "provider")
    }

    fun trackServiceStarted() {
        serviceStartTime = System.currentTimeMillis()
        analyticsService.trackEvent(
            AnalyticsEvent.ServiceStarted("LocationCollectionService"),
        )
    }

    fun trackServiceStopped() {
        val duration = System.currentTimeMillis() - serviceStartTime
        analyticsService.trackEvent(
            AnalyticsEvent.ServiceStopped(
                serviceType = "LocationCollectionService",
                durationMs = duration,
            ),
        )
        serviceStartTime = 0
    }

    fun trackLocationCollected(location: LocationData) {
        val currentTime = System.currentTimeMillis()
        val timeSinceLast =
            if (lastLocationTime > 0) {
                currentTime - lastLocationTime
            } else {
                0L
            }
        lastLocationTime = currentTime

        analyticsService.trackEvent(
            AnalyticsEvent.LocationCollected(
                accuracy = location.accuracy,
                provider = location.provider,
                timeSinceLastMs = timeSinceLast,
            ),
        )

        analyticsService.trackMetric(
            PerformanceMetric.LocationAccuracy(
                accuracyMeters = location.accuracy,
                provider = location.provider,
            ),
        )
    }

    fun trackLocationSaved(
        success: Boolean,
        durationMs: Long,
    ) {
        analyticsService.trackEvent(
            AnalyticsEvent.LocationSaved(
                success = success,
                durationMs = durationMs,
            ),
        )

        if (success) {
            analyticsService.trackMetric(
                PerformanceMetric.ResponseTime(
                    operation = "location_save",
                    durationMs = durationMs,
                ),
            )
        }
    }

    fun trackDatabaseOperation(
        operation: String,
        durationMs: Long,
        recordCount: Int,
    ) {
        analyticsService.trackEvent(
            AnalyticsEvent.DatabaseOperation(
                operation = operation,
                table = "locations",
                durationMs = durationMs,
                recordCount = recordCount,
            ),
        )

        analyticsService.trackMetric(
            PerformanceMetric.ResponseTime(
                operation = "db_$operation",
                durationMs = durationMs,
            ),
        )
    }

    fun trackEncryptionOperation(
        operation: String,
        durationMs: Long,
        success: Boolean,
    ) {
        analyticsService.trackEvent(
            AnalyticsEvent.EncryptionOperation(
                operation = operation,
                durationMs = durationMs,
                success = success,
            ),
        )

        analyticsService.trackMetric(
            PerformanceMetric.ResponseTime(
                operation = "encryption_$operation",
                durationMs = durationMs,
            ),
        )
    }

    fun trackDatabaseSize(
        sizeMb: Double,
        recordCount: Int,
    ) {
        analyticsService.trackMetric(
            PerformanceMetric.DatabaseSize(
                sizeMb = sizeMb,
                recordCount = recordCount,
            ),
        )
    }

    fun trackMemoryUsage() {
        val runtime = Runtime.getRuntime()
        val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024.0 * 1024.0)
        val maxMemory = runtime.maxMemory() / (1024.0 * 1024.0)

        analyticsService.trackMetric(
            PerformanceMetric.MemoryUsage(
                usedMb = usedMemory,
                maxMb = maxMemory,
            ),
        )
    }

    fun trackBatteryUsage(percentagePerHour: Double) {
        analyticsService.trackMetric(
            PerformanceMetric.BatteryUsage(
                percentagePerHour = percentagePerHour,
            ),
        )
    }

    fun trackCommandReceived(
        commandType: String,
        durationMs: Long,
    ) {
        analyticsService.trackMetric(
            PerformanceMetric.ResponseTime(
                operation = "command_handle_$commandType",
                durationMs = durationMs,
            ),
        )
    }
}
