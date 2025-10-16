package dev.internetapp.domain.usecase

import dev.abbasian.protocol.AppLogger
import dev.abbasian.protocol.LocationCommand
import dev.abbasian.protocol.LocationResponse
import dev.internetapp.domain.repository.CommandRepository

class StartServiceUseCase(
    private val repository: CommandRepository,
    private val logger: AppLogger
) {
    suspend operator fun invoke(): LocationResponse {
        logger.d(TAG, "Executing StartServiceUseCase")
        return repository.sendCommand(LocationCommand.StartService)
    }

    companion object {
        private const val TAG = "StartServiceUseCase"
    }
}