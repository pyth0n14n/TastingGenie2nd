package io.github.pyth0n14n.tastinggenie.feature.review.edit

sealed interface ReviewEditAction {
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
}

enum class ReviewTextField {
    DATE,
    BAR,
    PRICE,
    VOLUME,
    SCENE,
    DISH,
    COMMENT,
}

enum class ReviewSelectionField {
    TEMPERATURE,
    COLOR,
    VISCOSITY,
    INTENSITY,
    SWEET,
    SOUR,
    BITTER,
    UMAMI,
    SHARP,
    OVERALL_REVIEW,
}

enum class ReviewAromaField {
    TOP,
    BASE,
    MOUTH,
}
