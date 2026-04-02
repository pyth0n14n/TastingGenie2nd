package io.github.pyth0n14n.tastinggenie.domain.model

data class SakeDeleteResult(
    val isDeleted: Boolean,
    val hasImageCleanupError: Boolean = false,
    val imageCleanupErrorCauseKey: String? = null,
)
