package io.github.pyth0n14n.tastinggenie.feature.review.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.AppSettings
import io.github.pyth0n14n.tastinggenie.domain.model.MasterDataBundle
import io.github.pyth0n14n.tastinggenie.domain.model.Review
import io.github.pyth0n14n.tastinggenie.domain.model.UiError
import io.github.pyth0n14n.tastinggenie.domain.repository.MasterDataRepository
import io.github.pyth0n14n.tastinggenie.domain.repository.ReviewRepository
import io.github.pyth0n14n.tastinggenie.domain.repository.SakeFoodReviewRepository
import io.github.pyth0n14n.tastinggenie.domain.repository.SakeRepository
import io.github.pyth0n14n.tastinggenie.domain.repository.SettingsRepository
import io.github.pyth0n14n.tastinggenie.navigation.AppDestination
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ReviewListViewModel
    @javax.inject.Inject
    constructor(
        private val savedStateHandle: SavedStateHandle,
        private val sakeRepository: SakeRepository,
        private val reviewRepository: ReviewRepository,
        private val foodReviewRepository: SakeFoodReviewRepository,
        private val masterDataRepository: MasterDataRepository,
        private val settingsRepository: SettingsRepository = NoOpSettingsRepository,
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
                            loadError = UiError(messageResId = R.string.error_load_sake),
                        )
                    }
                    return@launch
                }

                val labels = loadReviewListLabels(sakeId) ?: return@launch
                val sake =
                    runCatching { sakeRepository.getSake(sakeId) }.getOrElse { throwable ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                sakeId = sakeId,
                                loadError =
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
                            loadError =
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
                    hasSakeImage = sake.imageUris.isNotEmpty(),
                    labels = labels,
                )
            }
        }

        private suspend fun loadReviewListLabels(sakeId: Long): ReviewListLabels? =
            runCatching {
                masterDataRepository.getMasterData().toReviewListLabels()
            }.getOrElse { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        sakeId = sakeId,
                        loadError =
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
            labels: ReviewListLabels,
        ) {
            reviewRepository
                .observeReviews(sakeId)
                .combine(foodReviewRepository.observeFoodReviews(sakeId)) { reviews, foodReviews ->
                    reviews to foodReviews
                }.combine(settingsRepository.observeSettings()) { reviewLists, settings ->
                    Triple(reviewLists.first, reviewLists.second, settings)
                }.catch { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            sakeId = sakeId,
                            sakeName = sakeName,
                            hasSakeImage = hasSakeImage,
                            overallReviewLabels = labels.overallReviewLabels,
                            temperatureLabels = labels.temperatureLabels,
                            aromaLabels = labels.aromaLabels,
                            tasteLabels = labels.tasteLabels,
                            loadError =
                                UiError(
                                    messageResId = R.string.error_load_reviews,
                                    causeKey = throwable.message,
                                ),
                        )
                    }
                }.collect { (reviews, foodReviews, settings) ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadError = null,
                            deleteError = null,
                            sakeId = sakeId,
                            sakeName = sakeName,
                            hasSakeImage = hasSakeImage,
                            reviews = reviews,
                            foodReviews = foodReviews,
                            overallReviewLabels = labels.overallReviewLabels,
                            temperatureLabels = labels.temperatureLabels,
                            aromaLabels = labels.aromaLabels,
                            tasteLabels = labels.tasteLabels,
                            isSakeMissing = false,
                            onboardingCompleted = settings.onboardingCompleted,
                            reviewEmptyFabCoachmarkSeen = settings.reviewEmptyFabCoachmarkSeen,
                        )
                    }
                    markReviewCoachmarkSeenAfterDataAdded(reviews, settings)
                }
        }

        private suspend fun markReviewCoachmarkSeenAfterDataAdded(
            reviews: List<Review>,
            settings: AppSettings,
        ) {
            if (reviews.isNotEmpty() && settings.onboardingCompleted && !settings.reviewEmptyFabCoachmarkSeen) {
                runCatching {
                    settingsRepository.updateReviewEmptyFabCoachmarkSeen(seen = true)
                }.onFailure { throwable ->
                    if (throwable is CancellationException) {
                        throw throwable
                    }
                }
            }
        }

        fun addReviewFromFab(onAddReview: () -> Unit) {
            viewModelScope.launch {
                if (_uiState.value.shouldShowReviewEmptyFabCoachmark) {
                    settingsRepository.updateReviewEmptyFabCoachmarkSeen(seen = true)
                }
                onAddReview()
            }
        }

        fun dismissReviewEmptyFabCoachmark() {
            viewModelScope.launch {
                settingsRepository.updateReviewEmptyFabCoachmarkSeen(seen = true)
            }
        }

        @Suppress("TooGenericExceptionCaught")
        fun deleteReview(reviewId: Long) {
            viewModelScope.launch {
                _uiState.update { it.copy(deleteError = null) }
                try {
                    val deleted = reviewRepository.deleteReview(reviewId)
                    if (!deleted) {
                        _uiState.update {
                            it.copy(
                                deleteError =
                                    UiError(
                                        messageResId = R.string.error_delete_review,
                                        causeKey = reviewId.toString(),
                                    ),
                            )
                        }
                    }
                } catch (throwable: CancellationException) {
                    throw throwable
                } catch (throwable: Exception) {
                    _uiState.update {
                        it.copy(
                            deleteError =
                                UiError(
                                    messageResId = R.string.error_delete_review,
                                    causeKey = throwable.message,
                                ),
                        )
                    }
                }
            }
        }

        @Suppress("TooGenericExceptionCaught")
        fun deleteFoodReview(reviewId: Long) {
            viewModelScope.launch {
                _uiState.update { it.copy(deleteError = null) }
                try {
                    val deleted = foodReviewRepository.deleteFoodReview(reviewId)
                    if (!deleted) {
                        _uiState.update {
                            it.copy(
                                deleteError =
                                    UiError(
                                        messageResId = R.string.error_delete_food_review,
                                        causeKey = reviewId.toString(),
                                    ),
                            )
                        }
                    }
                } catch (throwable: CancellationException) {
                    throw throwable
                } catch (throwable: Exception) {
                    _uiState.update {
                        it.copy(
                            deleteError =
                                UiError(
                                    messageResId = R.string.error_delete_food_review,
                                    causeKey = throwable.message,
                                ),
                        )
                    }
                }
            }
        }
    }

