package dev.locationapp.feature.location.presentation

import android.Manifest
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import dev.abbasian.protocol.data.constants.ProtocolConstants
import dev.abbasian.protocol.domain.logger.AppLogger
import dev.locationapp.feature.location.presentation.components.locationScreen
import dev.locationapp.feature.location.presentation.ui.theme.locationAppTheme
import dev.locationapp.feature.location.service.LocationCollectionService
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var logger: AppLogger

    private val viewModel: LocationListViewModel by viewModels()

    private val locationPermissionRequest =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { permissions ->
            handleLocationPermissionResult(permissions)
        }

    private val notificationPermissionRequest =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            handleNotificationPermissionResult(isGranted)
        }

    private val locationSettingsRequest =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult(),
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                logger.i(TAG, "Location settings enabled")
                startLocationService()
            } else {
                logger.w(TAG, "Location settings not enabled")
                Toast
                    .makeText(
                        this,
                        "Location services must be enabled",
                        Toast.LENGTH_LONG,
                    ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logger.i(TAG, "MainActivity created")

        checkPermissions()

        setContent {
            locationAppTheme {
                mainScreenWithFab()
            }
        }
    }

    @Composable
    private fun mainScreenWithFab() {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        startActivity(Intent(this@MainActivity, LocationDebugActivity::class.java))
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(bottom = 64.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = "Debug Dashboard",
                    )
                }
            },
        ) { paddingValues ->
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
            ) {
                mainContent()
            }
        }
    }

    @Composable
    private fun mainContent() {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        LaunchedEffect(uiState.error) {
            uiState.error?.let { error ->
                Toast.makeText(this@MainActivity, error, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }

        locationScreen(
            state = uiState,
            onStartService = {
                logger.d(TAG, "Start service button clicked")
                checkAndRequestPermissions {
                    checkLocationSettings()
                }
            },
            onStopService = {
                logger.d(TAG, "Stop service button clicked")
                stopLocationService()
            },
            onRefresh = {
                logger.d(TAG, "Refresh triggered")
                viewModel.refresh()
            },
        )
    }

    private fun handleLocationPermissionResult(permissions: Map<String, Boolean>) {
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                logger.i(TAG, "Fine location permission granted")
                checkLocationSettings()
            }

            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                logger.i(TAG, "Coarse location permission granted")
                checkLocationSettings()
            }

            else -> {
                logger.w(TAG, "Location permission denied")
                Toast.makeText(this, "Location permission required", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun handleNotificationPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            logger.i(TAG, "Notification permission granted")
        } else {
            logger.w(TAG, "Notification permission denied")
        }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun checkAndRequestPermissions(onGranted: () -> Unit) {
        when {
            hasLocationPermission() -> onGranted()
            else -> requestLocationPermissions()
        }
    }

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

    private fun requestLocationPermissions() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ),
        )
    }

    private fun checkLocationSettings() {
        val locationRequest =
            LocationRequest
                .Builder(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    ProtocolConstants.LOCATION_UPDATE_INTERVAL_MS,
                ).build()

        val settingsRequest =
            LocationSettingsRequest
                .Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true)
                .build()

        val settingsClient = LocationServices.getSettingsClient(this)

        settingsClient
            .checkLocationSettings(settingsRequest)
            .addOnSuccessListener {
                logger.i(TAG, "Location settings are satisfied")
                startLocationService()
            }.addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    try {
                        logger.i(TAG, "Requesting to enable location settings")
                        val intentSenderRequest =
                            IntentSenderRequest
                                .Builder(
                                    exception.resolution.intentSender,
                                ).build()
                        locationSettingsRequest.launch(intentSenderRequest)
                    } catch (sendEx: IntentSender.SendIntentException) {
                        logger.e(TAG, "Failed to show location settings dialog", sendEx)
                        Toast
                            .makeText(
                                this,
                                "Please enable location in settings",
                                Toast.LENGTH_LONG,
                            ).show()
                    }
                } else {
                    logger.e(TAG, "Location settings check failed", exception)
                    Toast
                        .makeText(
                            this,
                            "Location services are required",
                            Toast.LENGTH_LONG,
                        ).show()
                }
            }
    }

    private fun startLocationService() {
        val intent = createServiceIntent(LocationCollectionService.ACTION_START_COLLECTION)
        startLocationServiceWithIntent(intent)
        Toast.makeText(this, "Location service started", Toast.LENGTH_SHORT).show()
        logger.i(TAG, "Location service start requested")
    }

    private fun stopLocationService() {
        val intent = createServiceIntent(LocationCollectionService.ACTION_STOP_COLLECTION)
        startService(intent)
        Toast.makeText(this, "Location service stopped", Toast.LENGTH_SHORT).show()
        logger.i(TAG, "Location service stop requested")
    }

    private fun createServiceIntent(action: String): Intent =
        Intent(this, LocationCollectionService::class.java).apply {
            this.action = action
        }

    private fun startLocationServiceWithIntent(intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
