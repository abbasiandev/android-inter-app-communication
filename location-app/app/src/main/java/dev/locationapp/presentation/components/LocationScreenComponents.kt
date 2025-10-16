package dev.locationapp.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.abbasian.protocol.LocationData
import dev.locationapp.presentation.LocationListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun locationScreen(
    state: LocationListState,
    onStartService: () -> Unit,
    onStopService: () -> Unit,
    onRefresh: () -> Unit,
) {
    Scaffold(
        topBar = { locationAppBar() },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            locationCounter(count = state.locationCount)

            locationContent(
                state = state,
                onRefresh = onRefresh,
                modifier = Modifier.weight(1f),
            )

            serviceControlButtons(
                onStartService = onStartService,
                onStopService = onStopService,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun locationAppBar() {
    TopAppBar(
        title = { Text("Location App") },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
            ),
    )
}

@Composable
private fun locationCounter(count: Int) {
    Text(
        text = "Locations: $count",
        modifier = Modifier.padding(16.dp),
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun locationContent(
    state: LocationListState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = onRefresh,
        modifier = modifier,
    ) {
        if (state.locations.isEmpty() && !state.isLoading) {
            emptyLocationMessage()
        } else {
            locationList(locations = state.locations)
        }
    }
}

@Composable
private fun emptyLocationMessage() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "No locations collected yet.\nStart the service to begin tracking.",
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
        )
    }
}

@Composable
private fun locationList(locations: List<LocationData>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
    ) {
        items(locations) { location ->
            locationItem(location = location)
        }
    }
}

@Composable
private fun serviceControlButtons(
    onStartService: () -> Unit,
    onStopService: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Button(
            onClick = onStartService,
            modifier = Modifier.weight(1f),
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Start Service")
        }

        OutlinedButton(
            onClick = onStopService,
            modifier = Modifier.weight(1f),
        ) {
            Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Stop Service")
        }
    }
}

@Composable
private fun locationItem(location: LocationData) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            locationCoordinates(location)
            Spacer(modifier = Modifier.height(4.dp))
            locationTimestamp(location)
            Spacer(modifier = Modifier.height(8.dp))
            locationMetadata(location)
            Spacer(modifier = Modifier.height(4.dp))
            locationAge(location)
        }
    }
}

@Composable
private fun locationCoordinates(location: LocationData) {
    Text(
        text = location.getCoordinatesString(),
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun locationTimestamp(location: LocationData) {
    Text(
        text = location.getFormattedDate(),
        fontSize = 14.sp,
    )
}

@Composable
private fun locationMetadata(location: LocationData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Accuracy: ${location.accuracy}m",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )

        Text(
            text = "Provider: ${location.provider}",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun locationAge(location: LocationData) {
    val age = location.getAge()
    val ageText = formatAge(age)

    Text(
        text = ageText,
        fontSize = 12.sp,
        fontStyle = FontStyle.Italic,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
    )
}

private fun formatAge(age: Long): String =
    when {
        age < 60_000 -> "Just now"
        age < 3600_000 -> "${age / 60_000}m ago"
        age < 86400_000 -> "${age / 3600_000}h ago"
        else -> "${age / 86400_000}d ago"
    }
