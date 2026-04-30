package io.github.pyth0n14n.tastinggenie.feature.review.list

import io.github.pyth0n14n.tastinggenie.domain.model.Review
import io.github.pyth0n14n.tastinggenie.domain.model.SakeId
import io.github.pyth0n14n.tastinggenie.domain.model.UiError
import java.util.Locale

data class ReviewListUiState(
    val isLoading: Boolean = true,
    val loadError: UiError? = null,
    val deleteError: UiError? = null,
    val sakeId: SakeId? = null,
    val sakeName: String = "",
    val hasSakeImage: Boolean = false,
    val reviews: List<Review> = emptyList(),
    val overallReviewLabels: Map<String, String> = emptyMap(),
    val temperatureLabels: Map<String, String> = emptyMap(),
    val aromaLabels: Map<String, String> = emptyMap(),
    val tasteLabels: Map<String, String> = emptyMap(),
    val isSakeMissing: Boolean = false,
) {
    val reviewCount: Int
        get() = reviews.size

    val averageOverallReviewText: String
        get() {
            val ratings = reviews.mapNotNull { review -> review.overallReviewScore }
            val average = ratings.average().takeIf { value -> !value.isNaN() } ?: 0.0
            return String.format(Locale.US, "%.2f", average)
        }
}

private val Review.overallReviewScore: Int?
    get() = otherOverallReview?.let { review -> review.ordinal + 1 }
