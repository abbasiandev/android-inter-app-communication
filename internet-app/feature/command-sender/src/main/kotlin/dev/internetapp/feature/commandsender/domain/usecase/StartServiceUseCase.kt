package dev.internetapp.feature.commandsender.domain.usecase

import dev.abbasian.protocol.domain.logger.AppLogger
import dev.abbasian.protocol.domain.model.LocationCommand
import dev.abbasian.protocol.domain.model.LocationResponse
import dev.internetapp.feature.commandsender.domain.repository.CommandRepository

class StartServiceUseCase(
    private val repository: CommandRepository,
    private val logger: AppLogger,
) {
    suspend operator fun invoke(): LocationResponse {
        logger.d(TAG, "Executing StartServiceUseCase")
        return repository.sendCommand(LocationCommand.StartService)
    }

    companion object {
        private const val TAG = "StartServiceUseCase"
    }
}
