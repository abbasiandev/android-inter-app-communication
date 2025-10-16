package dev.internetapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.abbasian.protocol.AppLogger
import dev.abbasian.protocol.LocationResponse
import dev.internetapp.domain.usecase.GetAllLocationsUseCase
import dev.internetapp.domain.usecase.GetLatestLocationUseCase
import dev.internetapp.domain.usecase.StartServiceUseCase
import dev.internetapp.domain.usecase.StopServiceUseCase
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

    private fun startService() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val response = startServiceUseCase()) {
                is LocationResponse.Success -> {
                    logger.i(TAG, "Service started: ${response.message}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            lastResponse = response.message,
                            serviceStatus = ServiceStatus.RUNNING,
                        )
                    }
                    _effect.send(CommandEffect.ShowSuccess("Service started successfully"))
                }
                is LocationResponse.Error -> {
                    logger.e(TAG, "Failed to start service: ${response.message}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = response.message,
                        )
                    }
                    _effect.send(CommandEffect.ShowError(response.message))
                }
                else -> {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    private fun stopService() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val response = stopServiceUseCase()) {
                is LocationResponse.Success -> {
                    logger.i(TAG, "Service stopped: ${response.message}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            lastResponse = response.message,
                            serviceStatus = ServiceStatus.STOPPED,
                        )
                    }
                    _effect.send(CommandEffect.ShowSuccess("Service stopped successfully"))
                }
                is LocationResponse.Error -> {
                    logger.e(TAG, "Failed to stop service: ${response.message}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = response.message,
                        )
                    }
                    _effect.send(CommandEffect.ShowError(response.message))
                }
                else -> {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    private fun getAllLocations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val response = getAllLocationsUseCase()) {
                is LocationResponse.LocationList -> {
                    logger.i(TAG, "Received ${response.locations.size} locations")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            locations = response.locations,
                            lastResponse = "Retrieved ${response.locations.size} locations",
                        )
                    }
                    _effect.send(
                        CommandEffect.ShowToast(
                            "Retrieved ${response.locations.size} locations",
                        ),
                    )
                }
                is LocationResponse.Error -> {
                    logger.w(TAG, "Failed to get locations: ${response.message}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = response.message,
                            locations = emptyList(),
                        )
                    }
                    _effect.send(CommandEffect.ShowError(response.message))
                }
                else -> {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    private fun getLatestLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val response = getLatestLocationUseCase()) {
                is LocationResponse.SingleLocation -> {
                    logger.i(TAG, "Received latest location: ${response.location}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            latestLocation = response.location,
                            lastResponse =
                                if (response.location != null) {
                                    "Latest: ${response.location!!.getCoordinatesString()}"
                                } else {
                                    "No location available"
                                },
                        )
                    }
                    _effect.send(CommandEffect.ShowToast("Latest location retrieved"))
                }
                is LocationResponse.Error -> {
                    logger.w(TAG, "Failed to get latest location: ${response.message}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = response.message,
                            latestLocation = null,
                        )
                    }
                    _effect.send(CommandEffect.ShowError(response.message))
                }
                else -> {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    private fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun clearResponse() {
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
