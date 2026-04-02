package io.github.pyth0n14n.tastinggenie.domain.model

data class SakeDeleteResult(
    val isDeleted: Boolean,
    val imageCleanupErrorCauseKey: String? = null,
) {
    val hasImageCleanupError: Boolean
        get() = imageCleanupErrorCauseKey != null
}
