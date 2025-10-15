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
    private val logger: AppLogger
) : CommandRepository {

    private val contentResolver: ContentResolver = context.contentResolver

    override suspend fun sendCommand(command: LocationCommand): LocationResponse {
        return withContext(Dispatchers.IO) {
            try {
                logger.i(TAG, "Sending command: ${command.type}")

                val uri = Uri.parse("content://${ProtocolConstants.AUTHORITY}")
                val extras = Bundle().apply {
                    putString(ProtocolConstants.EXTRA_COMMAND_TYPE, command.type)
                }

                val response = withTimeout(ProtocolConstants.COMMAND_TIMEOUT_MS) {
                    contentResolver.call(
                        uri,
                        ProtocolConstants.METHOD_SEND_COMMAND,
                        null,
                        extras
                    )
                }

                parseResponse(response)
            } catch (e: IllegalArgumentException) {
                logger.e(TAG, "ContentProvider not found", e)
                LocationResponse.Error(
                    "Cannot connect to Location App. Ensure Location App is installed and both apps are signed with the same key.",
                    LocationResponse.ErrorCode.COMMUNICATION_ERROR
                )
            } catch (e: TimeoutCancellationException) {
                logger.e(TAG, "Command timeout", e)
                LocationResponse.Error(
                    "Command timeout after ${ProtocolConstants.COMMAND_TIMEOUT_MS}ms",
                    LocationResponse.ErrorCode.COMMUNICATION_ERROR
                )
            } catch (e: SecurityException) {
                logger.e(TAG, "Permission denied", e)
                LocationResponse.Error(
                    "Permission denied. Ensure both apps are signed with the same key.",
                    LocationResponse.ErrorCode.PERMISSION_DENIED
                )
            } catch (e: Exception) {
                logger.e(TAG, "Failed to send command", e)
                LocationResponse.Error(
                    "Communication failed: ${e.message}",
                    LocationResponse.ErrorCode.COMMUNICATION_ERROR
                )
            }
        }
    }

    private fun parseResponse(bundle: Bundle?): LocationResponse {
        if (bundle == null) {
            logger.w(TAG, "Received null response")
            return LocationResponse.Error(
                "No response received from Location App",
                LocationResponse.ErrorCode.COMMUNICATION_ERROR
            )
        }

        // set classLoader so android can find LocationData class
        bundle.classLoader = LocationData::class.java.classLoader

        val responseType = bundle.getString(ProtocolConstants.EXTRA_RESPONSE_TYPE)
        logger.d(TAG, "Response type: $responseType")

        return when (responseType) {
            ProtocolConstants.RESPONSE_TYPE_SUCCESS -> {
                val message = bundle.getString(ProtocolConstants.EXTRA_RESPONSE_DATA)
                    ?: "Success"
                logger.i(TAG, "Success response: $message")
                LocationResponse.Success(message)
            }

            ProtocolConstants.RESPONSE_TYPE_LOCATION_LIST -> {
                val locations = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    bundle.getParcelableArrayList(
                        ProtocolConstants.EXTRA_LOCATION_LIST,
                        LocationData::class.java
                    )
                } else {
                    @Suppress("DEPRECATION")
                    bundle.getParcelableArrayList<LocationData>(
                        ProtocolConstants.EXTRA_LOCATION_LIST
                    )
                } ?: emptyList()
                logger.i(TAG, "Received ${locations.size} locations")
                LocationResponse.LocationList(locations)
            }

            ProtocolConstants.RESPONSE_TYPE_SINGLE_LOCATION -> {
                val location = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    bundle.getParcelable(
                        ProtocolConstants.EXTRA_LOCATION,
                        LocationData::class.java
                    )
                } else {
                    @Suppress("DEPRECATION")
                    bundle.getParcelable<LocationData>(
                        ProtocolConstants.EXTRA_LOCATION
                    )
                }
                logger.i(TAG, "Received single location: ${location?.getCoordinatesString()}")
                LocationResponse.SingleLocation(location)
            }

            ProtocolConstants.RESPONSE_TYPE_ERROR -> {
                val message = bundle.getString(ProtocolConstants.EXTRA_ERROR_MESSAGE)
                    ?: "Unknown error"
                val codeStr = bundle.getString(ProtocolConstants.EXTRA_ERROR_CODE)
                val code = try {
                    LocationResponse.ErrorCode.fromString(codeStr ?: "")
                } catch (e: Exception) {
                    LocationResponse.ErrorCode.INTERNAL_ERROR
                }
                logger.w(TAG, "Error response: $message (code: $code)")
                LocationResponse.Error(message, code)
            }

            else -> {
                logger.e(TAG, "Unknown response type: $responseType")
                LocationResponse.Error(
                    "Unknown response type: $responseType",
                    LocationResponse.ErrorCode.COMMUNICATION_ERROR
                )
            }
        }
    }

    companion object {
        private const val TAG = "CommandRepository"
    }
}