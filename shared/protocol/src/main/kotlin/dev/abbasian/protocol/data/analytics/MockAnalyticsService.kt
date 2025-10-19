package dev.abbasian.protocol.data.analytics

import dev.abbasian.protocol.analytics.AnalyticsEvent
import dev.abbasian.protocol.analytics.PerformanceMetric
import dev.abbasian.protocol.analytics.PerformanceStats
import dev.abbasian.protocol.domain.analytics.IAnalyticsService
import dev.abbasian.protocol.domain.analytics.TimedOperation
import dev.abbasian.protocol.domain.logger.AppLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

/**
 * Mock/Simulated Analytics Service for Firebase, Mixpanel, etc
 */
class MockAnalyticsService(
    private val logger: AppLogger,
    private val enableLogging: Boolean = true,
) : IAnalyticsService {
    @Suppress("ktlint:standard:backing-property-naming")
    private val _events = MutableStateFlow<List<AnalyticsEvent>>(emptyList())

    @Suppress("ktlint:standard:backing-property-naming")
    private val _metrics = MutableStateFlow<List<PerformanceMetric>>(emptyList())
    private val userProperties = ConcurrentHashMap<String, String>()

    private val eventCounts = ConcurrentHashMap<String, Int>()
    private val metricValues = ConcurrentHashMap<String, MutableList<Double>>()

    override fun trackEvent(event: AnalyticsEvent) {
        if (enableLogging) {
            logger.i(TAG, "Event: ${event.eventName} | Properties: ${event.properties}")
        }

        _events.value = _events.value + event

        eventCounts[event.eventName] = (eventCounts[event.eventName] ?: 0) + 1

        simulateNetworkCall(event.eventName)
    }

    override fun trackMetric(metric: PerformanceMetric) {
        if (enableLogging) {
            logger.i(
                TAG,
                "Metric: ${metric.metricName} = ${metric.value} ${metric.unit} | Tags: ${metric.tags}",
            )
        }

        _metrics.value = _metrics.value + metric

        metricValues.getOrPut(metric.metricName) { mutableListOf() }.add(metric.value)

        simulateNetworkCall(metric.metricName)
    }

    override fun trackException(
        throwable: Throwable,
        message: String,
        isFatal: Boolean,
    ) {
        logger.e(TAG, "Exception${if (isFatal) " (FATAL)" else ""}: $message", throwable)

        val event =
            AnalyticsEvent.CommandFailed(
                commandType = "exception",
                errorType = throwable::class.simpleName ?: "Unknown",
                errorMessage = message,
                isRecoverable = !isFatal,
            )
        trackEvent(event)

        if (isFatal) {
            logger.e(TAG, "Fatal exception reported to crash service")
        }
    }

    override fun setUserProperty(
        key: String,
        value: String,
    ) {
        userProperties[key] = value
        if (enableLogging) {
            logger.d(TAG, "User Property: $key = $value")
        }
    }

    override fun startTimer(operationName: String): TimedOperation {
        if (enableLogging) {
            logger.d(TAG, "Timer started: $operationName")
        }

        return TimedOperation(operationName) { name, duration ->
            if (enableLogging) {
                logger.d(TAG, "Timer stopped: $name (${duration}ms)")
            }
            trackMetric(PerformanceMetric.ResponseTime(name, duration))
        }
    }

    override fun getTrackedEvents(): Flow<List<AnalyticsEvent>> = _events.asStateFlow()

    override fun getPerformanceStats(metricName: String): Flow<PerformanceStats> =
        _metrics.asStateFlow().map { metrics ->
            val values =
                metrics
                    .filter { it.metricName == metricName }
                    .map { it.value }
                    .sorted()

            if (values.isEmpty()) {
                PerformanceStats(
                    metricName = metricName,
                    count = 0,
                    min = 0.0,
                    max = 0.0,
                    avg = 0.0,
                    median = 0.0,
                    p95 = 0.0,
                    p99 = 0.0,
                )
            } else {
                PerformanceStats(
                    metricName = metricName,
                    count = values.size,
                    min = values.first(),
                    max = values.last(),
                    avg = values.average(),
                    median = values[values.size / 2],
                    p95 = values[(values.size * 0.95).toInt()],
                    p99 = values[(values.size * 0.99).toInt()],
                )
            }
        }

    override suspend fun clearData() {
        _events.value = emptyList()
        _metrics.value = emptyList()
        eventCounts.clear()
        metricValues.clear()
        userProperties.clear()
        logger.i(TAG, "Analytics data cleared")
    }

    override suspend fun exportData(): String {
        val json = JSONObject()

        val eventsArray = JSONArray()
        _events.value.forEach { event ->
            val eventJson =
                JSONObject().apply {
                    put("event_name", event.eventName)
                    put("timestamp", event.timestamp)
                    put("properties", JSONObject(event.properties))
                }
            eventsArray.put(eventJson)
        }
        json.put("events", eventsArray)

        val metricsArray = JSONArray()
        _metrics.value.forEach { metric ->
            val metricJson =
                JSONObject().apply {
                    put("metric_name", metric.metricName)
                    put("value", metric.value)
                    put("unit", metric.unit)
                    put("timestamp", metric.timestamp)
                    put("tags", JSONObject(metric.tags))
                }
            metricsArray.put(metricJson)
        }
        json.put("metrics", metricsArray)

        json.put("user_properties", JSONObject(userProperties.toMap()))

        val statsJson = JSONObject()
        eventCounts.forEach { (event, count) ->
            statsJson.put(event, count)
        }
        json.put("event_counts", statsJson)

        logger.i(TAG, "Analytics data exported (${json.length()} bytes)")
        return json.toString(2)
    }

    private fun simulateNetworkCall(eventName: String) {
        if (enableLogging) {
            logger.d(TAG, "Simulated network call for: $eventName")
        }
    }

    fun getSummary(): String {
        val totalEvents = _events.value.size
        val totalMetrics = _metrics.value.size
        val uniqueEvents = eventCounts.size

        return buildString {
            appendLine("Analytics Summary")
            appendLine("━━━━━━━━━━━━━━━━━━━━")
            appendLine("Total Events: $totalEvents")
            appendLine("Unique Events: $uniqueEvents")
            appendLine("Total Metrics: $totalMetrics")
            appendLine("User Properties: ${userProperties.size}")
            appendLine()
            appendLine("Top Events:")
            eventCounts.entries
                .sortedByDescending { it.value }
                .take(5)
                .forEach { (event, count) ->
                    appendLine("  • $event: $count times")
                }
        }
    }

    companion object {
        private const val TAG = "Analytics"
    }
}
