package dev.internetapp.feature.responsedisplay.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.internetapp.feature.responsedisplay.domain.model.CommandIntent
import dev.internetapp.feature.responsedisplay.domain.model.CommandUiState

@Composable
fun controlButtons(
    uiState: CommandUiState,
    onIntent: (CommandIntent) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Button(
            onClick = { onIntent(CommandIntent.StartService) },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Start Service", fontSize = 16.sp)
        }

        OutlinedButton(
            onClick = { onIntent(CommandIntent.StopService) },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Default.Close, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Stop Service", fontSize = 16.sp)
        }

        FilledTonalButton(
            onClick = { onIntent(CommandIntent.GetAllLocations) },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Default.List, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Get All Locations", fontSize = 16.sp)
        }

        FilledTonalButton(
            onClick = { onIntent(CommandIntent.GetLatestLocation) },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Get Latest Location", fontSize = 16.sp)
        }

        val canClear = uiState.locations.isNotEmpty() || uiState.latestLocation != null
        TextButton(
            onClick = { onIntent(CommandIntent.ClearResponse) },
            enabled = !uiState.isLoading && canClear,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Default.Clear, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Clear Display", fontSize = 16.sp)
        }
    }
}