private object NoOpSettingsRepository : SettingsRepository {
    override fun observeSettings() = MutableStateFlow(AppSettings())

    override suspend fun getCurrentSettings() = AppSettings()

    override suspend fun updateShowHelpHints(enabled: Boolean) = Unit

    override suspend fun updateShowReviewSoundness(enabled: Boolean) = Unit

    override suspend fun updateReviewMode(modeId: String) = Unit

    override suspend fun replaceSettings(settings: AppSettings) = Unit
}

private fun SavedStateHandle.reviewListSakeId(): Long = get<Long>(AppDestination.ARG_SAKE_ID) ?: AppDestination.NO_ID

private data class ReviewListLabels(
    val overallReviewLabels: Map<String, String>,
    val temperatureLabels: Map<String, String>,
    val aromaLabels: Map<String, String>,
    val tasteLabels: Map<String, String>,
)

private fun MasterDataBundle.toReviewListLabels(): ReviewListLabels =
    ReviewListLabels(
        overallReviewLabels = overallReviews.associate { option -> option.value to option.label },
        temperatureLabels = temperatures.associate { option -> option.value to option.label },
        aromaLabels =
            aromaCategories
                .flatMap { category -> category.items }
                .associate { option -> option.value to option.label },
        tasteLabels = tasteLevels.associate { option -> option.value to option.label },
    )
