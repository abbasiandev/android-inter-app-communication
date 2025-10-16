package dev.abbasian.protocol

/**
 * All the different commands that Internet App can send to Location App.
 *
 * Each one has its own type string so we can serialize/deserialize properly.
 */
sealed class LocationCommand(val type: String) {
    /**
     * Tells Location App to fire up the location collection service
     */
    data object StartService : LocationCommand(TYPE_START_SERVICE) {
        override fun toString() = "StartService"
    }

    /**
     * Tells Location App to shut down the location collection service
     */
    data object StopService : LocationCommand(TYPE_STOP_SERVICE) {
        override fun toString() = "StopService"
    }

    /**
     * Asks for every location we've stored
     */
    data object GetAllLocations : LocationCommand(TYPE_GET_ALL_LOCATIONS) {
        override fun toString() = "GetAllLocations"
    }

    /**
     * Just wants the most recent location
     */
    data object GetLatestLocation : LocationCommand(TYPE_GET_LATEST_LOCATION) {
        override fun toString() = "GetLatestLocation"
    }

    companion object {
        const val TYPE_START_SERVICE = "START_SERVICE"
        const val TYPE_STOP_SERVICE = "STOP_SERVICE"
        const val TYPE_GET_ALL_LOCATIONS = "GET_ALL_LOCATIONS"
        const val TYPE_GET_LATEST_LOCATION = "GET_LATEST_LOCATION"

        /**
         * Takes a command type string and gives you back the actual command object.
         * Crashes if you pass in something invalid - use fromStringOrNull if you're not sure.
         * @param type The string identifier for the command
         * @return The matching LocationCommand
         * @throws IllegalArgumentException when the type doesn't match anything we know
         */
        fun fromString(type: String): LocationCommand =
            when (type) {
                TYPE_START_SERVICE -> StartService
                TYPE_STOP_SERVICE -> StopService
                TYPE_GET_ALL_LOCATIONS -> GetAllLocations
                TYPE_GET_LATEST_LOCATION -> GetLatestLocation
                else -> throw IllegalArgumentException("Unknown command type: $type")
            }

        /**
         * Same as fromString but returns null instead of crashing on bad input.
         * Better for parsing user input or data from external sources.
         * @param type The string identifier (can be null)
         * @return The matching command, or null if it doesn't match anything
         */
        fun fromStringOrNull(type: String?): LocationCommand? =
            when (type) {
                TYPE_START_SERVICE -> StartService
                TYPE_STOP_SERVICE -> StopService
                TYPE_GET_ALL_LOCATIONS -> GetAllLocations
                TYPE_GET_LATEST_LOCATION -> GetLatestLocation
                else -> null
            }

        /**
         * Gives you a list of every command type we support
         * @return All valid command type strings
         */
        fun getAllTypes(): List<String> =
            listOf(
                TYPE_START_SERVICE,
                TYPE_STOP_SERVICE,
                TYPE_GET_ALL_LOCATIONS,
                TYPE_GET_LATEST_LOCATION,
            )

        /**
         * Quick check to see if a type string is actually valid
         * @param type The string to check
         * @return true if it's a legit command type
         */
        fun isValidType(type: String?): Boolean = type != null && getAllTypes().contains(type)
    }
}
