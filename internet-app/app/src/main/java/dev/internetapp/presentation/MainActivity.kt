package dev.internetapp.presentation

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.abbasian.protocol.AppLogger
import dev.abbasian.protocol.LocationData
import dev.internetapp.presentation.ui.theme.InternetAppTheme
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val logger: AppLogger by inject()
    private val viewModel: CommandViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            InternetAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CommandScreen(viewModel = viewModel)
                }
            }
        }
    }

    @Composable
    private fun CommandScreen(viewModel: CommandViewModel) {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            viewModel.effect.collect { effect ->
                when (effect) {
                    is CommandEffect.ShowToast -> {
                        Toast.makeText(this@MainActivity, effect.message, Toast.LENGTH_SHORT).show()
                    }

                    is CommandEffect.ShowError -> {
                        Toast.makeText(
                            this@MainActivity,
                            "Error: ${effect.error}",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    is CommandEffect.ShowSuccess -> {
                        Toast.makeText(this@MainActivity, effect.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Internet App - Control Panel",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            if (uiState.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }

            ServiceStatusCard(serviceStatus = uiState.serviceStatus)

            Spacer(modifier = Modifier.height(16.dp))

            ControlButtons(
                isLoading = uiState.isLoading,
                canClear = uiState.locations.isNotEmpty() || uiState.latestLocation != null,
                onStartService = { viewModel.handleIntent(CommandIntent.StartService) },
                onStopService = { viewModel.handleIntent(CommandIntent.StopService) },
                onGetAllLocations = { viewModel.handleIntent(CommandIntent.GetAllLocations) },
                onGetLatestLocation = { viewModel.handleIntent(CommandIntent.GetLatestLocation) },
                onClearData = { viewModel.handleIntent(CommandIntent.ClearResponse) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.error != null) {
                ErrorCard(error = uiState.error!!)
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (uiState.lastResponse != null) {
                LastResponseCard(response = uiState.lastResponse!!)
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (uiState.latestLocation != null) {
                LatestLocationCard(location = uiState.latestLocation!!)
                Spacer(modifier = Modifier.height(16.dp))
            }

            LocationsList(locations = uiState.locations)
        }
    }

    @Composable
    private fun ServiceStatusCard(serviceStatus: ServiceStatus) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            val (statusText, statusColor) = when (serviceStatus) {
                ServiceStatus.RUNNING -> "Service: RUNNING ✓" to Color(0xFF2E7D32)
                ServiceStatus.STOPPED -> "Service: STOPPED ✕" to Color(0xFFC62828)
                ServiceStatus.UNKNOWN -> "Service: UNKNOWN" to Color.Gray
            }

            Text(
                text = statusText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }

    @Composable
    private fun ControlButtons(
        isLoading: Boolean,
        canClear: Boolean,
        onStartService: () -> Unit,
        onStopService: () -> Unit,
        onGetAllLocations: () -> Unit,
        onGetLatestLocation: () -> Unit,
        onClearData: () -> Unit
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onStartService,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Service", fontSize = 16.sp)
            }

            OutlinedButton(
                onClick = onStopService,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Close, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Stop Service", fontSize = 16.sp)
            }

            FilledTonalButton(
                onClick = onGetAllLocations,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.List, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Get All Locations", fontSize = 16.sp)
            }

            FilledTonalButton(
                onClick = onGetLatestLocation,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Get Latest Location", fontSize = 16.sp)
            }

            TextButton(
                onClick = onClearData,
                enabled = !isLoading && canClear,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Clear, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear Display", fontSize = 16.sp)
            }
        }
    }

    @Composable
    private fun ErrorCard(error: String) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Error",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD32F2F)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = error,
                    fontSize = 14.sp,
                    color = Color(0xFFC62828)
                )
            }
        }
    }

    @Composable
    private fun LastResponseCard(response: String) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Last Response",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = response, fontSize = 14.sp)
            }
        }
    }

    @Composable
    private fun LatestLocationCard(location: LocationData) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Latest Location",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = location.getCoordinatesString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = location.getFormattedDate(),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Accuracy: ${location.accuracy}m",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    @Composable
    private fun LocationsList(locations: List<LocationData>) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Locations: ${locations.size}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (locations.isEmpty()) {
                Text(
                    text = "No locations received yet",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )
            } else {
                locations.forEachIndexed { index, location ->
                    LocationItem(location = location, position = index + 1)
                    if (index < locations.size - 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    @Composable
    private fun LocationItem(location: LocationData, position: Int) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#$position",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = location.getCoordinatesString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = location.getFormattedDate(),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Accuracy: ${location.accuracy}m",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = location.provider,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                val age = location.getAge()
                val ageText = when {
                    age < 60_000 -> "Just now"
                    age < 3600_000 -> "${age / 60_000}m ago"
                    age < 86400_000 -> "${age / 3600_000}h ago"
                    else -> "${age / 86400_000}d ago"
                }
                val ageColor = when {
                    location.isFresh() -> Color(0xFF2E7D32)
                    age < 3600_000 -> Color(0xFFEF6C00)
                    else -> Color(0xFFC62828)
                }

                Text(
                    text = ageText,
                    fontSize = 11.sp,
                    color = ageColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}