package dev.locationapp.feature.location.domain.usecase

import dev.abbasian.protocol.domain.model.LocationData
import dev.locationapp.feature.location.domain.repository.LocationRepository
import javax.inject.Inject

class SaveLocationUseCase
    @Inject
    constructor(
        private val repository: LocationRepository,
    ) {
        suspend operator fun invoke(location: LocationData) {
            repository.saveLocation(location)
        }
    }
