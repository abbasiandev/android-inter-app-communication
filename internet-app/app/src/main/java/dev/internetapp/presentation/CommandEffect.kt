package dev.internetapp.presentation

sealed class CommandEffect {
    data class ShowToast(val message: String) : CommandEffect()
    data class ShowError(val error: String) : CommandEffect()
    data class ShowSuccess(val message: String) : CommandEffect()
}