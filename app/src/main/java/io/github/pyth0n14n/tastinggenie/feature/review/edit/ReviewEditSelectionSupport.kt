package io.github.pyth0n14n.tastinggenie.feature.review.edit

import io.github.pyth0n14n.tastinggenie.domain.model.enums.ReviewSoundness

fun ReviewEditUiState.clearSelection(field: ReviewSelectionField): ReviewEditUiState =
    clearSoundnessSelection(field)
        ?: clearChoiceSelection(field)
        ?: clearTasteSelection(field)
        ?: clearRatingSelection(field)
        ?: this

private fun ReviewEditUiState.clearSoundnessSelection(field: ReviewSelectionField): ReviewEditUiState? =
    when (field) {
        ReviewSelectionField.APPEARANCE_SOUNDNESS ->
            copy(
                appearanceSoundness = ReviewSoundness.SOUND,
                error = null,
            )
        ReviewSelectionField.AROMA_SOUNDNESS ->
            copy(
                aromaSoundness = ReviewSoundness.SOUND,
                error = null,
            )
        ReviewSelectionField.TASTE_SOUNDNESS ->
            copy(
                tasteSoundness = ReviewSoundness.SOUND,
                error = null,
            )
        else -> null
    }

private fun ReviewEditUiState.clearChoiceSelection(field: ReviewSelectionField): ReviewEditUiState? =
    when (field) {
        ReviewSelectionField.TEMPERATURE -> copy(temperature = null, error = null)
        ReviewSelectionField.COLOR -> copy(color = null, error = null)
        ReviewSelectionField.VISCOSITY -> copy(viscosity = null, error = null)
        ReviewSelectionField.INTENSITY -> copy(intensity = null, error = null)
        ReviewSelectionField.AROMA_COMPLEXITY -> copy(aromaComplexity = null, error = null)
        ReviewSelectionField.TASTE_ATTACK -> copy(tasteAttack = null, error = null)
        ReviewSelectionField.TASTE_TEXTURE_ROUNDNESS -> copy(tasteTextureRoundness = null, error = null)
        ReviewSelectionField.TASTE_TEXTURE_SMOOTHNESS -> copy(tasteTextureSmoothness = null, error = null)
        ReviewSelectionField.TASTE_COMPLEXITY -> copy(tasteComplexity = null, error = null)
        else -> null
    }

private fun ReviewEditUiState.clearTasteSelection(field: ReviewSelectionField): ReviewEditUiState? =
    when (field) {
        ReviewSelectionField.SWEET -> copy(sweet = null, error = null)
        ReviewSelectionField.SOUR -> copy(sour = null, error = null)
        ReviewSelectionField.BITTER -> copy(bitter = null, error = null)
        ReviewSelectionField.UMAMI -> copy(umami = null, error = null)
        ReviewSelectionField.SHARP -> copy(sharp = null, error = null)
        else -> null
    }

private fun ReviewEditUiState.clearRatingSelection(field: ReviewSelectionField): ReviewEditUiState? =
    when (field) {
        ReviewSelectionField.OVERALL_REVIEW -> copy(review = null, error = null)
        else -> null
    }
