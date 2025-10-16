package dev.internetapp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.abbasian.protocol.LocationData

@Composable
fun locationsList(locations: List<LocationData>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Locations: ${locations.size}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        if (locations.isEmpty()) {
            emptyLocationsMessage()
        } else {
            locations.forEachIndexed { index, location ->
                locationItem(location = location, position = index + 1)
                if (index < locations.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun emptyLocationsMessage() {
    Text(
        text = "No locations received yet",
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
    )
}

@Composable
private fun locationItem(
    location: LocationData,
    position: Int,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            positionBadge(position)
            locationDetails(location)
            locationAge(location)
        }
    }
}

@Composable
private fun positionBadge(position: Int) {
    Box(
        modifier =
            Modifier
                .size(40.dp)
                .background(
                    MaterialTheme.colorScheme.primary,
                    CircleShape,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "#$position",
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
        )
    }
}

@Composable
private fun RowScope.locationDetails(location: LocationData) {
    Column(modifier = Modifier.weight(1f)) {
        Text(
            text = location.getCoordinatesString(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = location.getFormattedDate(),
            fontSize = 12.sp,
        )
        Spacer(modifier = Modifier.height(4.dp))
        locationMetadata(location)
    }
}

@Composable
private fun locationMetadata(location: LocationData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Accuracy: ${location.accuracy}m",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = location.provider,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun locationAge(location: LocationData) {
    val age = location.getAge()
    val (ageText, ageColor) = getAgeDisplayInfo(age, location.isFresh())

    Text(
        text = ageText,
        fontSize = 11.sp,
        color = ageColor,
        fontWeight = FontWeight.Medium,
    )
}

private fun getAgeDisplayInfo(
    age: Long,
    isFresh: Boolean,
): Pair<String, Color> {
    val ageText =
        when {
            age < 60_000 -> "Just now"
            age < 3600_000 -> "${age / 60_000}m ago"
            age < 86400_000 -> "${age / 3600_000}h ago"
            else -> "${age / 86400_000}d ago"
        }
    val ageColor =
        when {
            isFresh -> Color(0xFF2E7D32)
            age < 3600_000 -> Color(0xFFEF6C00)
            else -> Color(0xFFC62828)
        }
    return ageText to ageColor
}
