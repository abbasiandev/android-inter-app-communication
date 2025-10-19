@file:Suppress("TooGenericExceptionCaught")

package dev.locationapp.feature.command.data.handler

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import dev.abbasian.protocol.data.constants.ProtocolConstants
import dev.abbasian.protocol.domain.logger.AppLogger
import dev.abbasian.protocol.domain.model.LocationData
import dev.abbasian.protocol.domain.model.LocationResponse
import dev.locationapp.analytics.LocationAppAnalytics
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
    private val analytics: LocationAppAnalytics,
    private val coroutineContext: CoroutineContext,
) {
    fun handleStartService(): Bundle {
        val startTime = System.currentTimeMillis()

        return try {
            val intent =
                Intent(context, LocationCollectionService::class.java).apply {
                    action = LocationCollectionService.ACTION_START_COLLECTION
                }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }

            val duration = System.currentTimeMillis() - startTime
            logger.i(TAG, "Service start command executed in ${duration}ms")

            analytics.trackCommandReceived("START_SERVICE", duration)

            BundleFactory.createSuccessBundle("Location service started successfully")
        } catch (e: SecurityException) {
            val duration = System.currentTimeMillis() - startTime
            logger.e(TAG, "Permission denied to start service", e)
            analytics.trackCommandReceived("START_SERVICE_FAILED", duration)

            BundleFactory.createErrorBundle(
                "Permission denied to start location service",
                LocationResponse.ErrorCode.PERMISSION_DENIED,
            )
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            logger.e(TAG, "Failed to start service", e)
            analytics.trackCommandReceived("START_SERVICE_FAILED", duration)

            BundleFactory.createErrorBundle(
                "Failed to start service: ${e.message}",
                LocationResponse.ErrorCode.INTERNAL_ERROR,
            )
        }
    }

    fun handleStopService(): Bundle {
        val startTime = System.currentTimeMillis()

        return try {
            val intent =
                Intent(context, LocationCollectionService::class.java).apply {
                    action = LocationCollectionService.ACTION_STOP_COLLECTION
                }
            context.startService(intent)

            val duration = System.currentTimeMillis() - startTime
            logger.i(TAG, "Service stop command executed in ${duration}ms")

            analytics.trackCommandReceived("STOP_SERVICE", duration)

            BundleFactory.createSuccessBundle("Location service stopped successfully")
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            logger.e(TAG, "Failed to stop service", e)
            analytics.trackCommandReceived("STOP_SERVICE_FAILED", duration)

            BundleFactory.createErrorBundle(
                "Failed to stop service: ${e.message}",
                LocationResponse.ErrorCode.INTERNAL_ERROR,
            )
        }
    }

    fun handleGetAllLocations(): Bundle =
        runBlocking(coroutineContext) {
            val startTime = System.currentTimeMillis()

            try {
                val locations =
                    withTimeout(5000L) {
                        repository.getAllLocations().first()
                    }

                val duration = System.currentTimeMillis() - startTime
                analytics.trackCommandReceived("GET_ALL_LOCATIONS", duration)

                if (locations.isEmpty()) {
                    logger.w(TAG, "No locations available")
                    BundleFactory.createErrorBundle(
                        "No locations stored yet. Start the service to collect location data.",
                        LocationResponse.ErrorCode.NO_LOCATION_AVAILABLE,
                    )
                } else {
                    logger.i(TAG, "Returning ${locations.size} locations in ${duration}ms")
                    BundleFactory.createLocationListBundle(locations)
                }
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                val duration = System.currentTimeMillis() - startTime
                logger.e(TAG, "Timeout getting locations", e)
                analytics.trackCommandReceived("GET_ALL_LOCATIONS_TIMEOUT", duration)

                BundleFactory.createErrorBundle(
                    "Database query timed out",
                    LocationResponse.ErrorCode.INTERNAL_ERROR,
                )
            } catch (e: Exception) {
                val duration = System.currentTimeMillis() - startTime
                logger.e(TAG, "Error getting all locations", e)
                analytics.trackCommandReceived("GET_ALL_LOCATIONS_FAILED", duration)

                BundleFactory.createErrorBundle(
                    "Failed to retrieve locations: ${e.message}",
                    LocationResponse.ErrorCode.INTERNAL_ERROR,
                )
            }
        }

    fun handleGetLatestLocation(): Bundle =
        runBlocking(coroutineContext) {
            val startTime = System.currentTimeMillis()

            try {
                val location =
                    withTimeout(3000L) {
                        repository.getLatestLocation()
                    }

                val duration = System.currentTimeMillis() - startTime
                analytics.trackCommandReceived("GET_LATEST_LOCATION", duration)

                if (location == null) {
                    logger.w(TAG, "No latest location available")
                    BundleFactory.createErrorBundle(
                        "No location available yet. Start the service to begin tracking.",
                        LocationResponse.ErrorCode.NO_LOCATION_AVAILABLE,
                    )
                } else {
                    logger.i(
                        TAG,
                        "Returning latest location: ${location.getCoordinatesString()} in ${duration}ms",
                    )
                    BundleFactory.createSingleLocationBundle(location)
                }
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                val duration = System.currentTimeMillis() - startTime
                logger.e(TAG, "Timeout getting latest location", e)
                analytics.trackCommandReceived("GET_LATEST_LOCATION_TIMEOUT", duration)

                BundleFactory.createErrorBundle(
                    "Database query timed out",
                    LocationResponse.ErrorCode.INTERNAL_ERROR,
                )
            } catch (e: Exception) {
                val duration = System.currentTimeMillis() - startTime
                logger.e(TAG, "Error getting latest location", e)
                analytics.trackCommandReceived("GET_LATEST_LOCATION_FAILED", duration)

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
