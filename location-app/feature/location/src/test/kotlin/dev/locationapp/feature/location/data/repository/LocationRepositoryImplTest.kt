package dev.locationapp.feature.location.data.repository

import app.cash.turbine.test
import dev.abbasian.protocol.domain.logger.AppLogger
import dev.abbasian.protocol.domain.model.LocationData
import dev.locationapp.analytics.LocationAppAnalytics
import dev.locationapp.core.security.data.CryptoManager
import dev.locationapp.core.security.domain.EncryptedData
import dev.locationapp.feature.location.data.local.LocationDao
import dev.locationapp.feature.location.data.local.LocationEntity
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class LocationRepositoryImplTest {
    private lateinit var repository: LocationRepositoryImpl
    private lateinit var locationDao: LocationDao
    private lateinit var cryptoManager: CryptoManager
    private lateinit var logger: AppLogger
    private lateinit var analytics: LocationAppAnalytics

    @Before
    fun setup() {
        locationDao = mockk(relaxed = true)
        cryptoManager = mockk(relaxed = true)
        logger = mockk(relaxed = true)
        analytics = mockk(relaxed = true)
        repository = LocationRepositoryImpl(locationDao, cryptoManager, logger, analytics)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `saveLocation encrypts and stores location`() =
        runTest {
            val location =
                LocationData(
                    id = "test-id",
                    latitude = 48.8566,
                    longitude = 2.3522,
                    accuracy = 10f,
                    timestamp = System.currentTimeMillis(),
                    provider = "gps",
                )

            val encryptedLat = EncryptedData(byteArrayOf(1, 2, 3), byteArrayOf(4, 5, 6))
            val encryptedLon = EncryptedData(byteArrayOf(7, 8, 9), byteArrayOf(10, 11, 12))

            every { cryptoManager.encrypt(location.latitude.toString()) } returns encryptedLat
            every { cryptoManager.encrypt(location.longitude.toString()) } returns encryptedLon
            coEvery { locationDao.insertLocation(any()) } just Runs

            repository.saveLocation(location)

            coVerify(exactly = 1) { locationDao.insertLocation(any()) }
            verify(exactly = 1) { cryptoManager.encrypt(location.latitude.toString()) }
            verify(exactly = 1) { cryptoManager.encrypt(location.longitude.toString()) }
        }

    @Test
    fun `getAllLocations decrypts and returns locations`() =
        runTest {
            val entity =
                LocationEntity(
                    id = "test-id",
                    encryptedLatitude = byteArrayOf(1, 2, 3),
                    encryptedLongitude = byteArrayOf(7, 8, 9),
                    ivLatitude = byteArrayOf(4, 5, 6),
                    ivLongitude = byteArrayOf(10, 11, 12),
                    accuracy = 10f,
                    timestamp = System.currentTimeMillis(),
                    provider = "gps",
                )

            every { locationDao.getAllLocations() } returns flowOf(listOf(entity))

            every {
                cryptoManager.decrypt(
                    match { it.data.contentEquals(byteArrayOf(1, 2, 3)) },
                )
            } returns "48.8566"

            every {
                cryptoManager.decrypt(
                    match { it.data.contentEquals(byteArrayOf(7, 8, 9)) },
                )
            } returns "2.3522"

            repository.getAllLocations().test {
                val locations = awaitItem()
                Assert.assertEquals(1, locations.size)
                Assert.assertEquals(48.8566, locations[0].latitude, 0.0001)
                Assert.assertEquals(2.3522, locations[0].longitude, 0.0001)
                awaitComplete()
            }
        }

    @Test
    fun `getLatestLocation returns null when no data`() =
        runTest {
            coEvery { locationDao.getLatestLocation() } returns null

            val result = repository.getLatestLocation()

            Assert.assertNull(result)
        }
}
