@file:Suppress("ImplicitDefaultLocale")

package dev.abbasian.protocol.data.analytics

import dev.abbasian.protocol.analytics.AnalyticsEvent
import dev.abbasian.protocol.analytics.PerformanceMetric
import dev.abbasian.protocol.domain.logger.AppLogger

object AnalyticsLogger {
    fun logEvent(
        logger: AppLogger,
        event: AnalyticsEvent,
    ) {
        val message =
            buildString {
                append("${event.eventName}")
                if (event.properties.isNotEmpty()) {
                    append(" | ")
                    event.properties.entries
                        .joinToString(", ") { (key, value) ->
                            "$key=$value"
                        }.let { append(it) }
                }
            }
        logger.i("Analytics", message)
    }

    fun logMetric(
        logger: AppLogger,
        metric: PerformanceMetric,
    ) {
        val message =
            buildString {
                append("${metric.metricName}: ${metric.value} ${metric.unit}")
                if (metric.tags.isNotEmpty()) {
                    append(" | ")
                    metric.tags.entries
                        .joinToString(", ") { (key, value) ->
                            "$key=$value"
                        }.let { append(it) }
                }
            }
        logger.i("Performance", message)
    }

    fun formatDuration(durationMs: Long): String =
        when {
            durationMs < 1000 -> "${durationMs}ms"
            durationMs < 60000 -> String.format("%.2fs", durationMs / 1000.0)
            else -> String.format("%.2fm", durationMs / 60000.0)
        }
}
