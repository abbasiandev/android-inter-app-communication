package dev.internetapp.feature.commandsender.domain.usecase

import dev.abbasian.protocol.domain.logger.AppLogger
import dev.abbasian.protocol.domain.model.CommandResult
import dev.abbasian.protocol.domain.model.LocationCommand
import dev.abbasian.protocol.domain.model.LocationResponse
import dev.internetapp.feature.commandsender.domain.repository.CommandRepository

class GetAllLocationsUseCase(
    private val repository: CommandRepository,
    private val logger: AppLogger,
) {
    suspend operator fun invoke(useRetry: Boolean = true): CommandResult<LocationResponse> {
        logger.d(TAG, "Executing GetAllLocationsUseCase (retry: $useRetry)")
        return if (useRetry) {
            repository.sendCommandWithRetry(LocationCommand.GetAllLocations)
        } else {
            repository.sendCommand(LocationCommand.GetAllLocations)
        }
    }

    companion object {
        private const val TAG = "GetAllLocationsUseCase"
    }
}
