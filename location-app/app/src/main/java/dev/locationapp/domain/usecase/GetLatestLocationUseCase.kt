package dev.locationapp.domain.usecase

import dev.abbasian.protocol.LocationData
import dev.locationapp.domain.repository.LocationRepository
import javax.inject.Inject

class GetLatestLocationUseCase
    @Inject
    constructor(
        private val repository: LocationRepository,
    ) {
        suspend operator fun invoke(): LocationData? {
            return repository.getLatestLocation()
        }
    }
