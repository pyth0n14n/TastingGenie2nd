package io.github.pyth0n14n.tastinggenie.feature.review.image

import io.github.pyth0n14n.tastinggenie.domain.model.UiError

data class ReviewImageUiState(
    val isLoading: Boolean = true,
    val error: UiError? = null,
    val imageUris: List<String> = emptyList(),
)
