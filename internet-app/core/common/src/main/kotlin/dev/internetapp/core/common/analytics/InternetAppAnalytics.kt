package dev.internetapp.core.common.analytics

import dev.abbasian.protocol.analytics.AnalyticsEvent
import dev.abbasian.protocol.analytics.PerformanceMetric
import dev.abbasian.protocol.domain.analytics.IAnalyticsService
import dev.abbasian.protocol.domain.model.CommandError
import dev.abbasian.protocol.domain.model.LocationCommand

/**
 * Internet App specific analytics wrapper
 */
class InternetAppAnalytics(
    private val analyticsService: IAnalyticsService,
) {
    fun trackAppLaunched() {
        analyticsService.trackEvent(
            AnalyticsEvent.AppLaunched("InternetApp"),
        )
        analyticsService.setUserProperty("app_name", "InternetApp")
        analyticsService.setUserProperty("app_type", "controller")
    }

    fun trackScreenView(screenName: String) {
        analyticsService.trackEvent(
            AnalyticsEvent.ScreenViewed(screenName),
        )
    }

    fun trackCommandSent(
        command: LocationCommand,
        durationMs: Long,
        success: Boolean,
    ) {
        analyticsService.trackEvent(
            AnalyticsEvent.CommandSent(
                commandType = command.type,
                success = success,
                durationMs = durationMs,
            ),
        )

        analyticsService.trackMetric(
            PerformanceMetric.ResponseTime(
                operation = "command_${command.type}",
                durationMs = durationMs,
            ),
        )
    }

    fun trackCommandFailed(
        command: LocationCommand,
        error: CommandError,
    ) {
        analyticsService.trackEvent(
            AnalyticsEvent.CommandFailed(
                commandType = command.type,
                errorType = error::class.simpleName ?: "Unknown",
                errorMessage = error.message,
                isRecoverable = error.isRecoverable,
            ),
        )
    }

    fun trackCommandRetry(
        command: LocationCommand,
        attempt: Int,
        delayMs: Long,
    ) {
        analyticsService.trackEvent(
            AnalyticsEvent.CommandRetried(
                commandType = command.type,
                attemptNumber = attempt,
                delayMs = delayMs,
            ),
        )
    }

    fun trackButtonClick(buttonName: String) {
        analyticsService.trackEvent(
            AnalyticsEvent.ButtonClicked(
                buttonName = buttonName,
                screenName = "MainActivity",
            ),
        )
    }

    fun trackErrorDisplayed(
        error: CommandError,
        canRetry: Boolean,
    ) {
        analyticsService.trackEvent(
            AnalyticsEvent.ErrorDisplayed(
                errorType = error::class.simpleName ?: "Unknown",
                errorMessage = error.message,
                canRetry = canRetry,
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
}
