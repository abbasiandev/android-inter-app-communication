package dev.internetapp.feature.responsedisplay.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.abbasian.protocol.analytics.PerformanceStats
import dev.abbasian.protocol.data.analytics.MockAnalyticsService

@Composable
fun performanceMetricsCard(
    analyticsService: MockAnalyticsService,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Performance Metrics",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(16.dp))

        performanceStatsCard(
            analyticsService = analyticsService,
            metricName = "response_time",
            displayName = "Response Time",
            unit = "ms",
        )

        Spacer(modifier = Modifier.height(12.dp))

        performanceStatsCard(
            analyticsService = analyticsService,
            metricName = "location_accuracy",
            displayName = "Location Accuracy",
            unit = "meters",
        )

        Spacer(modifier = Modifier.height(12.dp))

        performanceStatsCard(
            analyticsService = analyticsService,
            metricName = "memory_usage",
            displayName = "Memory Usage",
            unit = "MB",
        )

        Spacer(modifier = Modifier.height(12.dp))

        performanceStatsCard(
            analyticsService = analyticsService,
            metricName = "database_size",
            displayName = "Database Size",
            unit = "MB",
        )
    }
}

@Composable
private fun performanceStatsCard(
    analyticsService: MockAnalyticsService,
    metricName: String,
    displayName: String,
    unit: String,
) {
    val stats by analyticsService
        .getPerformanceStats(metricName)
        .collectAsStateWithLifecycle(
            initialValue =
                PerformanceStats(
                    metricName = metricName,
                    count = 0,
                    min = 0.0,
                    max = 0.0,
                    avg = 0.0,
                    median = 0.0,
                    p95 = 0.0,
                    p99 = 0.0,
                ),
        )

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )

                Text(
                    text = "${stats.count} samples",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
                )
            }

            if (stats.count > 0) {
                Spacer(modifier = Modifier.height(12.dp))

                statsGrid(stats, unit)

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Avg: ${formatValue(stats.avg, unit)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )

                    LinearProgressIndicator(
                        progress = calculateProgress(stats.avg, stats.min, stats.max),
                        modifier = Modifier.weight(2f),
                        color = getColorForPerformance(stats.avg, stats.p95),
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No data collected yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.5f),
                )
            }
        }
    }
}

@Composable
private fun statsGrid(
    stats: PerformanceStats,
    unit: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        statItem("Min", formatValue(stats.min, unit))
        statItem("Max", formatValue(stats.max, unit))
        statItem("Median", formatValue(stats.median, unit))
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        statItem("P95", formatValue(stats.p95, unit))
        statItem("P99", formatValue(stats.p99, unit))
        statItem("Avg", formatValue(stats.avg, unit))
    }
}

@Composable
private fun RowScope.statItem(
    label: String,
    value: String,
) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
        )
    }
}

private fun formatValue(
    value: Double,
    unit: String,
): String =
    when {
        value < 0.01 -> "0"
        value < 1 -> String.format("%.2f %s", value, unit)
        value < 10 -> String.format("%.1f %s", value, unit)
        else -> String.format("%.0f %s", value, unit)
    }

private fun calculateProgress(
    current: Double,
    min: Double,
    max: Double,
): Float {
    if (max == min) return 0f
    return ((current - min) / (max - min)).toFloat().coerceIn(0f, 1f)
}

@Composable
private fun getColorForPerformance(
    avg: Double,
    p95: Double,
): androidx.compose.ui.graphics.Color =
    when {
        avg <= p95 * 0.5 -> MaterialTheme.colorScheme.primary
        avg <= p95 * 0.8 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }
