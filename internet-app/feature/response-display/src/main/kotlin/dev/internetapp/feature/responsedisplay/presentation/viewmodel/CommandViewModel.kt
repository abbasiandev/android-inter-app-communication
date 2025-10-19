package dev.internetapp.feature.responsedisplay.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.abbasian.protocol.domain.logger.AppLogger
import dev.abbasian.protocol.domain.model.LocationResponse
import dev.internetapp.feature.commandsender.domain.usecase.GetAllLocationsUseCase
import dev.internetapp.feature.commandsender.domain.usecase.GetLatestLocationUseCase
import dev.internetapp.feature.commandsender.domain.usecase.StartServiceUseCase
import dev.internetapp.feature.commandsender.domain.usecase.StopServiceUseCase
import dev.internetapp.feature.responsedisplay.domain.model.CommandEffect
import dev.internetapp.feature.responsedisplay.domain.model.CommandIntent
import dev.internetapp.feature.responsedisplay.domain.model.CommandUiState
import dev.internetapp.feature.responsedisplay.domain.model.ErrorState
import dev.internetapp.feature.responsedisplay.domain.model.ServiceStatus
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CommandViewModel(
    private val startServiceUseCase: StartServiceUseCase,
    private val stopServiceUseCase: StopServiceUseCase,
    private val getAllLocationsUseCase: GetAllLocationsUseCase,
    private val getLatestLocationUseCase: GetLatestLocationUseCase,
    private val logger: AppLogger,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CommandUiState())
    val uiState: StateFlow<CommandUiState> = _uiState.asStateFlow()

    private val _effect = Channel<CommandEffect>(Channel.BUFFERED)
    val effect: Flow<CommandEffect> = _effect.receiveAsFlow()

    private var lastFailedCommand: (() -> Unit)? = null

    init {
        logger.d(TAG, "CommandViewModel initialized")
    }

    fun handleIntent(intent: CommandIntent) {
        logger.d(TAG, "Handling intent: ${intent::class.simpleName}")
        when (intent) {
            is CommandIntent.StartService -> startService()
            is CommandIntent.StopService -> stopService()
            is CommandIntent.GetAllLocations -> getAllLocations()
            is CommandIntent.GetLatestLocation -> getLatestLocation()
            is CommandIntent.ClearError -> clearError()
            is CommandIntent.ClearResponse -> clearResponse()
        }
    }

    fun retryLastFailedCommand() {
        logger.d(TAG, "Retrying last failed command")
        lastFailedCommand?.invoke()
    }

    private fun startService() {
        viewModelScope.launch {
            lastFailedCommand = { startService() }
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = startServiceUseCase(useRetry = true)

            result
                .onSuccess { response ->
                    when (response) {
                        is LocationResponse.Success -> {
                            logger.i(TAG, "Service started: ${response.message}")
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    lastResponse = response.message,
                                    serviceStatus = ServiceStatus.RUNNING,
                                    error = null,
                                )
                            }
                            _effect.send(CommandEffect.ShowSuccess("${response.message}"))
                            lastFailedCommand = null
                        }
                        else -> {
                            _uiState.update { it.copy(isLoading = false) }
                        }
                    }
                }.onFailure { error ->
                    handleCommandError(error)
                }
        }
    }

    private fun stopService() {
        viewModelScope.launch {
            lastFailedCommand = { stopService() }
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = stopServiceUseCase(useRetry = true)

            result
                .onSuccess { response ->
                    when (response) {
                        is LocationResponse.Success -> {
                            logger.i(TAG, "Service stopped: ${response.message}")
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    lastResponse = response.message,
                                    serviceStatus = ServiceStatus.STOPPED,
                                    error = null,
                                )
                            }
                            _effect.send(CommandEffect.ShowSuccess("${response.message}"))
                            lastFailedCommand = null
                        }
                        else -> {
                            _uiState.update { it.copy(isLoading = false) }
                        }
                    }
                }.onFailure { error ->
                    handleCommandError(error)
                }
        }
    }

    private fun getAllLocations() {
        viewModelScope.launch {
            lastFailedCommand = { getAllLocations() }
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = getAllLocationsUseCase(useRetry = true)

            result
                .onSuccess { response ->
                    when (response) {
                        is LocationResponse.LocationList -> {
                            logger.i(TAG, "Received ${response.locations.size} locations")
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    locations = response.locations,
                                    lastResponse = "Retrieved ${response.locations.size} locations",
                                    error = null,
                                )
                            }
                            _effect.send(
                                CommandEffect.ShowToast("Retrieved ${response.locations.size} locations"),
                            )
                            lastFailedCommand = null
                        }
                        else -> {
                            _uiState.update { it.copy(isLoading = false, locations = emptyList()) }
                        }
                    }
                }.onFailure { error ->
                    _uiState.update { it.copy(locations = emptyList()) }
                    handleCommandError(error)
                }
        }
    }

    private fun getLatestLocation() {
        viewModelScope.launch {
            lastFailedCommand = { getLatestLocation() }
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = getLatestLocationUseCase(useRetry = true)

            result
                .onSuccess { response ->
                    when (response) {
                        is LocationResponse.SingleLocation -> {
                            logger.i(TAG, "Received latest location: ${response.location}")
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    latestLocation = response.location,
                                    lastResponse =
                                        if (response.location != null) {
                                            "Latest: ${response.location?.getCoordinatesString()}"
                                        } else {
                                            "No location available"
                                        },
                                    error = null,
                                )
                            }
                            _effect.send(CommandEffect.ShowToast("Latest location retrieved"))
                            lastFailedCommand = null
                        }
                        else -> {
                            _uiState.update { it.copy(isLoading = false, latestLocation = null) }
                        }
                    }
                }.onFailure { error ->
                    _uiState.update { it.copy(latestLocation = null) }
                    handleCommandError(error)
                }
        }
    }

    private suspend fun handleCommandError(error: dev.abbasian.protocol.domain.model.CommandError) {
        logger.e(TAG, "Command failed: ${error.message}", error.throwable)

        val errorState = ErrorState.from(error)

        _uiState.update {
            it.copy(
                isLoading = false,
                error = errorState,
            )
        }

        _effect.send(CommandEffect.ShowError(error.message))
    }

    private fun clearError() {
        logger.d(TAG, "Clearing error state")
        _uiState.update { it.copy(error = null) }
    }

    private fun clearResponse() {
        logger.d(TAG, "Clearing response data")
        _uiState.update {
            it.copy(
                lastResponse = null,
                locations = emptyList(),
                latestLocation = null,
            )
        }
    }

    companion object {
        private const val TAG = "CommandViewModel"
    }
}
