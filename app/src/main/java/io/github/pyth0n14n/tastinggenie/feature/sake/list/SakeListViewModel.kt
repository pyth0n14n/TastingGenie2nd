package io.github.pyth0n14n.tastinggenie.feature.sake.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.Sake
import io.github.pyth0n14n.tastinggenie.domain.model.UiError
import io.github.pyth0n14n.tastinggenie.domain.repository.MasterDataRepository
import io.github.pyth0n14n.tastinggenie.domain.repository.ReviewRepository
import io.github.pyth0n14n.tastinggenie.domain.repository.SakeRepository
import io.github.pyth0n14n.tastinggenie.domain.repository.SettingsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SakeListViewModel
    @Inject
    constructor(
        private val sakeRepository: SakeRepository,
        private val reviewRepository: ReviewRepository,
        private val masterDataRepository: MasterDataRepository,
        private val settingsRepository: SettingsRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(SakeListUiState())
        val uiState: StateFlow<SakeListUiState> = _uiState.asStateFlow()
        private var deleteRequestJob: Job? = null
        private var latestDeleteRequestId: Long = 0L

        init {
            loadInitial()
        }

        private fun loadInitial() {
            viewModelScope.launch {
                val labels =
                    runCatching {
                        masterDataRepository.getMasterData().let { master ->
                            master.sakeGrades.associate { option -> option.value to option.label } to
                                master.overallReviews.associate { option -> option.value to option.label }
                        }
                    }.getOrElse { throwable ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error =
                                    UiError(
                                        messageResId = R.string.error_load_sakes,
                                        causeKey = throwable.message,
                                    ),
                            )
                        }
                        return@launch
                    }

                val gradeLabels = labels.first
                val overallReviewLabels = labels.second

                // DB監視結果をそのままUiStateへ反映し、一覧再表示を自動化する。
                sakeRepository
                    .observeSakeListSummaries()
                    .combine(settingsRepository.observeSettings()) { sakes, settings -> sakes to settings }
                    .catch { throwable ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error =
                                    UiError(
                                        messageResId = R.string.error_load_sakes,
                                        causeKey = throwable.message,
                                    ),
                            )
                        }
                    }.collect { (sakes, settings) ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = null,
                                sakes = sakes,
                                gradeLabels = gradeLabels,
                                overallReviewLabels = overallReviewLabels,
                                showHelpHints = settings.showHelpHints,
                                showImagePreview = settings.showImagePreview,
                            )
                        }
                    }
            }
        }

        fun requestDeleteSake(sakeId: Long) {
            deleteRequestJob?.cancel()
            val requestId = ++latestDeleteRequestId
            deleteRequestJob =
                viewModelScope.launch {
                    _uiState.update { it.copy(deleteError = null, pendingDeleteSake = null) }
                    when (val target = loadSakeForDeletion(sakeId)) {
                        DeleteTargetLoadResult.Failed -> return@launch
                        DeleteTargetLoadResult.Missing -> {
                            showDeleteError(causeKey = sakeId.toString())
                            return@launch
                        }

                        is DeleteTargetLoadResult.Loaded -> {
                            val reviewCount = loadReviewCountForDeletion(sakeId) ?: return@launch
                            if (!isLatestDeleteRequest(requestId)) {
                                return@launch
                            }
                            _uiState.update {
                                it.copy(
                                    pendingDeleteSake =
                                        PendingDeleteSake(
                                            sakeId = target.sake.id,
                                            sakeName = target.sake.name,
                                            reviewCount = reviewCount,
                                            hasImage = !target.sake.imageUri.isNullOrBlank(),
                                        ),
                                )
                            }
                        }
                    }
                }
        }

        fun dismissDeleteSakeDialog() {
            _uiState.update { it.copy(pendingDeleteSake = null) }
        }

        @Suppress("TooGenericExceptionCaught")
        fun togglePinned(
            sakeId: Long,
            isPinned: Boolean,
        ) {
            viewModelScope.launch {
                _uiState.update { it.copy(deleteError = null) }
                try {
                    sakeRepository.setPinned(id = sakeId, isPinned = isPinned)
                } catch (throwable: CancellationException) {
                    throw throwable
                } catch (throwable: Exception) {
                    _uiState.update {
                        it.copy(
                            deleteError =
                                UiError(
                                    messageResId = R.string.error_save_sake,
                                    causeKey = throwable.message,
                                ),
                        )
                    }
                }
            }
        }

        @Suppress("TooGenericExceptionCaught")
        fun confirmDeleteSake() {
            val pendingDeleteSake = _uiState.value.pendingDeleteSake ?: return
            viewModelScope.launch {
                _uiState.update { it.copy(deleteError = null, pendingDeleteSake = null) }
                try {
                    val result = sakeRepository.deleteSake(pendingDeleteSake.sakeId)
                    when {
                        !result.isDeleted ->
                            showDeleteError(causeKey = pendingDeleteSake.sakeId.toString())

                        result.hasImageCleanupError ->
                            showDeleteError(
                                messageResId = R.string.error_delete_sake_image_cleanup,
                                causeKey = result.imageCleanupErrorCauseKey,
                            )
                    }
                } catch (throwable: CancellationException) {
                    throw throwable
                } catch (throwable: Exception) {
                    showDeleteError(causeKey = throwable.message)
                }
            }
        }

        @Suppress("TooGenericExceptionCaught")
        private suspend fun loadSakeForDeletion(sakeId: Long): DeleteTargetLoadResult =
            try {
                val sake =
                    _uiState.value.sakes
                        .firstOrNull { existing -> existing.sake.id == sakeId }
                        ?.sake
                        ?: sakeRepository.getSake(sakeId)
                if (sake == null) {
                    DeleteTargetLoadResult.Missing
                } else {
                    DeleteTargetLoadResult.Loaded(sake)
                }
            } catch (throwable: CancellationException) {
                throw throwable
            } catch (throwable: Exception) {
                showDeleteError(causeKey = throwable.message)
                DeleteTargetLoadResult.Failed
            }

        @Suppress("TooGenericExceptionCaught")
        private suspend fun loadReviewCountForDeletion(sakeId: Long): Int? =
            try {
                reviewRepository.observeReviews(sakeId).first().size
            } catch (throwable: CancellationException) {
                throw throwable
            } catch (throwable: Exception) {
                showDeleteError(causeKey = throwable.message)
                null
            }

        private fun showDeleteError(
            messageResId: Int = R.string.error_delete_sake,
            causeKey: String?,
        ) {
            _uiState.update {
                it.copy(
                    deleteError =
                        UiError(
                            messageResId = messageResId,
                            causeKey = causeKey,
                        ),
                )
            }
        }

        private fun isLatestDeleteRequest(requestId: Long): Boolean = requestId == latestDeleteRequestId
    }

private sealed interface DeleteTargetLoadResult {
    data class Loaded(
        val sake: Sake,
    ) : DeleteTargetLoadResult

    data object Missing : DeleteTargetLoadResult

    data object Failed : DeleteTargetLoadResult
}
