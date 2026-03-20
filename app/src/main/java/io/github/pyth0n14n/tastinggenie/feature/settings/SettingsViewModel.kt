package io.github.pyth0n14n.tastinggenie.feature.settings

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

        suspend fun exportJson(): String? {
            _uiState.update { it.copy(isProcessingTransfer = true, messageResId = null, error = null) }
            return try {
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
                    }.getOrNull()
            } catch (throwable: CancellationException) {
                clearTransferInProgress()
                throw throwable
            }
        }

        fun completeExport(writeResult: Result<Unit>) {
            writeResult
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isProcessingTransfer = false,
                            messageResId = R.string.message_export_success,
                            error = null,
                        )
                    }
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
                }
        }

        suspend fun importJson(rawJson: String) {
            _uiState.update { it.copy(isProcessingTransfer = true, messageResId = null, error = null) }
            try {
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
                    }.onFailure { throwable ->
                        _uiState.update {
                            it.copy(
                                isProcessingTransfer = false,
                                error = mapImportError(throwable),
                            )
                        }
                    }
            } catch (throwable: CancellationException) {
                clearTransferInProgress()
                throw throwable
            }
        }

        fun onImportFailed(throwable: Throwable) {
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
        }

        fun clearMessage() {
            _uiState.update { it.copy(messageResId = null) }
        }

        private fun clearTransferInProgress() {
            _uiState.update { it.copy(isProcessingTransfer = false) }
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
