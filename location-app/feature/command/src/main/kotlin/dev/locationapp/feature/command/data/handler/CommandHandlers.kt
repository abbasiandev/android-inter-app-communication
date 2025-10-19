package dev.locationapp.feature.command.data.handler

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import dev.abbasian.protocol.data.constants.ProtocolConstants
import dev.abbasian.protocol.domain.logger.AppLogger
import dev.abbasian.protocol.domain.model.LocationData
import dev.abbasian.protocol.domain.model.LocationResponse
import dev.locationapp.feature.location.domain.repository.LocationRepository
import dev.locationapp.feature.location.service.LocationCollectionService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.CoroutineContext

class CommandHandlers(
    private val context: Context,
    private val repository: LocationRepository,
    private val logger: AppLogger,
    private val coroutineContext: CoroutineContext,
) {
    fun handleStartService(): Bundle =
        try {
            val intent =
                Intent(context, LocationCollectionService::class.java).apply {
                    action = LocationCollectionService.ACTION_START_COLLECTION
                }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            logger.i(TAG, "Service start command executed")
            BundleFactory.createSuccessBundle("Location service started successfully")
        } catch (e: SecurityException) {
            logger.e(TAG, "Permission denied to start service", e)
            BundleFactory.createErrorBundle(
                "Permission denied to start location service",
                LocationResponse.ErrorCode.PERMISSION_DENIED,
            )
        } catch (e: Exception) {
            logger.e(TAG, "Failed to start service", e)
            BundleFactory.createErrorBundle(
                "Failed to start service: ${e.message}",
                LocationResponse.ErrorCode.INTERNAL_ERROR,
            )
        }

    fun handleStopService(): Bundle =
        try {
            val intent =
                Intent(context, LocationCollectionService::class.java).apply {
                    action = LocationCollectionService.ACTION_STOP_COLLECTION
                }
            context.startService(intent)
            logger.i(TAG, "Service stop command executed")
            BundleFactory.createSuccessBundle("Location service stopped successfully")
        } catch (e: Exception) {
            logger.e(TAG, "Failed to stop service", e)
            BundleFactory.createErrorBundle(
                "Failed to stop service: ${e.message}",
                LocationResponse.ErrorCode.INTERNAL_ERROR,
            )
        }

    fun handleGetAllLocations(): Bundle =
        runBlocking(coroutineContext) {
            try {
                val locations =
                    withTimeout(5000L) {
                        repository.getAllLocations().first()
                    }

                if (locations.isEmpty()) {
                    logger.w(TAG, "No locations available")
                    BundleFactory.createErrorBundle(
                        "No locations stored yet. Start the service to collect location data.",
                        LocationResponse.ErrorCode.NO_LOCATION_AVAILABLE,
                    )
                } else {
                    logger.i(TAG, "Returning ${locations.size} locations")
                    BundleFactory.createLocationListBundle(locations)
                }
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                logger.e(TAG, "Timeout getting locations", e)
                BundleFactory.createErrorBundle(
                    "Database query timed out",
                    LocationResponse.ErrorCode.INTERNAL_ERROR,
                )
            } catch (e: Exception) {
                logger.e(TAG, "Error getting all locations", e)
                BundleFactory.createErrorBundle(
                    "Failed to retrieve locations: ${e.message}",
                    LocationResponse.ErrorCode.INTERNAL_ERROR,
                )
            }
        }

    fun handleGetLatestLocation(): Bundle =
        runBlocking(coroutineContext) {
            try {
                val location =
                    withTimeout(3000L) {
                        repository.getLatestLocation()
                    }

                if (location == null) {
                    logger.w(TAG, "No latest location available")
                    BundleFactory.createErrorBundle(
                        "No location available yet. Start the service to begin tracking.",
                        LocationResponse.ErrorCode.NO_LOCATION_AVAILABLE,
                    )
                } else {
                    logger.i(TAG, "Returning latest location: ${location.getCoordinatesString()}")
                    BundleFactory.createSingleLocationBundle(location)
                }
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                logger.e(TAG, "Timeout getting latest location", e)
                BundleFactory.createErrorBundle(
                    "Database query timed out",
                    LocationResponse.ErrorCode.INTERNAL_ERROR,
                )
            } catch (e: Exception) {
                logger.e(TAG, "Error getting latest location", e)
                BundleFactory.createErrorBundle(
                    "Failed to retrieve latest location: ${e.message}",
                    LocationResponse.ErrorCode.INTERNAL_ERROR,
                )
            }
        }

    companion object {
        private const val TAG = "CommandHandlers"
    }
}

object BundleFactory {
    fun createSuccessBundle(message: String): Bundle =
        Bundle().apply {
            putString(
                ProtocolConstants.EXTRA_RESPONSE_TYPE,
                ProtocolConstants.RESPONSE_TYPE_SUCCESS,
            )
            putString(ProtocolConstants.EXTRA_RESPONSE_DATA, message)
        }

    fun createErrorBundle(
        message: String,
        errorCode: LocationResponse.ErrorCode,
    ): Bundle =
        Bundle().apply {
            putString(ProtocolConstants.EXTRA_RESPONSE_TYPE, ProtocolConstants.RESPONSE_TYPE_ERROR)
            putString(ProtocolConstants.EXTRA_ERROR_MESSAGE, message)
            putString(ProtocolConstants.EXTRA_ERROR_CODE, errorCode.name)
        }

    fun createLocationListBundle(locations: List<LocationData>): Bundle =
        Bundle().apply {
            putString(
                ProtocolConstants.EXTRA_RESPONSE_TYPE,
                ProtocolConstants.RESPONSE_TYPE_LOCATION_LIST,
            )
            putParcelableArrayList(
                ProtocolConstants.EXTRA_LOCATION_LIST,
                ArrayList(locations),
            )
        }

    fun createSingleLocationBundle(location: LocationData): Bundle =
        Bundle().apply {
            putString(
                ProtocolConstants.EXTRA_RESPONSE_TYPE,
                ProtocolConstants.RESPONSE_TYPE_SINGLE_LOCATION,
            )
            putParcelable(ProtocolConstants.EXTRA_LOCATION, location)
        }
}
