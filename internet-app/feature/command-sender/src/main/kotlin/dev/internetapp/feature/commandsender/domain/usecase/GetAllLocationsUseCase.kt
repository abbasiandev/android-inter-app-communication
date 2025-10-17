package dev.internetapp.feature.commandsender.domain.usecase

import dev.abbasian.protocol.AppLogger
import dev.abbasian.protocol.LocationCommand
import dev.abbasian.protocol.LocationResponse
import dev.internetapp.feature.commandsender.domain.repository.CommandRepository

class GetAllLocationsUseCase(
    private val repository: CommandRepository,
    private val logger: AppLogger,
) {
    suspend operator fun invoke(): LocationResponse {
        logger.d(TAG, "Executing GetAllLocationsUseCase")
        return repository.sendCommand(LocationCommand.GetAllLocations)
    }

    companion object {
        private const val TAG = "GetAllLocationsUseCase"
    }
}
