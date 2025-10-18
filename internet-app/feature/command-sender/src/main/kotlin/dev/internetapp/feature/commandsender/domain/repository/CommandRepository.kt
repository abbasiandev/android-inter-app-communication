package dev.internetapp.feature.commandsender.domain.repository

import dev.abbasian.protocol.domain.model.LocationCommand
import dev.abbasian.protocol.domain.model.LocationResponse

interface CommandRepository {
    suspend fun sendCommand(command: LocationCommand): LocationResponse
}
