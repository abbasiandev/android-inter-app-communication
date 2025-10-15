package dev.internetapp.domain.repository

import dev.abbasian.protocol.LocationCommand
import dev.abbasian.protocol.LocationResponse

interface CommandRepository {
    suspend fun sendCommand(command: LocationCommand): LocationResponse
}
