package dev.locationapp.feature.location.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.abbasian.protocol.domain.logger.AppLogger
import dev.locationapp.feature.location.domain.usecase.GetAllLocationsUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationListViewModel
    @Inject
    constructor(
        private val getAllLocationsUseCase: GetAllLocationsUseCase,
        private val logger: AppLogger,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(LocationListState())
        val uiState: StateFlow<LocationListState> = _uiState.asStateFlow()

        init {
            logger.d(TAG, "ViewModel initialized")
            loadLocations()
        }

        private fun loadLocations(showLoading: Boolean = false) {
            viewModelScope.launch {
                if (showLoading) {
                    _uiState.update { it.copy(isLoading = true) }
                }

                getAllLocationsUseCase()
                    .catch { e ->
                        logger.e(TAG, "Failed to load locations", e)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Failed to load locations: ${e.message}",
                            )
                        }
                    }.collect { locations ->
                        logger.d(TAG, "Loaded ${locations.size} locations")

                        if (showLoading) {
                            delay(300)
                        }

                        _uiState.update {
                            it.copy(
                                locations = locations,
                                locationCount = locations.size,
                                isLoading = false,
                                error = null,
                            )
                        }
                    }
            }
        }

        fun refresh() {
            logger.i(TAG, "Refreshing locations")
            loadLocations(showLoading = true)
        }

        fun clearError() {
            _uiState.update { it.copy(error = null) }
        }

        companion object {
            private const val TAG = "LocationListViewModel"
        }
    }
