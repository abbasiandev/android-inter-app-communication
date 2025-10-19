package dev.abbasian.protocol.domain.analytics

import dev.abbasian.protocol.analytics.AnalyticsEvent
import dev.abbasian.protocol.analytics.PerformanceMetric
import dev.abbasian.protocol.analytics.PerformanceStats
import kotlinx.coroutines.flow.Flow

interface IAnalyticsService {
    /**
     * Track an analytics event
     */
    fun trackEvent(event: AnalyticsEvent)

    /**
     * Track a performance metric
     */
    fun trackMetric(metric: PerformanceMetric)

    /**
     * Track an exception/error
     */
    fun trackException(
        throwable: Throwable,
        message: String,
        isFatal: Boolean = false,
    )

    /**
     * Set user property for analytics
     */
    fun setUserProperty(
        key: String,
        value: String,
    )

    /**
     * Start a timed event (returns duration when stopped)
     */
    fun startTimer(operationName: String): TimedOperation

    /**
     * Get all tracked events (for debugging/testing)
     */
    fun getTrackedEvents(): Flow<List<AnalyticsEvent>>

    /**
     * Get performance statistics
     */
    fun getPerformanceStats(metricName: String): Flow<PerformanceStats>

    /**
     * Clear all analytics data
     */
    suspend fun clearData()

    /**
     * Export analytics data as JSON
     */
    suspend fun exportData(): String
}

class TimedOperation(
    val operationName: String,
    private val startTime: Long = System.currentTimeMillis(),
    private val onComplete: (String, Long) -> Unit,
) {
    fun stop(): Long {
        val duration = System.currentTimeMillis() - startTime
        onComplete(operationName, duration)
        return duration
    }
}
