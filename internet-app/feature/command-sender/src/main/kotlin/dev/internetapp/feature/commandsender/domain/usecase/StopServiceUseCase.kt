package dev.internetapp.feature.commandsender.domain.usecase

import dev.abbasian.protocol.domain.logger.AppLogger
import dev.abbasian.protocol.domain.model.CommandResult
import dev.abbasian.protocol.domain.model.LocationCommand
import dev.abbasian.protocol.domain.model.LocationResponse
import dev.internetapp.feature.commandsender.domain.repository.CommandRepository

class StopServiceUseCase(
    private val repository: CommandRepository,
    private val logger: AppLogger,
) {
    suspend operator fun invoke(useRetry: Boolean = true): CommandResult<LocationResponse> {
        logger.d(TAG, "Executing StopServiceUseCase (retry: $useRetry)")
        return if (useRetry) {
            repository.sendCommandWithRetry(LocationCommand.StopService)
        } else {
            repository.sendCommand(LocationCommand.StopService)
        }
    }

    companion object {
        private const val TAG = "StopServiceUseCase"
    }
}
