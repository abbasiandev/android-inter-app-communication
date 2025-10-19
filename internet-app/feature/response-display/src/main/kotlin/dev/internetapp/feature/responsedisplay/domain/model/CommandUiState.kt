package dev.internetapp.feature.responsedisplay.domain.model

import dev.abbasian.protocol.domain.model.CommandError
import dev.abbasian.protocol.domain.model.LocationData

data class CommandUiState(
    val isLoading: Boolean = false,
    val lastResponse: String? = null,
    val locations: List<LocationData> = emptyList(),
    val latestLocation: LocationData? = null,
    val error: ErrorState? = null,
    val serviceStatus: ServiceStatus = ServiceStatus.UNKNOWN,
)

sealed class ErrorState(
    open val error: CommandError,
    open val canRetry: Boolean,
    open val userMessage: String,
    open val actionLabel: String? = null,
) {
    data class Recoverable(
        override val error: CommandError,
        override val userMessage: String,
    ) : ErrorState(error, canRetry = true, userMessage, actionLabel = "Retry")

    data class NonRecoverable(
        override val error: CommandError,
        override val userMessage: String,
        override val actionLabel: String? = null,
    ) : ErrorState(error, canRetry = false, userMessage, actionLabel)

    companion object {
        fun from(error: CommandError): ErrorState =
            when (error) {
                is CommandError.ProviderNotFound ->
                    NonRecoverable(
                        error = error,
                        userMessage = "Location App is not installed or not accessible.Both apps are signed with the same key",
                        actionLabel = "Help",
                    )

                is CommandError.PermissionDenied ->
                    NonRecoverable(
                        error = error,
                        userMessage = "Permission denied.\n\nBoth apps must be signed with the same signing key to communicate.",
                        actionLabel = "Learn More",
                    )

                is CommandError.Timeout ->
                    Recoverable(
                        error = error,
                        userMessage = "Request timed out.\n\nThe Location App took too long to respond. Please try again.",
                    )

                is CommandError.ServiceNotRunning ->
                    Recoverable(
                        error = error,
                        userMessage = "Location service is not running.\n\nPlease start the service first",
                    )

                is CommandError.LocationUnavailable ->
                    Recoverable(
                        error = error,
                        userMessage = "No location data available yet.\n\nPlease wait for the service to collect location data.",
                    )

                is CommandError.InvalidResponse,
                is CommandError.ParseError,
                ->
                    Recoverable(
                        error = error,
                        userMessage = "Failed to process response.\n\n${error.message}\n\nPlease try again.",
                    )

                is CommandError.DatabaseError ->
                    Recoverable(
                        error = error,
                        userMessage = "Database error occurred.\n\n${error.message}",
                    )

                is CommandError.Unknown ->
                    Recoverable(
                        error = error,
                        userMessage = "An unexpected error occurred.\n\n${error.message}",
                    )
            }
    }
}
