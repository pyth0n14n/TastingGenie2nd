package io.github.pyth0n14n.tastinggenie.domain.model.enums

import kotlinx.serialization.Serializable

@Serializable
enum class TasteLevel {
    VERY_WEAK,
    WEAK,
    MEDIUM,
    STRONG,
    VERY_STRONG,
}
