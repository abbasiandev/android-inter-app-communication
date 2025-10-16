package dev.abbasian.protocol

/**
 * All the different types of responses Location App can send back to Internet App.
 */
sealed class LocationResponse {
    /**
     * Everything worked fine
     * @param message A message explaining what succeeded
     */
    data class Success(val message: String) : LocationResponse() {
        override fun toString() = "Success: $message"
    }

    /**
     * Here's a bunch of locations you asked for
     * @param locations All the LocationData we found
     */
    data class LocationList(val locations: List<LocationData>) : LocationResponse() {
        override fun toString() = "LocationList: ${locations.size} locations"
    }

    /**
     * Here's one specific location (or null if we don't have one)
     * @param location The LocationData, or null if none exists
     */
    data class SingleLocation(val location: LocationData?) : LocationResponse() {
        override fun toString() = "SingleLocation: $location"
    }

    /**
     * Something went wrong
     * @param message What happened
     * @param code Which specific error it was
     */
    data class Error(
        val message: String,
        val code: ErrorCode = ErrorCode.INTERNAL_ERROR,
    ) : LocationResponse() {
        override fun toString() = "Error[$code]: $message"
    }

    /**
     * Different categories of things that can go wrong
     */
    enum class ErrorCode(val value: String) {
        /** Location App said no to the permission request */
        PERMISSION_DENIED("PERMISSION_DENIED"),

        /** The location service isn't active right now */
        SERVICE_NOT_RUNNING("SERVICE_NOT_RUNNING"),

        /** We don't have any location data to give you */
        NO_LOCATION_AVAILABLE("NO_LOCATION_AVAILABLE"),

        /** Something broke inside Location App */
        INTERNAL_ERROR("INTERNAL_ERROR"),

        /** The command didn't make sense */
        INVALID_COMMAND("INVALID_COMMAND"),

        /** The two apps couldn't talk to each other properly */
        COMMUNICATION_ERROR("COMMUNICATION_ERROR"),
        ;

        companion object {
            fun fromString(value: String): ErrorCode = values().find { it.value == value } ?: INTERNAL_ERROR
        }
    }
}
