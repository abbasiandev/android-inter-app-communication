package dev.locationapp.feature.command

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import dev.abbasian.protocol.AppLogger
import dev.abbasian.protocol.LocationCommand
import dev.abbasian.protocol.LocationData
import dev.abbasian.protocol.LocationResponse
import dev.abbasian.protocol.ProtocolConstants
import dev.locationapp.domain.repository.LocationRepository
import dev.locationapp.feature.location.LocationCollectionService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class LocationCommandProvider : ContentProvider() {
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface LocationCommandProviderEntryPoint {
        fun locationRepository(): LocationRepository

        fun logger(): AppLogger
    }

    // dedicated scope for provider. cancelled when the provider is shut down.
    private val providerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private lateinit var logger: AppLogger
    private lateinit var repository: LocationRepository

    override fun onCreate(): Boolean {
        val appContext = context?.applicationContext ?: return false

        val entryPoint =
            EntryPointAccessors.fromApplication(
                appContext,
                LocationCommandProviderEntryPoint::class.java,
            )

        logger = entryPoint.logger()
        repository = entryPoint.locationRepository()

        logger.i(TAG, "LocationCommandProvider created")
        return true
    }

    override fun shutdown() {
        super.shutdown()
        providerScope.cancel()
    }

    override fun call(
        method: String,
        arg: String?,
        extras: Bundle?,
    ): Bundle? {
        val callingPackage = callingPackage
        logger.d(TAG, "Call from package: $callingPackage")

        context?.enforceCallingPermission(
            "dev.locationapp.permission.ACCESS_LOCATION_COMMANDS",
            "Caller must have ACCESS_LOCATION_COMMANDS permission",
        )

        return when (method) {
            ProtocolConstants.METHOD_SEND_COMMAND -> {
                handleCommandFromBundle(extras)
            }

            else -> {
                logger.w(TAG, "Unknown method: $method")
                createErrorBundle(
                    "Unknown method: $method",
                    LocationResponse.ErrorCode.INTERNAL_ERROR,
                )
            }
        }
    }

    private fun handleCommandFromBundle(extras: Bundle?): Bundle {
        if (extras == null) {
            logger.e(TAG, "Received null extras")
            return createErrorBundle(
                "No command data received",
                LocationResponse.ErrorCode.INTERNAL_ERROR,
            )
        }

        val commandType = extras.getString(ProtocolConstants.EXTRA_COMMAND_TYPE)
        if (commandType == null) {
            logger.e(TAG, "No command type in extras")
            return createErrorBundle(
                "No command type specified",
                LocationResponse.ErrorCode.INTERNAL_ERROR,
            )
        }

        logger.i(TAG, "Processing command: $commandType")

        // command type string to LocationCommand object
        val command =
            try {
                when (commandType) {
                    LocationCommand.TYPE_START_SERVICE -> LocationCommand.StartService
                    LocationCommand.TYPE_STOP_SERVICE -> LocationCommand.StopService
                    LocationCommand.TYPE_GET_ALL_LOCATIONS -> LocationCommand.GetAllLocations
                    LocationCommand.TYPE_GET_LATEST_LOCATION -> LocationCommand.GetLatestLocation
                    else -> {
                        logger.e(TAG, "Unknown command type: $commandType")
                        return createErrorBundle(
                            "Unknown command type: $commandType",
                            LocationResponse.ErrorCode.INTERNAL_ERROR,
                        )
                    }
                }
            } catch (e: Exception) {
                logger.e(TAG, "Error parsing command", e)
                return createErrorBundle(
                    "Invalid command: ${e.message}",
                    LocationResponse.ErrorCode.INTERNAL_ERROR,
                )
            }

        return handleCommand(command)
    }

    private fun handleCommand(command: LocationCommand): Bundle {
        return try {
            when (command) {
                is LocationCommand.StartService -> handleStartService()
                is LocationCommand.StopService -> handleStopService()
                is LocationCommand.GetAllLocations -> handleGetAllLocations()
                is LocationCommand.GetLatestLocation -> handleGetLatestLocation()
            }
        } catch (e: Exception) {
            logger.e(TAG, "Error handling command: ${command.type}", e)
            createErrorBundle(
                "Error: ${e.message}",
                LocationResponse.ErrorCode.INTERNAL_ERROR,
            )
        }
    }

    private fun handleStartService(): Bundle {
        val ctx =
            context ?: return createErrorBundle(
                "Context is null",
                LocationResponse.ErrorCode.INTERNAL_ERROR,
            )
        val intent =
            Intent(ctx, LocationCollectionService::class.java).apply {
                action = LocationCollectionService.ACTION_START_COLLECTION
            }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ctx.startForegroundService(intent)
        } else {
            ctx.startService(intent)
        }
        logger.i(TAG, "Service start command executed")
        return createSuccessBundle("Location service started successfully")
    }

    private fun handleStopService(): Bundle {
        val ctx =
            context ?: return createErrorBundle(
                "Context is null",
                LocationResponse.ErrorCode.INTERNAL_ERROR,
            )
        val intent =
            Intent(ctx, LocationCollectionService::class.java).apply {
                action = LocationCollectionService.ACTION_STOP_COLLECTION
            }
        ctx.startService(intent)
        logger.i(TAG, "Service stop command executed")
        return createSuccessBundle("Location service stopped successfully")
    }

    private fun handleGetAllLocations(): Bundle {
        return runBlocking(providerScope.coroutineContext) {
            try {
                val locations = repository.getAllLocations().first()
                if (locations.isEmpty()) {
                    logger.w(TAG, "No locations available")
                    createErrorBundle(
                        "No locations stored yet",
                        LocationResponse.ErrorCode.NO_LOCATION_AVAILABLE,
                    )
                } else {
                    logger.i(TAG, "Returning ${locations.size} locations")
                    createLocationListBundle(locations)
                }
            } catch (e: Exception) {
                logger.e(TAG, "Failed to get locations", e)
                createErrorBundle(
                    "Failed to retrieve locations: ${e.message}",
                    LocationResponse.ErrorCode.INTERNAL_ERROR,
                )
            }
        }
    }

    private fun handleGetLatestLocation(): Bundle {
        return runBlocking(providerScope.coroutineContext) {
            try {
                val location = repository.getLatestLocation()
                if (location == null) {
                    logger.w(TAG, "No latest location available")
                    createErrorBundle(
                        "No location available yet",
                        LocationResponse.ErrorCode.NO_LOCATION_AVAILABLE,
                    )
                } else {
                    logger.i(TAG, "Returning latest location: ${location.getCoordinatesString()}")
                    createSingleLocationBundle(location)
                }
            } catch (e: Exception) {
                logger.e(TAG, "Failed to get latest location", e)
                createErrorBundle(
                    "Failed to retrieve location: ${e.message}",
                    LocationResponse.ErrorCode.INTERNAL_ERROR,
                )
            }
        }
    }

    private fun createSuccessBundle(message: String): Bundle {
        return Bundle().apply {
            putString(
                ProtocolConstants.EXTRA_RESPONSE_TYPE,
                ProtocolConstants.RESPONSE_TYPE_SUCCESS,
            )
            putString(ProtocolConstants.EXTRA_RESPONSE_DATA, message)
        }
    }

    private fun createErrorBundle(
        message: String,
        errorCode: LocationResponse.ErrorCode,
    ): Bundle {
        return Bundle().apply {
            putString(ProtocolConstants.EXTRA_RESPONSE_TYPE, ProtocolConstants.RESPONSE_TYPE_ERROR)
            putString(ProtocolConstants.EXTRA_ERROR_MESSAGE, message)
            putString(ProtocolConstants.EXTRA_ERROR_CODE, errorCode.name)
        }
    }

    private fun createLocationListBundle(locations: List<LocationData>): Bundle {
        return Bundle().apply {
            putString(
                ProtocolConstants.EXTRA_RESPONSE_TYPE,
                ProtocolConstants.RESPONSE_TYPE_LOCATION_LIST,
            )
            putParcelableArrayList(
                ProtocolConstants.EXTRA_LOCATION_LIST,
                kotlin.collections.ArrayList(locations),
            )
        }
    }

    private fun createSingleLocationBundle(location: LocationData): Bundle {
        return Bundle().apply {
            putString(
                ProtocolConstants.EXTRA_RESPONSE_TYPE,
                ProtocolConstants.RESPONSE_TYPE_SINGLE_LOCATION,
            )
            putParcelable(ProtocolConstants.EXTRA_LOCATION, location)
        }
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? = null

    override fun insert(
        uri: Uri,
        values: ContentValues?,
    ): Uri? = null

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0

    override fun getType(uri: Uri): String? = null

    companion object {
        private const val TAG = "LocationCommandProvider"
    }
}
