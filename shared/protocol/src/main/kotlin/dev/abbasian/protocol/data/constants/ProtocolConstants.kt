package dev.abbasian.protocol.data.constants

import dev.abbasian.protocol.domain.model.LocationCommand

/**
 * Holds the constant values we need for talking between the two apps.
 * Both apps need to use the exact same values here or things won't work.
 */
object ProtocolConstants {
    // ==================== ContentProvider ====================

    /** The authority string for Location App's ContentProvider */
    const val AUTHORITY = "dev.locationapp.provider"

    /** Complete URI to access the provider */
    const val PROVIDER_URI = "content://$AUTHORITY"

    /** Method identifier when calling commands through ContentProvider.call() */
    const val METHOD_SEND_COMMAND = "send_command"

    // ==================== Bundle Keys ====================

    /** Bundle key that holds the command type in requests */
    const val EXTRA_COMMAND_TYPE = "command_type"

    /** Bundle key that holds the response type we get back */
    const val EXTRA_RESPONSE_TYPE = "response_type"

    /** Bundle key for generic string data in responses */
    const val EXTRA_RESPONSE_DATA = "response_data"

    /** Bundle key for the error code when something goes wrong */
    const val EXTRA_ERROR_CODE = "error_code"

    /** Bundle key for the error description text */
    const val EXTRA_ERROR_MESSAGE = "error_message"

    /** Bundle key for multiple locations (ArrayList<LocationData>) */
    const val EXTRA_LOCATION_LIST = "location_list"

    /** Bundle key when we're dealing with just one location (LocationData) */
    const val EXTRA_LOCATION = "location"

    // ==================== Response Types ====================

    /** Indicates the operation completed successfully */
    const val RESPONSE_TYPE_SUCCESS = "SUCCESS"

    /** Indicates we're returning multiple location records */
    const val RESPONSE_TYPE_LOCATION_LIST = "LOCATION_LIST"

    /** Indicates we're returning a single location record */
    const val RESPONSE_TYPE_SINGLE_LOCATION = "SINGLE_LOCATION"

    /** Indicates something went wrong during processing */
    const val RESPONSE_TYPE_ERROR = "ERROR"

    // ==================== Permissions ====================

    /** The custom permission needed to access Location App's command functionality */
    const val PERMISSION = "dev.locationapp.permission.ACCESS_LOCATION_COMMANDS"

    // ==================== Timeouts ====================

    /** How long we wait before giving up on a command */
    const val COMMAND_TIMEOUT_MS = 5000L

    /** Number of times we'll retry a command that fails */
    const val MAX_RETRY_ATTEMPTS = 3

    // ==================== Service Constants ====================

    /** ID number for the location service's ongoing notification */
    const val SERVICE_NOTIFICATION_ID = 1001

    /** Channel identifier for location service notifications */
    const val SERVICE_NOTIFICATION_CHANNEL_ID = "location_service_channel"

    /** How often we grab location updates in milliseconds */
    const val LOCATION_UPDATE_INTERVAL_MS = 60_000L

    // ==================== Validation ====================

    /**
     * Checks if the given command type is one we actually support
     */
    fun isValidCommandType(type: String?): Boolean = type != null && LocationCommand.Companion.getAllTypes().contains(type)

    /**
     * Checks if the response type is one of the recognized types
     */
    fun isValidResponseType(type: String?): Boolean =
        type in
            listOf(
                RESPONSE_TYPE_SUCCESS,
                RESPONSE_TYPE_LOCATION_LIST,
                RESPONSE_TYPE_SINGLE_LOCATION,
                RESPONSE_TYPE_ERROR,
            )
}
