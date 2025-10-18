package dev.internetapp.feature.commandsender.domain.usecase

import dev.abbasian.protocol.domain.logger.AppLogger
import dev.abbasian.protocol.domain.model.LocationCommand
import dev.abbasian.protocol.domain.model.LocationResponse
import dev.internetapp.feature.commandsender.domain.repository.CommandRepository

class StopServiceUseCase(
    private val repository: CommandRepository,
    private val logger: AppLogger,
) {
    suspend operator fun invoke(): LocationResponse {
        logger.d(TAG, "Executing StopServiceUseCase")
        return repository.sendCommand(LocationCommand.StopService)
    }

    companion object {
        private const val TAG = "StopServiceUseCase"
    }
}
