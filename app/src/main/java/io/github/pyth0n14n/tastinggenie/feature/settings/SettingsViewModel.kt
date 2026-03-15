package io.github.pyth0n14n.tastinggenie.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.UiError
import io.github.pyth0n14n.tastinggenie.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val settingsRepository: SettingsRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(SettingsUiState())
        val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

        init {
            observeSettings()
        }

        fun toggleHelpHints(enabled: Boolean) {
            updateSetting { settingsRepository.updateShowHelpHints(enabled) }
        }

        fun toggleImagePreview(enabled: Boolean) {
            updateSetting { settingsRepository.updateShowImagePreview(enabled) }
        }

        private fun observeSettings() {
            viewModelScope.launch {
                settingsRepository
                    .observeSettings()
                    .catch { throwable ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error =
                                    UiError(
                                        messageResId = R.string.error_load_settings,
                                        causeKey = throwable.message,
                                    ),
                            )
                        }
                    }.collect { settings ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                settings = settings,
                                error = null,
                            )
                        }
                    }
            }
        }

        private fun updateSetting(action: suspend () -> Unit) {
            viewModelScope.launch {
                runCatching { action() }
                    .onFailure { throwable ->
                        _uiState.update {
                            it.copy(
                                error =
                                    UiError(
                                        messageResId = R.string.error_save_settings,
                                        causeKey = throwable.message,
                                    ),
                            )
                        }
                    }
            }
        }
    }
