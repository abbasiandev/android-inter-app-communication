package dev.internetapp.presentation

sealed class CommandIntent {
    object StartService : CommandIntent()
    object StopService : CommandIntent()
    object GetAllLocations : CommandIntent()
    object GetLatestLocation : CommandIntent()
    object ClearError : CommandIntent()
    object ClearResponse : CommandIntent()
}