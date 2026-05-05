package io.github.pyth0n14n.tastinggenie.domain.model

data class AppSettings(
    val showHelpHints: Boolean = true,
    val showReviewSoundness: Boolean = true,
    val autoDeleteUnusedImages: Boolean = false,
    val reviewModeId: String = ReviewMode.NORMAL.id,
)
