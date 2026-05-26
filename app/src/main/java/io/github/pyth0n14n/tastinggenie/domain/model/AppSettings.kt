package io.github.pyth0n14n.tastinggenie.domain.model

data class AppSettings(
    val showHelpHints: Boolean = true,
    val showReviewSoundness: Boolean = false,
    val reviewModeId: String = ReviewMode.NORMAL.id,
    val onboardingCompleted: Boolean = false,
    val sakeEmptyFabCoachmarkSeen: Boolean = false,
    val reviewEmptyFabCoachmarkSeen: Boolean = false,
    val hasSeenTastingGuide: Boolean = false,
)
