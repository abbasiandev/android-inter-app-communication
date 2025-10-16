package dev.locationapp.domain.usecase

import dev.abbasian.protocol.LocationData
import dev.locationapp.domain.repository.LocationRepository
import javax.inject.Inject

class SaveLocationUseCase @Inject constructor(
    private val repository: LocationRepository
) {
    suspend operator fun invoke(location: LocationData) {
        repository.saveLocation(location)
    }
}