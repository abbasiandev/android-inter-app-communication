package dev.locationapp.feature.location.data.repository

import dev.abbasian.protocol.domain.logger.AppLogger
import dev.abbasian.protocol.domain.model.LocationData
import dev.locationapp.core.security.data.CryptoManager
import dev.locationapp.core.security.domain.EncryptedData
import dev.locationapp.feature.location.data.local.LocationDao
import dev.locationapp.feature.location.data.local.LocationEntity
import dev.locationapp.feature.location.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocationRepositoryImpl
    @Inject
    constructor(
        private val locationDao: LocationDao,
        private val cryptoManager: CryptoManager,
        private val logger: AppLogger,
    ) : LocationRepository {
        override fun getAllLocations(): Flow<List<LocationData>> =
            locationDao
                .getAllLocations()
                .map { entities ->
                    entities.mapNotNull { entity ->
                        try {
                            decryptLocation(entity)
                        } catch (e: Exception) {
                            logger.e(TAG, "Failed to decrypt location: ${entity.id}", e)
                            null
                        }
                    }
                }.catch { e ->
                    logger.e(TAG, "Error retrieving locations", e)
                    emit(emptyList())
                }

        override suspend fun getLatestLocation(): LocationData? =
            try {
                locationDao.getLatestLocation()?.let { entity ->
                    try {
                        decryptLocation(entity)
                    } catch (e: Exception) {
                        logger.e(TAG, "Failed to decrypt latest location", e)
                        null
                    }
                }
            } catch (e: Exception) {
                logger.e(TAG, "Error getting latest location", e)
                null
            }

        override suspend fun saveLocation(location: LocationData) {
            try {
                val entity = encryptLocation(location)
                locationDao.insertLocation(entity)
                logger.d(TAG, "Location saved successfully: ${location.id}")
            } catch (e: Exception) {
                logger.e(TAG, "Failed to save location", e)
                throw e
            }
        }

        override suspend fun deleteAllLocations() {
            try {
                locationDao.deleteAll()
                logger.i(TAG, "All locations deleted")
            } catch (e: Exception) {
                logger.e(TAG, "Failed to delete locations", e)
                throw e
            }
        }

        override suspend fun getLocationCount(): Int =
            try {
                locationDao.getCount().also {
                    logger.d(TAG, "Location count: $it")
                }
            } catch (e: Exception) {
                logger.e(TAG, "Failed to get location count", e)
                0
            }

        private fun encryptLocation(location: LocationData): LocationEntity {
            try {
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
            } catch (e: Exception) {
                logger.e(TAG, "Encryption failed for location: ${location.id}", e)
                throw IllegalStateException("Failed to encrypt location data", e)
            }
        }

        private fun decryptLocation(entity: LocationEntity): LocationData {
            try {
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
            } catch (e: Exception) {
                logger.e(TAG, "Decryption failed for location: ${entity.id}", e)
                throw IllegalStateException("Failed to decrypt location data", e)
            }
        }

        companion object {
            private const val TAG = "LocationRepository"
        }
    }
