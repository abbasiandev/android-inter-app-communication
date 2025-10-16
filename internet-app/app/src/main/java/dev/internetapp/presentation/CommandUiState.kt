package dev.internetapp.presentation

import dev.abbasian.protocol.LocationData

data class CommandUiState(
    val isLoading: Boolean = false,
    val lastResponse: String? = null,
    val locations: List<LocationData> = emptyList(),
    val latestLocation: LocationData? = null,
    val error: String? = null,
    val serviceStatus: ServiceStatus = ServiceStatus.UNKNOWN,
)

enum class ServiceStatus {
    UNKNOWN,
    RUNNING,
    STOPPED,
}
