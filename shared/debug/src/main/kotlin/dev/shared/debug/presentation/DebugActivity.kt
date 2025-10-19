@file:Suppress("ktlint:standard:no-wildcard-imports")

package dev.shared.debug.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import dev.abbasian.protocol.data.analytics.MockAnalyticsService
import dev.abbasian.protocol.domain.analytics.IAnalyticsService
import dev.shared.debug.presentation.components.eventsTab
import dev.shared.debug.presentation.components.exportDataDialog
import dev.shared.debug.presentation.components.metricsTab
import dev.shared.debug.presentation.components.summaryTab
import dev.shared.debug.ui.theme.debugTheme
import kotlinx.coroutines.launch

abstract class DebugActivity : ComponentActivity() {
    protected abstract fun getAnalyticsService(): IAnalyticsService

    protected open fun getAppName(): String = "AndroidCodeChallenge"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            debugTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    debugScreen(
                        analyticsService = getAnalyticsService(),
                        appName = getAppName(),
                        onBack = { finish() },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun debugScreen(
    analyticsService: IAnalyticsService,
    appName: String,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(0) }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportedData by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "$appName Debug",
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            val data = (analyticsService as? MockAnalyticsService)?.exportData()
                            if (data != null) {
                                exportedData = data
                                showExportDialog = true
                            }
                        }
                    }) {
                        Icon(Icons.Default.Share, "Export")
                    }

                    IconButton(onClick = {
                        scope.launch {
                            analyticsService.clearData()
                        }
                    }) {
                        Icon(Icons.Default.Delete, "Clear")
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Events") },
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Metrics") },
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Summary") },
                )
            }

            when (selectedTab) {
                0 -> eventsTab(analyticsService)
                1 -> metricsTab(analyticsService)
                2 -> summaryTab(analyticsService)
            }
        }
    }

    if (showExportDialog) {
        exportDataDialog(
            data = exportedData,
            onDismiss = { showExportDialog = false },
        )
    }
}
