package dev.locationapp.feature.location.service

import android.Manifest
import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import dev.abbasian.protocol.data.constants.ProtocolConstants
import dev.abbasian.protocol.domain.logger.AppLogger
import dev.abbasian.protocol.domain.model.LocationData
import dev.locationapp.feature.location.domain.usecase.SaveLocationUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class LocationCollectionService : Service() {
    @Inject
    lateinit var saveLocationUseCase: SaveLocationUseCase

    @Inject
    lateinit var logger: AppLogger

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var notificationManager: NotificationManager

    private var isCollecting = false
    private var retryCount = 0
    private val maxRetries = 3

    override fun onCreate() {
        super.onCreate()
        logger.i(TAG, "Service created")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        setupLocationCallback()
        createNotificationChannel()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        logger.i(TAG, "Service started with intent: ${intent?.action}")

        when (intent?.action) {
            ACTION_START_COLLECTION -> startLocationCollection()
            ACTION_STOP_COLLECTION -> stopLocationCollection()
            else -> startLocationCollection()
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startLocationCollection() {
        if (isCollecting) {
            logger.w(TAG, "Location collection already running")
            return
        }

        logger.i(TAG, "Starting location collection")
        startForeground(ProtocolConstants.SERVICE_NOTIFICATION_ID, createNotification())
        startLocationUpdates()
        isCollecting = true
    }

    fun stopLocationCollection() {
        if (!isCollecting) {
            logger.w(TAG, "Location collection not running")
            return
        }

        logger.i(TAG, "Stopping location collection")
        stopLocationUpdates()
        isCollecting = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun setupLocationCallback() {
        locationCallback =
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.lastLocation?.let { location ->
                        logger.d(
                            TAG,
                            "Location received: ${location.latitude}, ${location.longitude}",
                        )
                        saveLocation(location)
                        retryCount = 0
                    }
                }

                override fun onLocationAvailability(availability: LocationAvailability) {
                    logger.d(TAG, "Location availability: ${availability.isLocationAvailable}")
                    if (!availability.isLocationAvailable) {
                        handleLocationUnavailable()
                    }
                }
            }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            logger.e(TAG, "Location permission not granted")
            handlePermissionError()
            return
        }

        val locationRequest =
            LocationRequest
                .Builder(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    ProtocolConstants.LOCATION_UPDATE_INTERVAL_MS,
                ).apply {
                    setMinUpdateIntervalMillis(ProtocolConstants.LOCATION_UPDATE_INTERVAL_MS / 2)
                    setWaitForAccurateLocation(true)
                    setMaxUpdateDelayMillis(ProtocolConstants.LOCATION_UPDATE_INTERVAL_MS * 2)
                    setMinUpdateDistanceMeters(15f)
                }.build()

        fusedLocationClient
            .requestLocationUpdates(
                locationRequest,
                locationCallback,
                null,
            ).addOnSuccessListener {
                logger.i(TAG, "Location updates started successfully")
                updateNotification("Location tracking active")
            }.addOnFailureListener { exception ->
                logger.e(TAG, "Failed to start location updates", exception)
                handleLocationUpdateError(exception)
            }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        logger.i(TAG, "Location updates stopped")
    }

    private fun saveLocation(location: Location) {
        serviceScope.launch {
            try {
                val locationData =
                    LocationData(
                        id = UUID.randomUUID().toString(),
                        latitude = location.latitude,
                        longitude = location.longitude,
                        accuracy = location.accuracy,
                        timestamp = location.time,
                        provider = location.provider ?: "unknown",
                    )

                saveLocationUseCase(locationData)
                logger.d(TAG, "Location saved successfully")
            } catch (e: IllegalArgumentException) {
                logger.e(TAG, "Invalid location data", e)
                handleSaveError(e)
            } catch (e: IllegalStateException) {
                logger.e(TAG, "Error saving location", e)
                handleSaveError(e)
            } catch (e: Exception) {
                logger.e(TAG, "Unexpected error saving location", e)
                handleSaveError(e)
            }
        }
    }

    private fun handlePermissionError() {
        showErrorNotification(
            "Location Permission Denied",
            "Please grant location permission to continue",
        )
        stopLocationCollection()
    }

    private fun handleLocationUnavailable() {
        logger.w(TAG, "Location unavailable")
        if (retryCount < maxRetries) {
            scheduleRetry()
        } else {
            showErrorNotification(
                "Location Unavailable",
                "Unable to access location services after $maxRetries attempts",
            )
        }
    }

    private fun handleLocationUpdateError(exception: Exception) {
        logger.e(TAG, "Location update error", exception)

        when (exception) {
            is SecurityException -> {
                handlePermissionError()
            }

            else -> {
                if (retryCount < maxRetries) {
                    scheduleRetry()
                } else {
                    showErrorNotification(
                        "Location Service Error",
                        exception.message ?: "Failed to get location updates",
                    )
                }
            }
        }
    }

    private fun handleSaveError(exception: Exception) {
        logger.e(TAG, "Save error", exception)
        showErrorNotification(
            "Database Error",
            "Failed to save location: ${exception.message}",
        )
    }

    private fun scheduleRetry() {
        retryCount++
        logger.i(TAG, "Scheduling retry $retryCount/$maxRetries")

        serviceScope.launch {
            val delayMs = (1000L * retryCount).coerceAtMost(30_000L)
            updateNotification("Retrying location updates ($retryCount/$maxRetries)...")
            delay(delayMs)

            if (isCollecting) {
                logger.i(TAG, "Retrying location updates")
                startLocationUpdates()
            }
        }
    }

    private fun showErrorNotification(
        title: String,
        message: String,
    ) {
        val notification =
            NotificationCompat
                .Builder(this, ERROR_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_dialog_alert)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

        notificationManager.notify(ERROR_NOTIFICATION_ID, notification)
    }

    private fun updateNotification(contentText: String) {
        val notification = createNotification(contentText)
        notificationManager.notify(ProtocolConstants.SERVICE_NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel =
                NotificationChannel(
                    CHANNEL_ID,
                    "Location Collection",
                    NotificationManager.IMPORTANCE_LOW,
                ).apply {
                    description = "Notification for location collection service"
                    setShowBadge(false)
                }

            val errorChannel =
                NotificationChannel(
                    ERROR_CHANNEL_ID,
                    "Location Errors",
                    NotificationManager.IMPORTANCE_HIGH,
                ).apply {
                    description = "Notifications for location service errors"
                    setShowBadge(true)
                }

            notificationManager.createNotificationChannel(serviceChannel)
            notificationManager.createNotificationChannel(errorChannel)
            logger.i(TAG, "Notification channels created")
        }
    }

    private fun createNotification(contentText: String = "Collecting location data in background"): Notification {
        val stopIntent =
            Intent(this, LocationCollectionService::class.java).apply {
                action = ACTION_STOP_COLLECTION
            }

        val stopPendingIntent =
            PendingIntent.getService(
                this,
                0,
                stopIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )

        return NotificationCompat
            .Builder(this, CHANNEL_ID)
            .setContentTitle("Location Collection Active")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                R.drawable.ic_menu_close_clear_cancel,
                "Stop",
                stopPendingIntent,
            ).build()
    }

    override fun onDestroy() {
        super.onDestroy()
        logger.i(TAG, "Service destroyed")
        stopLocationUpdates()
        serviceScope.cancel()
    }

    companion object {
        private const val TAG = "LocationCollectionService"
        private const val CHANNEL_ID = "location_collection_channel"
        private const val ERROR_CHANNEL_ID = "location_error_channel"
        private const val ERROR_NOTIFICATION_ID = 1002
        const val ACTION_START_COLLECTION = "dev.locationapp.ACTION_START_COLLECTION"
        const val ACTION_STOP_COLLECTION = "dev.locationapp.ACTION_STOP_COLLECTION"
    }
}
