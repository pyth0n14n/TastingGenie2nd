package io.github.pyth0n14n.tastinggenie.feature.settings

import io.github.pyth0n14n.tastinggenie.domain.model.AppSettings
import io.github.pyth0n14n.tastinggenie.domain.model.UiError

data class SettingsUiState(
    val isLoading: Boolean = true,
    val isProcessingTransfer: Boolean = false,
    val settings: AppSettings = AppSettings(),
    val messageResId: Int? = null,
    val error: UiError? = null,
)
