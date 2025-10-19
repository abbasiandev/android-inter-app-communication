package dev.abbasian.protocol.analytics

sealed class PerformanceMetric(
    open val metricName: String,
    open val value: Double,
    open val unit: String,
    open val timestamp: Long = System.currentTimeMillis(),
    open val tags: Map<String, String> = emptyMap(),
) {
    data class ResponseTime(
        val operation: String,
        val durationMs: Long,
        override val timestamp: Long = System.currentTimeMillis(),
    ) : PerformanceMetric(
            metricName = "response_time",
            value = durationMs.toDouble(),
            unit = "ms",
            timestamp = timestamp,
            tags = mapOf("operation" to operation),
        )

    data class MemoryUsage(
        val usedMb: Double,
        val maxMb: Double,
        override val timestamp: Long = System.currentTimeMillis(),
    ) : PerformanceMetric(
            metricName = "memory_usage",
            value = usedMb,
            unit = "MB",
            timestamp = timestamp,
            tags = mapOf("max_mb" to maxMb.toString()),
        )

    data class DatabaseSize(
        val sizeMb: Double,
        val recordCount: Int,
        override val timestamp: Long = System.currentTimeMillis(),
    ) : PerformanceMetric(
            metricName = "database_size",
            value = sizeMb,
            unit = "MB",
            timestamp = timestamp,
            tags = mapOf("record_count" to recordCount.toString()),
        )

    data class NetworkLatency(
        val latencyMs: Long,
        val endpoint: String,
        override val timestamp: Long = System.currentTimeMillis(),
    ) : PerformanceMetric(
            metricName = "network_latency",
            value = latencyMs.toDouble(),
            unit = "ms",
            timestamp = timestamp,
            tags = mapOf("endpoint" to endpoint),
        )

    data class BatteryUsage(
        val percentagePerHour: Double,
        override val timestamp: Long = System.currentTimeMillis(),
    ) : PerformanceMetric(
            metricName = "battery_usage",
            value = percentagePerHour,
            unit = "%/hour",
            timestamp = timestamp,
        )

    data class LocationAccuracy(
        val accuracyMeters: Float,
        val provider: String,
        override val timestamp: Long = System.currentTimeMillis(),
    ) : PerformanceMetric(
            metricName = "location_accuracy",
            value = accuracyMeters.toDouble(),
            unit = "meters",
            timestamp = timestamp,
            tags = mapOf("provider" to provider),
        )

    data class CpuUsage(
        val percentage: Double,
        override val timestamp: Long = System.currentTimeMillis(),
    ) : PerformanceMetric(
            metricName = "cpu_usage",
            value = percentage,
            unit = "%",
            timestamp = timestamp,
        )

    data class FrameRate(
        val fps: Double,
        override val timestamp: Long = System.currentTimeMillis(),
    ) : PerformanceMetric(
            metricName = "frame_rate",
            value = fps,
            unit = "fps",
            timestamp = timestamp,
        )
}

data class PerformanceStats(
    val metricName: String,
    val count: Int,
    val min: Double,
    val max: Double,
    val avg: Double,
    val median: Double,
    val p95: Double,
    val p99: Double,
    val timestamp: Long = System.currentTimeMillis(),
)
