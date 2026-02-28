package io.github.pyth0n14n.tastinggenie.domain.model.enums

import kotlinx.serialization.Serializable

@Serializable
enum class OverallReview {
    VERY_BAD,
    BAD,
    NEUTRAL,
    GOOD,
    VERY_GOOD,
}
