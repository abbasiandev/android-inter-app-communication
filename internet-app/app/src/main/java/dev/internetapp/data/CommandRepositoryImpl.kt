package dev.internetapp.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import dev.abbasian.protocol.AppLogger
import dev.abbasian.protocol.LocationCommand
import dev.abbasian.protocol.LocationData
import dev.abbasian.protocol.LocationResponse
import dev.abbasian.protocol.ProtocolConstants
import dev.internetapp.domain.repository.CommandRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class CommandRepositoryImpl(
    private val context: Context,
    private val logger: AppLogger,
) : CommandRepository {
    private val contentResolver: ContentResolver = context.contentResolver

    override suspend fun sendCommand(command: LocationCommand): LocationResponse =
        withContext(Dispatchers.IO) {
            try {
                logger.i(TAG, "Sending command: ${command.type}")

                val uri = Uri.parse("content://${ProtocolConstants.AUTHORITY}")
                val extras =
                    Bundle().apply {
                        putString(ProtocolConstants.EXTRA_COMMAND_TYPE, command.type)
                    }

                val response =
                    withTimeout(ProtocolConstants.COMMAND_TIMEOUT_MS) {
                        contentResolver.call(
                            uri,
                            ProtocolConstants.METHOD_SEND_COMMAND,
                            null,
                            extras,
                        )
                    }

                parseResponse(response)
            } catch (e: IllegalArgumentException) {
                logger.e(TAG, "ContentProvider not found", e)
                createProviderNotFoundError()
            } catch (e: TimeoutCancellationException) {
                logger.e(TAG, "Command timeout", e)
                createTimeoutError()
            } catch (e: SecurityException) {
                logger.e(TAG, "Permission denied", e)
                createPermissionDeniedError()
            }
        }

    private fun createProviderNotFoundError() =
        LocationResponse.Error(
            "Cannot connect to Location App. Ensure Location App is installed " +
                "and both apps are signed with the same key.",
            LocationResponse.ErrorCode.COMMUNICATION_ERROR,
        )

    private fun createTimeoutError() =
        LocationResponse.Error(
            "Command timeout after ${ProtocolConstants.COMMAND_TIMEOUT_MS}ms",
            LocationResponse.ErrorCode.COMMUNICATION_ERROR,
        )

    private fun createPermissionDeniedError() =
        LocationResponse.Error(
            "Permission denied. Ensure both apps are signed with the same key.",
            LocationResponse.ErrorCode.PERMISSION_DENIED,
        )

    private fun parseResponse(bundle: Bundle?): LocationResponse {
        if (bundle == null) {
            logger.w(TAG, "Received null response")
            return createNullResponseError()
        }

        bundle.classLoader = LocationData::class.java.classLoader
        val responseType = bundle.getString(ProtocolConstants.EXTRA_RESPONSE_TYPE)
        logger.d(TAG, "Response type: $responseType")

        return when (responseType) {
            ProtocolConstants.RESPONSE_TYPE_SUCCESS -> parseSuccessResponse(bundle)
            ProtocolConstants.RESPONSE_TYPE_LOCATION_LIST -> parseLocationListResponse(bundle)
            ProtocolConstants.RESPONSE_TYPE_SINGLE_LOCATION -> parseSingleLocationResponse(bundle)
            ProtocolConstants.RESPONSE_TYPE_ERROR -> parseErrorResponse(bundle)
            else -> createUnknownResponseError(responseType)
        }
    }

    private fun createNullResponseError() =
        LocationResponse.Error(
            "No response received from Location App",
            LocationResponse.ErrorCode.COMMUNICATION_ERROR,
        )

    private fun parseSuccessResponse(bundle: Bundle): LocationResponse {
        val message = bundle.getString(ProtocolConstants.EXTRA_RESPONSE_DATA) ?: "Success"
        logger.i(TAG, "Success response: $message")
        return LocationResponse.Success(message)
    }

    private fun parseLocationListResponse(bundle: Bundle): LocationResponse {
        val locations = extractLocationList(bundle)
        logger.i(TAG, "Received ${locations.size} locations")
        return LocationResponse.LocationList(locations)
    }

    private fun extractLocationList(bundle: Bundle): List<LocationData> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle.getParcelableArrayList(
                ProtocolConstants.EXTRA_LOCATION_LIST,
                LocationData::class.java,
            )
        } else {
            @Suppress("DEPRECATION")
            bundle.getParcelableArrayList<LocationData>(
                ProtocolConstants.EXTRA_LOCATION_LIST,
            )
        } ?: emptyList()

    private fun parseSingleLocationResponse(bundle: Bundle): LocationResponse {
        val location = extractSingleLocation(bundle)
        logger.i(TAG, "Received single location: ${location?.getCoordinatesString()}")
        return LocationResponse.SingleLocation(location)
    }

    private fun extractSingleLocation(bundle: Bundle): LocationData? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle.getParcelable(
                ProtocolConstants.EXTRA_LOCATION,
                LocationData::class.java,
            )
        } else {
            @Suppress("DEPRECATION")
            bundle.getParcelable<LocationData>(
                ProtocolConstants.EXTRA_LOCATION,
            )
        }

    private fun parseErrorResponse(bundle: Bundle): LocationResponse {
        val message = bundle.getString(ProtocolConstants.EXTRA_ERROR_MESSAGE) ?: "Unknown error"
        val codeStr = bundle.getString(ProtocolConstants.EXTRA_ERROR_CODE)
        val code = parseErrorCode(codeStr)
        logger.w(TAG, "Error response: $message (code: $code)")
        return LocationResponse.Error(message, code)
    }

    private fun parseErrorCode(codeStr: String?): LocationResponse.ErrorCode =
        if (codeStr != null) {
            LocationResponse.ErrorCode.fromString(codeStr)
        } else {
            LocationResponse.ErrorCode.INTERNAL_ERROR
        }

    private fun createUnknownResponseError(responseType: String?) =
        LocationResponse.Error(
            "Unknown response type: $responseType",
            LocationResponse.ErrorCode.COMMUNICATION_ERROR,
        )

    companion object {
        private const val TAG = "CommandRepository"
    }
}
