package io.github.pyth0n14n.tastinggenie.feature.review.edit

fun ReviewEditUiState.clearSelection(field: ReviewSelectionField): ReviewEditUiState =
    when (field) {
        ReviewSelectionField.TEMPERATURE -> copy(temperature = null, error = null)
        ReviewSelectionField.COLOR -> copy(color = null, error = null)
        ReviewSelectionField.VISCOSITY -> copy(viscosity = null, error = null)
        ReviewSelectionField.INTENSITY -> copy(intensity = null, error = null)
        ReviewSelectionField.SWEET -> copy(sweet = null, error = null)
        ReviewSelectionField.SOUR -> copy(sour = null, error = null)
        ReviewSelectionField.BITTER -> copy(bitter = null, error = null)
        ReviewSelectionField.UMAMI -> copy(umami = null, error = null)
        ReviewSelectionField.SHARP -> copy(sharp = null, error = null)
        ReviewSelectionField.OVERALL_REVIEW -> copy(review = null, error = null)
    }
