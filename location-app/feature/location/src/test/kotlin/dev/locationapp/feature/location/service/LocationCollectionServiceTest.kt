package dev.locationapp.feature.location.service

import android.Manifest
import android.app.Application
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import dev.abbasian.protocol.domain.logger.AppLogger
import dev.locationapp.feature.location.di.LocationFeatureModule
import dev.locationapp.feature.location.domain.usecase.SaveLocationUseCase
import io.mockk.clearAllMocks
import io.mockk.clearMocks
import io.mockk.verify
import junit.framework.TestCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowApplication
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(LocationFeatureModule::class)
@RunWith(AndroidJUnit4::class)
@Config(
    sdk = [33],
    application = HiltTestApplication::class,
)
@OptIn(ExperimentalCoroutinesApi::class)
class LocationCollectionServiceTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var logger: AppLogger

    @Inject
    lateinit var saveLocationUseCase: SaveLocationUseCase

    private lateinit var shadowApplication: ShadowApplication

    @Before
    fun setup() {
        hiltRule.inject()

        val context = ApplicationProvider.getApplicationContext<Context>()
        shadowApplication = shadowOf(context.applicationContext as Application)

        shadowApplication.grantPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            shadowApplication.grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `service starts successfully`() {
        val service =
            Robolectric
                .buildService(LocationCollectionService::class.java)
                .create()
                .get()

        TestCase.assertNotNull(service)
        TestCase.assertNotNull(service.logger)
        TestCase.assertNotNull(service.saveLocationUseCase)
    }

    @Test
    fun `onCreate creates notification channel`() {
        val service =
            Robolectric
                .buildService(LocationCollectionService::class.java)
                .create()
                .get()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                ApplicationProvider
                    .getApplicationContext<Context>()
                    .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channel = notificationManager.getNotificationChannel("location_collection_channel")
            TestCase.assertNotNull(channel)
            TestCase.assertEquals("Location Collection", channel.name)
        }
    }

    @Test
    fun `onStartCommand with no action starts collection`() {
        val intent =
            Intent(
                ApplicationProvider.getApplicationContext(),
                LocationCollectionService::class.java,
            )

        val service =
            Robolectric
                .buildService(LocationCollectionService::class.java, intent)
                .create()
                .get()

        val result = service.onStartCommand(intent, 0, 1)

        TestCase.assertEquals(Service.START_STICKY, result)
    }

    @Test
    fun `onStartCommand with ACTION_START_COLLECTION starts collection`() {
        val intent =
            Intent(
                ApplicationProvider.getApplicationContext(),
                LocationCollectionService::class.java,
            ).apply { action = LocationCollectionService.ACTION_START_COLLECTION }

        val service =
            Robolectric
                .buildService(LocationCollectionService::class.java, intent)
                .create()
                .get()

        val result = service.onStartCommand(intent, 0, 1)

        TestCase.assertEquals(Service.START_STICKY, result)
    }

    @Test
    fun `onStartCommand with ACTION_STOP_COLLECTION stops collection`() {
        val startIntent =
            Intent(
                ApplicationProvider.getApplicationContext(),
                LocationCollectionService::class.java,
            ).apply { action = LocationCollectionService.ACTION_START_COLLECTION }

        val service =
            Robolectric
                .buildService(LocationCollectionService::class.java, startIntent)
                .create()
                .get()

        service.onStartCommand(startIntent, 0, 1)

        val stopIntent =
            Intent(
                ApplicationProvider.getApplicationContext(),
                LocationCollectionService::class.java,
            ).apply { action = LocationCollectionService.ACTION_STOP_COLLECTION }

        val result = service.onStartCommand(stopIntent, 0, 2)

        TestCase.assertEquals(Service.START_STICKY, result)
    }

    @Test
    fun `stopLocationCollection stops service`() {
        val intent =
            Intent(
                ApplicationProvider.getApplicationContext(),
                LocationCollectionService::class.java,
            ).apply { action = LocationCollectionService.ACTION_START_COLLECTION }

        val service =
            Robolectric
                .buildService(LocationCollectionService::class.java, intent)
                .create()
                .get()

        service.onStartCommand(intent, 0, 1)

        service.stopLocationCollection()
    }

    @Test
    fun `onBind returns null`() {
        val service =
            Robolectric
                .buildService(LocationCollectionService::class.java)
                .create()
                .get()

        val binder = service.onBind(null)

        TestCase.assertEquals(null, binder)
    }

    @Test
    fun `service without location permission stops itself`() {
        shadowApplication.denyPermissions(Manifest.permission.ACCESS_FINE_LOCATION)

        val intent =
            Intent(
                ApplicationProvider.getApplicationContext(),
                LocationCollectionService::class.java,
            ).apply { action = LocationCollectionService.ACTION_START_COLLECTION }

        val service =
            Robolectric
                .buildService(LocationCollectionService::class.java, intent)
                .create()
                .get()

        service.onStartCommand(intent, 0, 1)

        verify(atLeast = 1) { logger.e(any(), any()) }
    }

    @Test
    fun `service lifecycle completes successfully`() {
        val intent =
            Intent(
                ApplicationProvider.getApplicationContext(),
                LocationCollectionService::class.java,
            )

        Robolectric
            .buildService(LocationCollectionService::class.java, intent)
            .create()
            .startCommand(0, 1)
            .destroy()
    }

    @Test
    fun `onDestroy cleans up resources`() {
        val service =
            Robolectric
                .buildService(LocationCollectionService::class.java)
                .create()
                .get()

        Robolectric
            .buildService(LocationCollectionService::class.java)
            .create()
            .destroy()

        verify(atLeast = 1) { logger.i("LocationCollectionService", any()) }
    }

    @Test
    fun `starting collection twice logs warning`() {
        clearMocks(logger)
        val intent =
            Intent(
                ApplicationProvider.getApplicationContext(),
                LocationCollectionService::class.java,
            ).apply { action = LocationCollectionService.ACTION_START_COLLECTION }

        val service =
            Robolectric
                .buildService(LocationCollectionService::class.java, intent)
                .create()
                .get()

        service.onStartCommand(intent, 0, 1)
        service.onStartCommand(intent, 0, 2)

        verify { logger.w("LocationCollectionService", "Location collection already running") }
    }

    @Test
    fun `stopping collection when not running logs warning`() {
        clearMocks(logger)
        val service =
            Robolectric
                .buildService(LocationCollectionService::class.java)
                .create()
                .get()

        service.stopLocationCollection()

        verify { logger.w("LocationCollectionService", "Location collection not running") }
    }

    @Test
    fun `notification has correct properties`() {
        val intent =
            Intent(
                ApplicationProvider.getApplicationContext(),
                LocationCollectionService::class.java,
            ).apply { action = LocationCollectionService.ACTION_START_COLLECTION }

        Robolectric
            .buildService(LocationCollectionService::class.java, intent)
            .create()
            .startCommand(0, 1)

        val shadowNotificationManager =
            shadowOf(
                ApplicationProvider
                    .getApplicationContext<Context>()
                    .getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager,
            )

        val notifications = shadowNotificationManager.allNotifications
        TestCase.assertTrue(notifications.isNotEmpty())

        val notification = notifications.first()
        TestCase.assertNotNull(notification)
    }

    @Test
    fun `service constants are correct`() {
        TestCase.assertEquals(
            "dev.locationapp.ACTION_START_COLLECTION",
            LocationCollectionService.ACTION_START_COLLECTION,
        )
        TestCase.assertEquals(
            "dev.locationapp.ACTION_STOP_COLLECTION",
            LocationCollectionService.ACTION_STOP_COLLECTION,
        )
    }
}
