package io.github.pyth0n14n.tastinggenie.domain.model.enums

import kotlinx.serialization.Serializable

@Serializable
enum class SweetDryness {
    SWEET,
    MEDIUM_SWEET,
    MEDIUM,
    MEDIUM_DRY,
    DRY,
}
