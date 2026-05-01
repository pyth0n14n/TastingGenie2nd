package io.github.pyth0n14n.tastinggenie.feature.review.edit

import io.github.pyth0n14n.tastinggenie.domain.model.enums.ComplexityLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.IntensityLevel

sealed interface ReviewEditAction {
    data class DateSelected(
        val epochMillis: Long,
    ) : ReviewEditAction

    data class TextChanged(
        val field: ReviewTextField,
        val value: String,
    ) : ReviewEditAction

    data class SelectionChanged(
        val field: ReviewSelectionField,
        val value: String,
    ) : ReviewEditAction

    data class AromaToggled(
        val field: ReviewAromaField,
        val value: String,
    ) : ReviewEditAction

    data class FlavorProfileSelected(
        val intensity: IntensityLevel,
        val complexity: ComplexityLevel,
    ) : ReviewEditAction
}

enum class ReviewTextField {
    DATE,
    BAR,
    PRICE,
    VOLUME,
    AROMA_MAIN_NOTE,
    TASTE_MAIN_NOTE,
    OTHER_INDIVIDUALITY,
    OTHER_CAUTIONS,
    SCENE,
    DISH,
    COMMENT,
}

enum class ReviewSelectionField {
    APPEARANCE_SOUNDNESS,
    TEMPERATURE,
    COLOR,
    VISCOSITY,
    AROMA_SOUNDNESS,
    INTENSITY,
    AROMA_COMPLEXITY,
    TASTE_SOUNDNESS,
    TASTE_ATTACK,
    TASTE_TEXTURE_ROUNDNESS,
    TASTE_TEXTURE_SMOOTHNESS,
    SWEET,
    SOUR,
    BITTER,
    UMAMI,
    SHARP,
    TASTE_COMPLEXITY,
    OVERALL_REVIEW,
}

enum class ReviewAromaField {
    TOP,
    MOUTH,
}
