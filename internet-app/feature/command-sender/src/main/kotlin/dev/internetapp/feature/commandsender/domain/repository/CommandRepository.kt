package dev.internetapp.feature.commandsender.domain.repository

import dev.abbasian.protocol.data.constants.ProtocolConstants
import dev.abbasian.protocol.domain.model.CommandResult
import dev.abbasian.protocol.domain.model.LocationCommand
import dev.abbasian.protocol.domain.model.LocationResponse

interface CommandRepository {
    suspend fun sendCommand(command: LocationCommand): CommandResult<LocationResponse>

    suspend fun sendCommandWithRetry(
        command: LocationCommand,
        maxRetries: Int = ProtocolConstants.MAX_RETRY_ATTEMPTS,
    ): CommandResult<LocationResponse>
}
