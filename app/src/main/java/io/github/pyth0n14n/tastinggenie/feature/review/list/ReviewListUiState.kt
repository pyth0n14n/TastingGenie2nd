package io.github.pyth0n14n.tastinggenie.feature.review.list

import io.github.pyth0n14n.tastinggenie.domain.model.Review
import io.github.pyth0n14n.tastinggenie.domain.model.SakeId
import io.github.pyth0n14n.tastinggenie.domain.model.UiError

data class ReviewListUiState(
    val isLoading: Boolean = true,
    val error: UiError? = null,
    val sakeId: SakeId? = null,
    val sakeName: String = "",
    val hasSakeImage: Boolean = false,
    val reviews: List<Review> = emptyList(),
    val overallReviewLabels: Map<String, String> = emptyMap(),
    val isSakeMissing: Boolean = false,
)
