package dev.locationapp.feature.command

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import dev.abbasian.protocol.AppLogger
import dev.abbasian.protocol.LocationData
import dev.abbasian.protocol.LocationResponse
import dev.abbasian.protocol.ProtocolConstants
import dev.locationapp.domain.repository.LocationRepository
import dev.locationapp.feature.location.LocationCollectionService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext

class CommandHandlers(
    private val context: Context,
    private val repository: LocationRepository,
    private val logger: AppLogger,
    private val coroutineContext: CoroutineContext,
) {
    fun handleStartService(): Bundle {
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
        return BundleFactory.createSuccessBundle("Location service started successfully")
    }

    fun handleStopService(): Bundle {
        val intent =
            Intent(context, LocationCollectionService::class.java).apply {
                action = LocationCollectionService.ACTION_STOP_COLLECTION
            }
        context.startService(intent)
        logger.i(TAG, "Service stop command executed")
        return BundleFactory.createSuccessBundle("Location service stopped successfully")
    }

    fun handleGetAllLocations(): Bundle =
        runBlocking(coroutineContext) {
            val locations = repository.getAllLocations().first()
            if (locations.isEmpty()) {
                logger.w(TAG, "No locations available")
                BundleFactory.createErrorBundle(
                    "No locations stored yet",
                    LocationResponse.ErrorCode.NO_LOCATION_AVAILABLE,
                )
            } else {
                logger.i(TAG, "Returning ${locations.size} locations")
                BundleFactory.createLocationListBundle(locations)
            }
        }

    fun handleGetLatestLocation(): Bundle =
        runBlocking(coroutineContext) {
            val location = repository.getLatestLocation()
            if (location == null) {
                logger.w(TAG, "No latest location available")
                BundleFactory.createErrorBundle(
                    "No location available yet",
                    LocationResponse.ErrorCode.NO_LOCATION_AVAILABLE,
                )
            } else {
                logger.i(TAG, "Returning latest location: ${location.getCoordinatesString()}")
                BundleFactory.createSingleLocationBundle(location)
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
