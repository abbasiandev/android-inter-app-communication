package dev.locationapp.feature.location.domain.usecase

import dev.abbasian.protocol.domain.model.LocationData
import dev.locationapp.feature.location.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllLocationsUseCase
    @Inject
    constructor(
        private val repository: LocationRepository,
    ) {
        operator fun invoke(): Flow<List<LocationData>> = repository.getAllLocations()
    }
