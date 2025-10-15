package dev.locationapp.domain.usecase

import dev.abbasian.protocol.LocationData
import dev.locationapp.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllLocationsUseCase @Inject constructor(
    private val repository: LocationRepository
) {
    operator fun invoke(): Flow<List<LocationData>> {
        return repository.getAllLocations()
    }
}