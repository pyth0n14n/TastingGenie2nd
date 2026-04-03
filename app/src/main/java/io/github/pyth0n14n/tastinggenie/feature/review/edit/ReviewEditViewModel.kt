package io.github.pyth0n14n.tastinggenie.feature.review.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.ReviewInput
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
class ReviewEditViewModel
    @Inject
    constructor(
        private val savedStateHandle: SavedStateHandle,
        private val sakeRepository: SakeRepository,
        private val reviewRepository: ReviewRepository,
        private val masterDataRepository: MasterDataRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ReviewEditUiState())
        val uiState: StateFlow<ReviewEditUiState> = _uiState.asStateFlow()

        init {
            loadInitial()
        }

        fun onAction(action: ReviewEditAction) {
            _uiState.updateEditable { current ->
                when (action) {
                    is ReviewEditAction.DateSelected -> current.withDateSelected(action.epochMillis)
                    is ReviewEditAction.TextChanged -> current.withText(action.field, action.value)
                    is ReviewEditAction.SelectionChanged -> current.withSelection(action.field, action.value)
                    is ReviewEditAction.AromaToggled -> current.withAromaToggled(action.field, action.value)
                }
            }
        }

        fun save() {
            val snapshot = uiState.value
            if (snapshot.isInputLocked) {
                return
            }

            val sakeId = snapshot.sakeId
            val parsedDate = snapshot.date.toLocalDateOrNull()
            val parsedPrice = snapshot.price.toOptionalInt()
            val parsedVolume = snapshot.volume.toOptionalInt()
            val hasInvalidNumber = parsedPrice == INVALID_NUMBER || parsedVolume == INVALID_NUMBER
            if (sakeId == null || parsedDate == null || hasInvalidNumber) {
                _uiState.update { it.copy(error = UiError(messageResId = R.string.error_invalid_review_input)) }
                return
            }

            viewModelScope.launch {
                _uiState.update { it.copy(isSaving = true, error = null) }
                runCatching {
                    reviewRepository.upsertReview(
                        ReviewInput(
                            id = snapshot.reviewId,
                            sakeId = sakeId,
                            date = parsedDate,
                            bar = snapshot.bar.trimmedOrNull(),
                            price = parsedPrice,
                            volume = parsedVolume,
                            temperature = snapshot.temperature,
                            color = snapshot.color,
                            viscosity = snapshot.viscosity,
                            intensity = snapshot.intensity,
                            scentTop = snapshot.scentTop,
                            scentBase = snapshot.scentBase,
                            scentMouth = snapshot.scentMouth,
                            sweet = snapshot.sweet,
                            sour = snapshot.sour,
                            bitter = snapshot.bitter,
                            umami = snapshot.umami,
                            sharp = snapshot.sharp,
                            scene = snapshot.scene.trimmedOrNull(),
                            dish = snapshot.dish.trimmedOrNull(),
                            comment = snapshot.comment.trimmedOrNull(),
                            review = snapshot.review,
                        ),
                    )
                }.onSuccess {
                    _uiState.update { it.copy(isSaving = false, isSaved = true) }
                }.onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error =
                                UiError(
                                    messageResId = R.string.error_save_review,
                                    causeKey = throwable.message,
                                ),
                        )
                    }
                }
            }
        }

        fun consumeSaved() {
            _uiState.update { it.copy(isSaved = false) }
        }

        private fun loadInitial() {
            viewModelScope.launch {
                val args = savedStateHandle.toReviewArgs()
                if (args.sakeId == AppDestination.NO_ID) {
                    _uiState.update { it.toMissingSakeState() }
                    return@launch
                }

                runCatching {
                    loadReviewSeedData(args)
                }.onSuccess { loaded ->
                    _uiState.update { current -> current.toLoadedState(args, loaded) }
                }.onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            sakeId = args.sakeId,
                            reviewId = args.reviewId,
                            isEditTargetMissing = args.reviewId != null,
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

        private suspend fun loadReviewSeedData(args: ReviewEditArgs): ReviewSeedData =
            ReviewSeedData(
                master = masterDataRepository.getMasterData(),
                sake = sakeRepository.getSake(args.sakeId),
                review = args.reviewId?.let { reviewRepository.getReview(it) },
            )
    }

private fun String.trimmedOrNull(): String? = trim().takeIf { it.isNotEmpty() }
