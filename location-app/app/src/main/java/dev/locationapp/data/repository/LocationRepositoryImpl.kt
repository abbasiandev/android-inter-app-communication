package dev.locationapp.data.repository

import dev.abbasian.protocol.LocationData
import dev.locationapp.core.security.CryptoManager
import dev.locationapp.core.security.EncryptedData
import dev.locationapp.data.local.LocationDao
import dev.locationapp.data.local.LocationEntity
import dev.locationapp.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(
    private val locationDao: LocationDao,
    private val cryptoManager: CryptoManager
) : LocationRepository {

    override fun getAllLocations(): Flow<List<LocationData>> {
        return locationDao.getAllLocations().map { entities ->
            entities.map { decryptLocation(it) }
        }
    }

    override suspend fun getLatestLocation(): LocationData? {
        return locationDao.getLatestLocation()?.let { decryptLocation(it) }
    }

    override suspend fun saveLocation(location: LocationData) {
        val entity = encryptLocation(location)
        locationDao.insertLocation(entity)
    }

    override suspend fun deleteAllLocations() {
        locationDao.deleteAll()
    }

    override suspend fun getLocationCount(): Int {
        return locationDao.getCount()
    }

    private fun encryptLocation(location: LocationData): LocationEntity {
        val encryptedLat = cryptoManager.encrypt(location.latitude.toString())
        val encryptedLon = cryptoManager.encrypt(location.longitude.toString())

        return LocationEntity(
            id = location.id,
            encryptedLatitude = encryptedLat.data,
            encryptedLongitude = encryptedLon.data,
            accuracy = location.accuracy,
            timestamp = location.timestamp,
            provider = location.provider
        )
    }

    private fun decryptLocation(entity: LocationEntity): LocationData {
        val latitude = cryptoManager.decrypt(
            EncryptedData(entity.encryptedLatitude)
        ).toDouble()

        val longitude = cryptoManager.decrypt(
            EncryptedData(entity.encryptedLongitude)
        ).toDouble()

        return LocationData(
            id = entity.id,
            latitude = latitude,
            longitude = longitude,
            accuracy = entity.accuracy,
            timestamp = entity.timestamp,
            provider = entity.provider
        )
    }
}