package io.github.pyth0n14n.tastinggenie.feature.review.detail

import io.github.pyth0n14n.tastinggenie.domain.model.Review
import io.github.pyth0n14n.tastinggenie.domain.model.UiError

data class ReviewDetailUiState(
    val isLoading: Boolean = true,
    val error: UiError? = null,
    val sakeName: String = "",
    val review: Review? = null,
    val temperatureLabels: Map<String, String> = emptyMap(),
    val colorLabels: Map<String, String> = emptyMap(),
    val intensityLabels: Map<String, String> = emptyMap(),
    val tasteLabels: Map<String, String> = emptyMap(),
    val overallReviewLabels: Map<String, String> = emptyMap(),
    val aromaLabels: Map<String, String> = emptyMap(),
)
