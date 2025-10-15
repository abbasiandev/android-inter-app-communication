package dev.internetapp.domain.usecase

import dev.abbasian.protocol.AppLogger
import dev.abbasian.protocol.LocationCommand
import dev.abbasian.protocol.LocationResponse
import dev.internetapp.domain.repository.CommandRepository

class StopServiceUseCase(
    private val repository: CommandRepository,
    private val logger: AppLogger
) {
    suspend operator fun invoke(): LocationResponse {
        logger.d(TAG, "Executing StopServiceUseCase")
        return repository.sendCommand(LocationCommand.StopService)
    }

    companion object {
        private const val TAG = "StopServiceUseCase"
    }
}