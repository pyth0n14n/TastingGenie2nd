package io.github.pyth0n14n.tastinggenie.feature.review.image

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.UiError
import io.github.pyth0n14n.tastinggenie.domain.repository.ReviewRepository
import io.github.pyth0n14n.tastinggenie.navigation.AppDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewImageViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val reviewRepository: ReviewRepository,
    ) : ViewModel() {
        private val reviewId = savedStateHandle.get<Long>(AppDestination.ARG_REVIEW_ID) ?: AppDestination.NO_ID

        private val _uiState = MutableStateFlow(ReviewImageUiState())
        val uiState: StateFlow<ReviewImageUiState> = _uiState.asStateFlow()

        init {
            loadImage()
        }

        private fun loadImage() {
            viewModelScope.launch {
                runCatching {
                    reviewRepository.getReview(reviewId)?.imageUri
                        ?: error("missing review image")
                }.onSuccess { imageUri ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            imageUri = imageUri,
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
