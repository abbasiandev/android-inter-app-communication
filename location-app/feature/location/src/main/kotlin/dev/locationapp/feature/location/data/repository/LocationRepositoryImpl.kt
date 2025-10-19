@file:Suppress("TooGenericExceptionCaught")

package dev.locationapp.feature.location.data.repository

import dev.abbasian.protocol.domain.logger.AppLogger
import dev.abbasian.protocol.domain.model.LocationData
import dev.locationapp.analytics.LocationAppAnalytics
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
        private val analytics: LocationAppAnalytics,
    ) : LocationRepository {
        override fun getAllLocations(): Flow<List<LocationData>> =
            locationDao
                .getAllLocations()
                .map { entities ->
                    val startTime = System.currentTimeMillis()

                    val locations =
                        entities.mapNotNull { entity ->
                            try {
                                decryptLocation(entity)
                            } catch (e: Exception) {
                                logger.e(TAG, "Failed to decrypt location: ${entity.id}", e)
                                null
                            }
                        }

                    val duration = System.currentTimeMillis() - startTime
                    analytics.trackDatabaseOperation(
                        operation = "query_all",
                        durationMs = duration,
                        recordCount = locations.size,
                    )

                    locations
                }.catch { e ->
                    logger.e(TAG, "Error retrieving locations", e)
                    emit(emptyList())
                }

        override suspend fun getLatestLocation(): LocationData? =
            try {
                val startTime = System.currentTimeMillis()

                val location =
                    locationDao.getLatestLocation()?.let { entity ->
                        try {
                            decryptLocation(entity)
                        } catch (e: Exception) {
                            logger.e(TAG, "Failed to decrypt latest location", e)
                            null
                        }
                    }

                val duration = System.currentTimeMillis() - startTime
                analytics.trackDatabaseOperation(
                    operation = "query_latest",
                    durationMs = duration,
                    recordCount = if (location != null) 1 else 0,
                )

                location
            } catch (e: Exception) {
                logger.e(TAG, "Error getting latest location", e)
                null
            }

        override suspend fun saveLocation(location: LocationData) {
            val startTime = System.currentTimeMillis()

            try {
                val entity = encryptLocation(location)
                locationDao.insertLocation(entity)

                val duration = System.currentTimeMillis() - startTime
                logger.d(TAG, "Location saved successfully: ${location.id} in ${duration}ms")

                analytics.trackDatabaseOperation(
                    operation = "insert",
                    durationMs = duration,
                    recordCount = 1,
                )
            } catch (e: Exception) {
                val duration = System.currentTimeMillis() - startTime
                logger.e(TAG, "Failed to save location", e)

                analytics.trackDatabaseOperation(
                    operation = "insert_failed",
                    durationMs = duration,
                    recordCount = 0,
                )

                throw e
            }
        }

        override suspend fun deleteAllLocations() {
            val startTime = System.currentTimeMillis()

            try {
                val count = getLocationCount()
                locationDao.deleteAll()

                val duration = System.currentTimeMillis() - startTime
                logger.i(TAG, "All locations deleted")

                analytics.trackDatabaseOperation(
                    operation = "delete_all",
                    durationMs = duration,
                    recordCount = count,
                )
            } catch (e: Exception) {
                logger.e(TAG, "Failed to delete locations", e)
                throw e
            }
        }

        override suspend fun getLocationCount(): Int =
            try {
                val startTime = System.currentTimeMillis()
                val count = locationDao.getCount()
                val duration = System.currentTimeMillis() - startTime

                logger.d(TAG, "Location count: $count (${duration}ms)")
                count
            } catch (e: Exception) {
                logger.e(TAG, "Failed to get location count", e)
                0
            }

        private fun encryptLocation(location: LocationData): LocationEntity {
            val startTime = System.currentTimeMillis()

            try {
                val encryptedLat = cryptoManager.encrypt(location.latitude.toString())
                val encryptedLon = cryptoManager.encrypt(location.longitude.toString())

                val duration = System.currentTimeMillis() - startTime
                analytics.trackEncryptionOperation(
                    operation = "encrypt",
                    durationMs = duration,
                    success = true,
                )

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
                val duration = System.currentTimeMillis() - startTime
                logger.e(TAG, "Encryption failed for location: ${location.id}", e)

                analytics.trackEncryptionOperation(
                    operation = "encrypt",
                    durationMs = duration,
                    success = false,
                )

                throw IllegalStateException("Failed to encrypt location data", e)
            }
        }

        private fun decryptLocation(entity: LocationEntity): LocationData {
            val startTime = System.currentTimeMillis()

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

                val duration = System.currentTimeMillis() - startTime
                analytics.trackEncryptionOperation(
                    operation = "decrypt",
                    durationMs = duration,
                    success = true,
                )

                return LocationData(
                    id = entity.id,
                    latitude = latitude,
                    longitude = longitude,
                    accuracy = entity.accuracy,
                    timestamp = entity.timestamp,
                    provider = entity.provider,
                )
            } catch (e: Exception) {
                val duration = System.currentTimeMillis() - startTime
                logger.e(TAG, "Decryption failed for location: ${entity.id}", e)

                analytics.trackEncryptionOperation(
                    operation = "decrypt",
                    durationMs = duration,
                    success = false,
                )

                throw IllegalStateException("Failed to decrypt location data", e)
            }
        }

        companion object {
            private const val TAG = "LocationRepository"
        }
    }
