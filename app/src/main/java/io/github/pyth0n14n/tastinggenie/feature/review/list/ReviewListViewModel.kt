package io.github.pyth0n14n.tastinggenie.feature.review.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.UiError
import io.github.pyth0n14n.tastinggenie.domain.repository.MasterDataRepository
import io.github.pyth0n14n.tastinggenie.domain.repository.ReviewRepository
import io.github.pyth0n14n.tastinggenie.domain.repository.SakeRepository
import io.github.pyth0n14n.tastinggenie.navigation.AppDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewListViewModel
    @Inject
    constructor(
        private val savedStateHandle: SavedStateHandle,
        private val sakeRepository: SakeRepository,
        private val reviewRepository: ReviewRepository,
        private val masterDataRepository: MasterDataRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ReviewListUiState())
        val uiState: StateFlow<ReviewListUiState> = _uiState.asStateFlow()

        init {
            loadInitial()
        }

        private fun loadInitial() {
            viewModelScope.launch {
                val sakeId = savedStateHandle.reviewListSakeId()
                if (sakeId == AppDestination.NO_ID) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSakeMissing = true,
                            error = UiError(messageResId = R.string.error_load_sake),
                        )
                    }
                    return@launch
                }

                val overallReviewLabels = loadOverallReviewLabels(sakeId) ?: return@launch
                val sake =
                    runCatching { sakeRepository.getSake(sakeId) }.getOrElse { throwable ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                sakeId = sakeId,
                                error =
                                    UiError(
                                        messageResId = R.string.error_load_sake,
                                        causeKey = throwable.message,
                                    ),
                            )
                        }
                        return@launch
                    }
                if (sake == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            sakeId = sakeId,
                            isSakeMissing = true,
                            error =
                                UiError(
                                    messageResId = R.string.error_load_sake,
                                    causeKey = sakeId.toString(),
                                ),
                        )
                    }
                    return@launch
                }
                observeReviews(
                    sakeId = sakeId,
                    sakeName = sake.name,
                    hasSakeImage = !sake.imageUri.isNullOrBlank(),
                    overallReviewLabels = overallReviewLabels,
                )
            }
        }

        private suspend fun loadOverallReviewLabels(sakeId: Long): Map<String, String>? =
            runCatching {
                masterDataRepository
                    .getMasterData()
                    .overallReviews
                    .associate { option -> option.value to option.label }
            }.getOrElse { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        sakeId = sakeId,
                        error =
                            UiError(
                                messageResId = R.string.error_load_reviews,
                                causeKey = throwable.message,
                            ),
                    )
                }
                null
            }

        private suspend fun observeReviews(
            sakeId: Long,
            sakeName: String,
            hasSakeImage: Boolean,
            overallReviewLabels: Map<String, String>,
        ) {
            reviewRepository
                .observeReviews(sakeId)
                .catch { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            sakeId = sakeId,
                            sakeName = sakeName,
                            hasSakeImage = hasSakeImage,
                            overallReviewLabels = overallReviewLabels,
                            error =
                                UiError(
                                    messageResId = R.string.error_load_reviews,
                                    causeKey = throwable.message,
                                ),
                        )
                    }
                }.collect { reviews ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = null,
                            sakeId = sakeId,
                            sakeName = sakeName,
                            hasSakeImage = hasSakeImage,
                            reviews = reviews,
                            overallReviewLabels = overallReviewLabels,
                            isSakeMissing = false,
                        )
                    }
                }
        }
    }

private fun SavedStateHandle.reviewListSakeId(): Long = get<Long>(AppDestination.ARG_SAKE_ID) ?: AppDestination.NO_ID
