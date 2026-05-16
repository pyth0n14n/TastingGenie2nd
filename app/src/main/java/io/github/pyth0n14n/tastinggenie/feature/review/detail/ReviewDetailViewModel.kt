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
import io.github.pyth0n14n.tastinggenie.domain.repository.SettingsRepository
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
        private val settingsRepository: SettingsRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ReviewDetailUiState())
        val uiState: StateFlow<ReviewDetailUiState> = _uiState.asStateFlow()

        init {
            refresh()
        }

        fun refresh() {
            val reviewId = savedStateHandle.reviewIdOrDefault()
            if (reviewId == AppDestination.NO_ID) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = UiError(messageResId = R.string.error_load_review),
                    )
                }
                return
            }

            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, error = null) }
                val loadedResult = runCatching { loadDetailData(reviewId) }
                val loaded = loadedResult.getOrNull()
                if (loaded != null) {
                    applyLoadedData(reviewId = reviewId, loaded = loaded)
                } else {
                    applyLoadFailure(loadedResult.exceptionOrNull())
                }
            }
        }

        private suspend fun loadDetailData(reviewId: Long): ReviewDetailLoadedData {
            val master = masterDataRepository.getMasterData()
            val settings = settingsRepository.getCurrentSettings()
            val review = reviewRepository.getReview(reviewId)
            val sake = review?.let { sakeRepository.getSake(it.sakeId) }
            return ReviewDetailLoadedData(master = master, settings = settings, review = review, sake = sake)
        }

        private fun applyLoadedData(
            reviewId: Long,
            loaded: ReviewDetailLoadedData,
        ) {
            val review = loaded.review
            val sake = loaded.sake
            if (review == null || sake == null) {
                _uiState.update { it.toMissingReviewState(reviewId) }
                return
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    sakeName = sake.name,
                    review = review,
                    showReviewSoundness = loaded.settings.showReviewSoundness,
                    temperatureLabels = loaded.master.temperatures.labelMap(),
                    colorLabels = loaded.master.colors.labelMap(),
                    intensityLabels = loaded.master.intensityLevels.labelMap(),
                    tasteLabels = loaded.master.tasteLevels.labelMap(),
                    overallReviewLabels = loaded.master.overallReviews.labelMap(),
                    aromaLabels =
                        loaded.master.aromaCategories
                            .flatMap { category -> category.items }
                            .labelMap(),
                )
            }
        }

        private fun applyLoadFailure(throwable: Throwable?) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error =
                        UiError(
                            messageResId = R.string.error_load_review,
                            causeKey = throwable?.message,
                        ),
                )
            }
        }
    }

private data class ReviewDetailLoadedData(
    val master: io.github.pyth0n14n.tastinggenie.domain.model.MasterDataBundle,
    val settings: io.github.pyth0n14n.tastinggenie.domain.model.AppSettings,
    val review: io.github.pyth0n14n.tastinggenie.domain.model.Review?,
    val sake: io.github.pyth0n14n.tastinggenie.domain.model.Sake?,
)

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
