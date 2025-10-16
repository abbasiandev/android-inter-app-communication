package dev.locationapp.feature.location

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
import dev.abbasian.protocol.AppLogger
import dev.abbasian.protocol.LocationData
import dev.abbasian.protocol.ProtocolConstants
import dev.locationapp.domain.usecase.SaveLocationUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
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

    private var isCollecting = false

    override fun onCreate() {
        super.onCreate()
        logger.i(TAG, "Service created")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
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

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

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
                        logger.d(TAG, "Location received: ${location.latitude}, ${location.longitude}")
                        saveLocation(location)
                    }
                }

                override fun onLocationAvailability(availability: LocationAvailability) {
                    logger.d(TAG, "Location availability: ${availability.isLocationAvailable}")
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
            stopSelf()
            return
        }

        val locationRequest =
            LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                ProtocolConstants.LOCATION_UPDATE_INTERVAL_MS,
            ).apply {
                setMinUpdateIntervalMillis(ProtocolConstants.LOCATION_UPDATE_INTERVAL_MS)
                setWaitForAccurateLocation(false)
                setMaxUpdateDelayMillis(ProtocolConstants.LOCATION_UPDATE_INTERVAL_MS * 2)
            }.build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null,
        ).addOnSuccessListener {
            logger.i(TAG, "Location updates started successfully")
        }.addOnFailureListener { exception ->
            logger.e(TAG, "Failed to start location updates: ${exception.message}")
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
            } catch (e: Exception) {
                logger.e(TAG, "Error saving location: ${e.message}")
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    "Location Collection",
                    NotificationManager.IMPORTANCE_LOW,
                ).apply {
                    description = "Notification for location collection service"
                    setShowBadge(false)
                }

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            logger.i(TAG, "Notification channel created")
        }
    }

    private fun createNotification(): Notification {
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

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Location Collection Active")
            .setContentText("Collecting location data in background")
            .setSmallIcon(R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                R.drawable.ic_menu_close_clear_cancel,
                "Stop",
                stopPendingIntent,
            )
            .build()
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
        const val ACTION_START_COLLECTION = "dev.locationapp.ACTION_START_COLLECTION"
        const val ACTION_STOP_COLLECTION = "dev.locationapp.ACTION_STOP_COLLECTION"
    }
}
