package io.github.pyth0n14n.tastinggenie.feature.review.detail

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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewDetailViewModel
    @Inject
    constructor(
        private val savedStateHandle: SavedStateHandle,
        private val reviewRepository: ReviewRepository,
        private val sakeRepository: SakeRepository,
        private val masterDataRepository: MasterDataRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ReviewDetailUiState())
        val uiState: StateFlow<ReviewDetailUiState> = _uiState.asStateFlow()

        init {
            loadInitial()
        }

        private fun loadInitial() {
            viewModelScope.launch {
                val reviewId = savedStateHandle.reviewIdOrDefault()
                if (reviewId == AppDestination.NO_ID) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = UiError(messageResId = R.string.error_load_review),
                        )
                    }
                    return@launch
                }

                runCatching {
                    val master = masterDataRepository.getMasterData()
                    val review = reviewRepository.getReview(reviewId)
                    val sake = review?.let { sakeRepository.getSake(it.sakeId) }
                    Triple(master, review, sake)
                }.onSuccess { (master, review, sake) ->
                    if (review == null || sake == null) {
                        _uiState.update { it.toMissingReviewState(reviewId) }
                        return@onSuccess
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            sakeName = sake.name,
                            review = review,
                            temperatureLabels = master.temperatures.labelMap(),
                            colorLabels = master.colors.labelMap(),
                            intensityLabels = master.intensityLevels.labelMap(),
                            tasteLabels = master.tasteLevels.labelMap(),
                            overallReviewLabels = master.overallReviews.labelMap(),
                            aromaLabels = master.aromaCategories.flatMap { category -> category.items }.labelMap(),
                        )
                    }
                }.onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error =
                                UiError(
                                    messageResId = R.string.error_load_review,
                                    causeKey = throwable.message,
                                ),
                        )
                    }
                }
            }
        }
    }

private fun SavedStateHandle.reviewIdOrDefault(): Long = get<Long>(AppDestination.ARG_REVIEW_ID) ?: AppDestination.NO_ID

private fun ReviewDetailUiState.toMissingReviewState(reviewId: Long): ReviewDetailUiState =
    copy(
        isLoading = false,
        error =
            UiError(
                messageResId = R.string.error_load_review,
                causeKey = reviewId.toString(),
            ),
    )

private fun List<io.github.pyth0n14n.tastinggenie.domain.model.MasterOption>.labelMap(): Map<String, String> =
    associate { option -> option.value to option.label }
