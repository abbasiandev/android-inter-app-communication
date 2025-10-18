package dev.locationapp.feature.location.presentation

import dev.abbasian.protocol.domain.model.LocationData

data class LocationListState(
    val locations: List<LocationData> = emptyList(),
    val isLoading: Boolean = true,
    val isServiceRunning: Boolean = false,
    val error: String? = null,
    val locationCount: Int = 0,
)
