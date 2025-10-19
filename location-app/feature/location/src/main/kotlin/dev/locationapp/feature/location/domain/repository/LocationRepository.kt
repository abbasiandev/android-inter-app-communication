package dev.locationapp.feature.location.domain.repository

import dev.abbasian.protocol.domain.model.LocationData
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    fun getAllLocations(): Flow<List<LocationData>>

    suspend fun getLatestLocation(): LocationData?

    suspend fun saveLocation(location: LocationData)

    suspend fun deleteAllLocations()

    suspend fun getLocationCount(): Int
}
