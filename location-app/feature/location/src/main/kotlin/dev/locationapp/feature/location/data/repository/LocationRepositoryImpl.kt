package dev.locationapp.feature.location.data.repository

import dev.abbasian.protocol.domain.model.LocationData
import dev.locationapp.core.security.data.CryptoManager
import dev.locationapp.core.security.domain.EncryptedData
import dev.locationapp.feature.location.data.local.LocationDao
import dev.locationapp.feature.location.data.local.LocationEntity
import dev.locationapp.feature.location.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocationRepositoryImpl
    @Inject
    constructor(
        private val locationDao: LocationDao,
        private val cryptoManager: CryptoManager,
    ) : LocationRepository {
        override fun getAllLocations(): Flow<List<LocationData>> =
            locationDao.getAllLocations().map { entities ->
                entities.map { decryptLocation(it) }
            }

        @Suppress("ktlint:standard:function-return-type-spacing")
        override suspend fun getLatestLocation():
            LocationData? = locationDao.getLatestLocation()?.let { decryptLocation(it) }

        override suspend fun saveLocation(location: LocationData) {
            val entity = encryptLocation(location)
            locationDao.insertLocation(entity)
        }

        override suspend fun deleteAllLocations() {
            locationDao.deleteAll()
        }

        override suspend fun getLocationCount(): Int = locationDao.getCount()

        private fun encryptLocation(location: LocationData): LocationEntity {
            val encryptedLat = cryptoManager.encrypt(location.latitude.toString())
            val encryptedLon = cryptoManager.encrypt(location.longitude.toString())

            return LocationEntity(
                id = location.id,
                encryptedLatitude = encryptedLat.data,
                encryptedLongitude = encryptedLon.data,
                ivLatitude = encryptedLat.iv,
                ivLongitude = encryptedLon.iv,
                accuracy = location.accuracy,
                timestamp = location.timestamp,
                provider = location.provider,
            )
        }

        private fun decryptLocation(entity: LocationEntity): LocationData {
            val latitude =
                cryptoManager
                    .decrypt(
                        EncryptedData(entity.encryptedLatitude, entity.ivLatitude),
                    ).toDouble()

            val longitude =
                cryptoManager
                    .decrypt(
                        EncryptedData(entity.encryptedLongitude, entity.ivLongitude),
                    ).toDouble()

            return LocationData(
                id = entity.id,
                latitude = latitude,
                longitude = longitude,
                accuracy = entity.accuracy,
                timestamp = entity.timestamp,
                provider = entity.provider,
            )
        }
    }
