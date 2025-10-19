@file:Suppress("ktlint:standard:no-wildcard-imports", "ImplicitDefaultLocale")

package dev.shared.debug.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.abbasian.protocol.data.analytics.MockAnalyticsService
import dev.abbasian.protocol.domain.analytics.IAnalyticsService

@Composable
fun summaryTab(analyticsService: IAnalyticsService) {
    val mockService = analyticsService as? MockAnalyticsService
    val summary = mockService?.getSummary() ?: "No data available"

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
            ) {
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        systemInfoCard()
    }
}

@Composable
fun systemInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            Text(
                text = "System Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(12.dp))

            val runtime = Runtime.getRuntime()
            val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024.0 * 1024.0)
            val maxMemory = runtime.maxMemory() / (1024.0 * 1024.0)
            val totalMemory = runtime.totalMemory() / (1024.0 * 1024.0)

            infoRow("Memory Used", String.format("%.2f MB", usedMemory))
            infoRow("Memory Available", String.format("%.2f MB", totalMemory - usedMemory))
            infoRow("Max Memory", String.format("%.2f MB", maxMemory))
            infoRow("Memory Usage", String.format("%.1f%%", (usedMemory / maxMemory) * 100))

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { (usedMemory / maxMemory).toFloat() },
                modifier = Modifier.fillMaxWidth(),
                color =
                    if ((usedMemory / maxMemory) > 0.8) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
            )
        }
    }
}

@Composable
fun infoRow(
    label: String,
    value: String,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}
