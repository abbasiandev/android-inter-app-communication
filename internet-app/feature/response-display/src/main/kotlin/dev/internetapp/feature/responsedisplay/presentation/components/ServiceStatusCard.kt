package dev.internetapp.feature.responsedisplay.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.internetapp.feature.responsedisplay.domain.model.ServiceStatus

@Composable
fun serviceStatusCard(serviceStatus: ServiceStatus) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        val (statusText, statusColor) =
            when (serviceStatus) {
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
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        )
    }
}
