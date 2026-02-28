package io.github.pyth0n14n.tastinggenie.domain.model.enums

import kotlinx.serialization.Serializable

@Serializable
enum class SakeGrade {
    JUNMAI_DAIGINJO,
    DAIGINJO,
    JUNMAI_GINJO,
    GINJO,
    TOKUBETSU_JUNMAI,
    TOKUBETSU_HONJOZO,
    JUNMAI,
    HONJOZO,
    OTHER,
}
