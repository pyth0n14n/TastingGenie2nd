package io.github.pyth0n14n.tastinggenie.domain.model.enums

import kotlinx.serialization.Serializable

@Serializable
enum class FoodCompatibility {
    BAD,
    SLIGHTLY_BAD,
    MEDIUM,
    SLIGHTLY_GOOD,
    GOOD,
}
