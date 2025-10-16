package dev.internetapp.presentation

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.internetapp.presentation.components.controlButtons
import dev.internetapp.presentation.components.errorCard
import dev.internetapp.presentation.components.lastResponseCard
import dev.internetapp.presentation.components.latestLocationCard
import dev.internetapp.presentation.components.locationsList
import dev.internetapp.presentation.components.serviceStatusCard
import dev.internetapp.presentation.ui.theme.internetAppTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val viewModel: CommandViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            internetAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    commandScreen(viewModel = viewModel)
                }
            }
        }
    }

    @Composable
    private fun commandScreen(viewModel: CommandViewModel) {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            viewModel.effect.collect { effect ->
                handleEffect(effect)
            }
        }

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
        ) {
            appHeader()

            if (uiState.isLoading) {
                loadingIndicator()
            }

            serviceStatusCard(serviceStatus = uiState.serviceStatus)
            Spacer(modifier = Modifier.height(16.dp))

            controlButtons(
                uiState = uiState,
                onIntent = viewModel::handleIntent,
            )
            Spacer(modifier = Modifier.height(16.dp))

            uiState.error?.let { error ->
                errorCard(error = error)
                Spacer(modifier = Modifier.height(16.dp))
            }

            uiState.lastResponse?.let { response ->
                lastResponseCard(response = response)
                Spacer(modifier = Modifier.height(16.dp))
            }

            uiState.latestLocation?.let { location ->
                latestLocationCard(location = location)
                Spacer(modifier = Modifier.height(16.dp))
            }

            locationsList(locations = uiState.locations)
        }
    }

    private fun handleEffect(effect: CommandEffect) {
        when (effect) {
            is CommandEffect.ShowToast -> {
                Toast.makeText(this, effect.message, Toast.LENGTH_SHORT).show()
            }
            is CommandEffect.ShowError -> {
                Toast
                    .makeText(
                        this,
                        "Error: ${effect.error}",
                        Toast.LENGTH_LONG,
                    ).show()
            }
            is CommandEffect.ShowSuccess -> {
                Toast.makeText(this, effect.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Composable
    private fun appHeader() {
        Text(
            text = "Internet App - Control Panel",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
        )
    }

    @Composable
    private fun loadingIndicator() {
        LinearProgressIndicator(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
        )
    }
}
