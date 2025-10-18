package dev.locationapp.feature.command.presentation.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import dev.abbasian.protocol.data.constants.ProtocolConstants
import dev.abbasian.protocol.domain.logger.AppLogger
import dev.abbasian.protocol.domain.model.LocationCommand
import dev.abbasian.protocol.domain.model.LocationResponse
import dev.locationapp.feature.command.data.handler.BundleFactory
import dev.locationapp.feature.command.data.handler.CommandHandlers
import dev.locationapp.feature.location.domain.repository.LocationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class LocationCommandProvider : ContentProvider() {
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface LocationCommandProviderEntryPoint {
        fun locationRepository(): LocationRepository

        fun logger(): AppLogger
    }

    private val providerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private lateinit var logger: AppLogger
    private lateinit var commandHandlers: CommandHandlers

    override fun onCreate(): Boolean {
        val appContext = context?.applicationContext ?: return false

        val entryPoint =
            EntryPointAccessors.fromApplication(
                appContext,
                LocationCommandProviderEntryPoint::class.java,
            )

        logger = entryPoint.logger()
        commandHandlers =
            CommandHandlers(
                appContext,
                entryPoint.locationRepository(),
                logger,
                providerScope.coroutineContext,
            )

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
            ProtocolConstants.METHOD_SEND_COMMAND -> handleCommandFromBundle(extras)
            else -> {
                logger.w(TAG, "Unknown method: $method")
                BundleFactory.createErrorBundle(
                    "Unknown method: $method",
                    LocationResponse.ErrorCode.INTERNAL_ERROR,
                )
            }
        }
    }

    private fun handleCommandFromBundle(extras: Bundle?): Bundle {
        if (extras == null) {
            logger.e(TAG, "Received null extras")
            return BundleFactory.createErrorBundle(
                "No command data received",
                LocationResponse.ErrorCode.INTERNAL_ERROR,
            )
        }

        val commandType = extras.getString(ProtocolConstants.EXTRA_COMMAND_TYPE)

        if (commandType == null) {
            logger.e(TAG, "No command type in extras")
            return BundleFactory.createErrorBundle(
                "No command type specified",
                LocationResponse.ErrorCode.INTERNAL_ERROR,
            )
        }

        logger.i(TAG, "Processing command: $commandType")

        val command = parseCommandType(commandType)

        return if (command != null) {
            executeCommand(command)
        } else {
            logger.e(TAG, "Unknown command type: $commandType")
            BundleFactory.createErrorBundle(
                "Unknown command type: $commandType",
                LocationResponse.ErrorCode.INTERNAL_ERROR,
            )
        }
    }

    private fun parseCommandType(commandType: String): LocationCommand? =
        when (commandType) {
            LocationCommand.TYPE_START_SERVICE -> LocationCommand.StartService
            LocationCommand.TYPE_STOP_SERVICE -> LocationCommand.StopService
            LocationCommand.TYPE_GET_ALL_LOCATIONS -> LocationCommand.GetAllLocations
            LocationCommand.TYPE_GET_LATEST_LOCATION -> LocationCommand.GetLatestLocation
            else -> null
        }

    private fun executeCommand(command: LocationCommand): Bundle =
        when (command) {
            is LocationCommand.StartService -> commandHandlers.handleStartService()
            is LocationCommand.StopService -> commandHandlers.handleStopService()
            is LocationCommand.GetAllLocations -> commandHandlers.handleGetAllLocations()
            is LocationCommand.GetLatestLocation -> commandHandlers.handleGetLatestLocation()
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
