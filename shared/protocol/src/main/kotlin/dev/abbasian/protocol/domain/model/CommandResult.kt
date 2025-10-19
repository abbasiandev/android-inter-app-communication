package dev.abbasian.protocol.domain.model

sealed class CommandResult<out T> {
    data class Success<T>(
        val data: T,
    ) : CommandResult<T>()

    data class Failure(
        val error: CommandError,
    ) : CommandResult<Nothing>()

    fun isSuccess(): Boolean = this is Success

    fun isFailure(): Boolean = this is Failure

    fun getOrNull(): T? =
        when (this) {
            is Success -> data
            is Failure -> null
        }

    fun errorOrNull(): CommandError? =
        when (this) {
            is Success -> null
            is Failure -> error
        }

    inline fun onSuccess(action: (T) -> Unit): CommandResult<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onFailure(action: (CommandError) -> Unit): CommandResult<T> {
        if (this is Failure) action(error)
        return this
    }
}

sealed class CommandError(
    open val message: String,
    open val throwable: Throwable? = null,
    val isRecoverable: Boolean = true,
    val requiresUserAction: Boolean = false,
) {
    data class ProviderNotFound(
        override val message: String = "Location App not installed or not accessible",
        override val throwable: Throwable? = null,
    ) : CommandError(message, throwable, isRecoverable = false, requiresUserAction = true)

    data class Timeout(
        override val message: String = "Command timed out",
        override val throwable: Throwable? = null,
    ) : CommandError(message, throwable, isRecoverable = true, requiresUserAction = false)

    data class PermissionDenied(
        override val message: String = "Permission denied",
        override val throwable: Throwable? = null,
    ) : CommandError(message, throwable, isRecoverable = false, requiresUserAction = true)

    data class ServiceNotRunning(
        override val message: String = "Location service is not running",
        override val throwable: Throwable? = null,
    ) : CommandError(message, throwable, isRecoverable = true, requiresUserAction = true)

    data class LocationUnavailable(
        override val message: String = "No location data available",
        override val throwable: Throwable? = null,
    ) : CommandError(message, throwable, isRecoverable = true, requiresUserAction = false)

    data class InvalidResponse(
        override val message: String = "Invalid or corrupted response",
        override val throwable: Throwable? = null,
    ) : CommandError(message, throwable, isRecoverable = true, requiresUserAction = false)

    data class ParseError(
        override val message: String = "Failed to parse response",
        override val throwable: Throwable? = null,
    ) : CommandError(message, throwable, isRecoverable = true, requiresUserAction = false)

    data class DatabaseError(
        override val message: String = "Database operation failed",
        override val throwable: Throwable? = null,
    ) : CommandError(message, throwable, isRecoverable = true, requiresUserAction = false)

    data class Unknown(
        override val message: String = "An unexpected error occurred",
        override val throwable: Throwable? = null,
    ) : CommandError(message, throwable, isRecoverable = true, requiresUserAction = false)

    companion object {
        fun fromLocationResponse(error: LocationResponse.Error): CommandError =
            when (error.code) {
                LocationResponse.ErrorCode.PERMISSION_DENIED ->
                    PermissionDenied(error.message)

                LocationResponse.ErrorCode.SERVICE_NOT_RUNNING ->
                    ServiceNotRunning(error.message)

                LocationResponse.ErrorCode.NO_LOCATION_AVAILABLE ->
                    LocationUnavailable(error.message)

                LocationResponse.ErrorCode.COMMUNICATION_ERROR ->
                    ProviderNotFound(error.message)

                LocationResponse.ErrorCode.INVALID_COMMAND ->
                    InvalidResponse(error.message)

                LocationResponse.ErrorCode.INTERNAL_ERROR ->
                    Unknown(error.message)
            }
    }
}
