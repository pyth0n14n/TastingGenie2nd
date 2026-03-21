package io.github.pyth0n14n.tastinggenie.feature.settings

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.InvalidBackupReferenceException
import io.github.pyth0n14n.tastinggenie.domain.model.UiError
import io.github.pyth0n14n.tastinggenie.domain.model.UnsupportedSchemaVersionException
import io.github.pyth0n14n.tastinggenie.domain.repository.ImportExportRepository
import io.github.pyth0n14n.tastinggenie.domain.repository.SettingsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val settingsRepository: SettingsRepository,
        private val importExportRepository: ImportExportRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(SettingsUiState())
        private var isSettingsVisible = false
        private var hasUnseenTransferFeedback = false
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

        fun exportBackup(writeJson: suspend (String) -> Result<Unit>) {
            hasUnseenTransferFeedback = false
            _uiState.update { it.copy(isProcessingTransfer = true, messageResId = null, error = null) }
            viewModelScope.launch {
                try {
                    val rawJson = exportJsonPayload() ?: return@launch
                    writeJson(rawJson)
                        .onSuccess {
                            _uiState.update {
                                it.copy(
                                    isProcessingTransfer = false,
                                    messageResId = R.string.message_export_success,
                                    error = null,
                                )
                            }
                            hasUnseenTransferFeedback = !isSettingsVisible
                        }.onFailure { throwable ->
                            _uiState.update {
                                it.copy(
                                    isProcessingTransfer = false,
                                    error =
                                        UiError(
                                            messageResId = R.string.error_export_failed,
                                            causeKey = throwable.message,
                                        ),
                                )
                            }
                            hasUnseenTransferFeedback = !isSettingsVisible
                        }
                } catch (throwable: CancellationException) {
                    _uiState.update { it.copy(isProcessingTransfer = false) }
                    throw throwable
                }
            }
        }

        fun importBackup(readJson: suspend () -> Result<String>) {
            hasUnseenTransferFeedback = false
            _uiState.update { it.copy(isProcessingTransfer = true, messageResId = null, error = null) }
            viewModelScope.launch {
                try {
                    val rawJson =
                        readJson().getOrElse { throwable ->
                            _uiState.update {
                                it.copy(
                                    isProcessingTransfer = false,
                                    error =
                                        UiError(
                                            messageResId = R.string.error_import_failed,
                                            causeKey = throwable.message,
                                        ),
                                )
                            }
                            hasUnseenTransferFeedback = !isSettingsVisible
                            return@launch
                        }
                    importJsonPayload(rawJson)
                } catch (throwable: CancellationException) {
                    _uiState.update { it.copy(isProcessingTransfer = false) }
                    throw throwable
                }
            }
        }

        fun clearTransferFeedback() {
            hasUnseenTransferFeedback = false
            _uiState.update { current ->
                if (current.isProcessingTransfer) {
                    current
                } else {
                    current.copy(
                        messageResId = null,
                        error = current.error?.takeUnless { error -> error.messageResId.isTransferFeedbackMessage() },
                    )
                }
            }
        }

        fun setSettingsVisible(visible: Boolean) {
            isSettingsVisible = visible
            if (!visible || uiState.value.isProcessingTransfer) {
                return
            }
            if (uiState.value.hasTransferFeedback()) {
                if (hasUnseenTransferFeedback) {
                    hasUnseenTransferFeedback = false
                } else {
                    clearTransferFeedback()
                }
            }
        }

        private suspend fun exportJsonPayload(): String? =
            importExportRepository
                .exportJson()
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isProcessingTransfer = false,
                            error =
                                UiError(
                                    messageResId = R.string.error_export_failed,
                                    causeKey = throwable.message,
                                ),
                        )
                    }
                    hasUnseenTransferFeedback = !isSettingsVisible
                }.getOrNull()

        private suspend fun importJsonPayload(rawJson: String) {
            importExportRepository
                .importJson(rawJson)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isProcessingTransfer = false,
                            messageResId = R.string.message_import_success,
                            error = null,
                        )
                    }
                    hasUnseenTransferFeedback = !isSettingsVisible
                }.onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isProcessingTransfer = false,
                            error = mapImportError(throwable),
                        )
                    }
                    hasUnseenTransferFeedback = !isSettingsVisible
                }
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

private fun mapImportError(throwable: Throwable): UiError {
    val messageResId =
        when (throwable) {
            is UnsupportedSchemaVersionException -> R.string.error_import_unsupported_version
            is SerializationException -> R.string.error_import_invalid_json
            is InvalidBackupReferenceException,
            is IllegalArgumentException,
            -> R.string.error_import_invalid_payload
            else -> R.string.error_import_failed
        }
    return UiError(messageResId = messageResId, causeKey = throwable.message)
}

private fun SettingsUiState.hasTransferFeedback(): Boolean =
    messageResId != null || error?.messageResId?.isTransferFeedbackMessage() == true

@StringRes
private fun Int.isTransferFeedbackMessage(): Boolean =
    this == R.string.error_export_failed ||
        this == R.string.error_import_failed ||
        this == R.string.error_import_invalid_json ||
        this == R.string.error_import_unsupported_version ||
        this == R.string.error_import_invalid_payload
