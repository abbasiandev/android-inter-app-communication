package dev.abbasian.protocol.analytics

sealed class AnalyticsEvent(
    open val eventName: String,
    open val timestamp: Long = System.currentTimeMillis(),
    open val properties: Map<String, Any> = emptyMap(),
) {
    data class CommandSent(
        val commandType: String,
        val success: Boolean,
        val durationMs: Long,
        override val timestamp: Long = System.currentTimeMillis(),
    ) : AnalyticsEvent(
            eventName = "command_sent",
            timestamp = timestamp,
            properties =
                mapOf(
                    "command_type" to commandType,
                    "success" to success,
                    "duration_ms" to durationMs,
                ),
        )

    data class CommandFailed(
        val commandType: String,
        val errorType: String,
        val errorMessage: String,
        val isRecoverable: Boolean,
        override val timestamp: Long = System.currentTimeMillis(),
    ) : AnalyticsEvent(
            eventName = "command_failed",
            timestamp = timestamp,
            properties =
                mapOf(
                    "command_type" to commandType,
                    "error_type" to errorType,
                    "error_message" to errorMessage,
                    "is_recoverable" to isRecoverable,
                ),
        )

    data class CommandRetried(
        val commandType: String,
        val attemptNumber: Int,
        val delayMs: Long,
        override val timestamp: Long = System.currentTimeMillis(),
    ) : AnalyticsEvent(
            eventName = "command_retried",
            timestamp = timestamp,
            properties =
                mapOf(
                    "command_type" to commandType,
                    "attempt_number" to attemptNumber,
                    "delay_ms" to delayMs,
                ),
        )

    data class ServiceStarted(
        val serviceType: String,
        override val timestamp: Long = System.currentTimeMillis(),
    ) : AnalyticsEvent(
            eventName = "service_started",
            timestamp = timestamp,
            properties = mapOf("service_type" to serviceType),
        )

    data class ServiceStopped(
        val serviceType: String,
        val durationMs: Long,
        override val timestamp: Long = System.currentTimeMillis(),
    ) : AnalyticsEvent(
            eventName = "service_stopped",
            timestamp = timestamp,
            properties =
                mapOf(
                    "service_type" to serviceType,
                    "duration_ms" to durationMs,
                ),
        )

    data class LocationCollected(
        val accuracy: Float,
        val provider: String,
        val timeSinceLastMs: Long,
        override val timestamp: Long = System.currentTimeMillis(),
    ) : AnalyticsEvent(
            eventName = "location_collected",
            timestamp = timestamp,
            properties =
                mapOf(
                    "accuracy" to accuracy,
                    "provider" to provider,
                    "time_since_last_ms" to timeSinceLastMs,
                ),
        )

    data class LocationSaved(
        val success: Boolean,
        val durationMs: Long,
        override val timestamp: Long = System.currentTimeMillis(),
    ) : AnalyticsEvent(
            eventName = "location_saved",
            timestamp = timestamp,
            properties =
                mapOf(
                    "success" to success,
                    "duration_ms" to durationMs,
                ),
        )

    data class DatabaseOperation(
        val operation: String,
        val table: String,
        val durationMs: Long,
        val recordCount: Int,
        override val timestamp: Long = System.currentTimeMillis(),
    ) : AnalyticsEvent(
            eventName = "database_operation",
            timestamp = timestamp,
            properties =
                mapOf(
                    "operation" to operation,
                    "table" to table,
                    "duration_ms" to durationMs,
                    "record_count" to recordCount,
                ),
        )

    data class EncryptionOperation(
        val operation: String,
        val durationMs: Long,
        val success: Boolean,
        override val timestamp: Long = System.currentTimeMillis(),
    ) : AnalyticsEvent(
            eventName = "encryption_operation",
            timestamp = timestamp,
            properties =
                mapOf(
                    "operation" to operation,
                    "duration_ms" to durationMs,
                    "success" to success,
                ),
        )

    data class ScreenViewed(
        val screenName: String,
        override val timestamp: Long = System.currentTimeMillis(),
    ) : AnalyticsEvent(
            eventName = "screen_viewed",
            timestamp = timestamp,
            properties = mapOf("screen_name" to screenName),
        )

    data class ButtonClicked(
        val buttonName: String,
        val screenName: String,
        override val timestamp: Long = System.currentTimeMillis(),
    ) : AnalyticsEvent(
            eventName = "button_clicked",
            timestamp = timestamp,
            properties =
                mapOf(
                    "button_name" to buttonName,
                    "screen_name" to screenName,
                ),
        )

    data class ErrorDisplayed(
        val errorType: String,
        val errorMessage: String,
        val canRetry: Boolean,
        override val timestamp: Long = System.currentTimeMillis(),
    ) : AnalyticsEvent(
            eventName = "error_displayed",
            timestamp = timestamp,
            properties =
                mapOf(
                    "error_type" to errorType,
                    "error_message" to errorMessage,
                    "can_retry" to canRetry,
                ),
        )

    data class AppLaunched(
        val appName: String,
        override val timestamp: Long = System.currentTimeMillis(),
    ) : AnalyticsEvent(
            eventName = "app_launched",
            timestamp = timestamp,
            properties = mapOf("app_name" to appName),
        )

    data class AppBackgrounded(
        val appName: String,
        val sessionDurationMs: Long,
        override val timestamp: Long = System.currentTimeMillis(),
    ) : AnalyticsEvent(
            eventName = "app_backgrounded",
            timestamp = timestamp,
            properties =
                mapOf(
                    "app_name" to appName,
                    "session_duration_ms" to sessionDurationMs,
                ),
        )
}
