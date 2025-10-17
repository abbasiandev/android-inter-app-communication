package dev.locationapp.feature.location.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import dev.abbasian.protocol.AppLogger
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logger.i(TAG, "MainActivity created")

        checkPermissions()

        setContent {
            locationAppTheme {
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
                    startLocationService()
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
                startLocationService()
            }

            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                logger.i(TAG, "Coarse location permission granted")
                startLocationService()
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
