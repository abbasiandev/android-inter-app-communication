package dev.internetapp.feature.responsedisplay.domain.model

sealed class CommandEffect {
    data class ShowToast(
        val message: String,
    ) : CommandEffect()

    data class ShowError(
        val error: String,
    ) : CommandEffect()

    data class ShowSuccess(
        val message: String,
    ) : CommandEffect()
}
