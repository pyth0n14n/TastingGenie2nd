package io.github.pyth0n14n.tastinggenie.feature.review.edit

import androidx.compose.foundation.lazy.LazyListScope
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.ReviewItemId

internal fun LazyListScope.addTasteEvaluationFields(
    state: ReviewEditUiState,
    uiData: ReviewEditFormUiData,
    onAction: (ReviewEditAction) -> Unit,
) {
    if (state.showReviewSoundness && state.isItemEnabled(ReviewItemId.TASTE_SOUNDNESS)) {
        steppedResourceField(
            labelRes = R.string.label_soundness,
            selectedValue = state.tasteSoundness.name,
            options = reviewSoundnessOptions(),
            field = ReviewSelectionField.TASTE_SOUNDNESS,
            onAction = onAction,
        )
    }
    if (state.isItemEnabled(ReviewItemId.TASTE_SWEETNESS)) {
        steppedField(
            labelRes = R.string.label_sweet,
            selectedValue = state.sweet?.name,
            options = uiData.tasteOptions,
            field = ReviewSelectionField.SWEET,
            onAction = onAction,
        )
    }
    if (state.isItemEnabled(ReviewItemId.TASTE_SOURNESS)) {
        steppedField(
            labelRes = R.string.label_sour,
            selectedValue = state.sour?.name,
            options = uiData.tasteOptions,
            field = ReviewSelectionField.SOUR,
            onAction = onAction,
        )
    }
    if (state.isItemEnabled(ReviewItemId.TASTE_BITTERNESS)) {
        steppedField(
            labelRes = R.string.label_bitter,
            selectedValue = state.bitter?.name,
            options = uiData.tasteOptions,
            field = ReviewSelectionField.BITTER,
            onAction = onAction,
        )
    }
    if (state.isItemEnabled(ReviewItemId.TASTE_UMAMI)) {
        steppedField(
            labelRes = R.string.label_umami,
            selectedValue = state.umami?.name,
            options = uiData.tasteOptions,
            field = ReviewSelectionField.UMAMI,
            onAction = onAction,
        )
    }
    if (state.isItemEnabled(ReviewItemId.TASTE_AFTERTASTE_LENGTH)) {
        steppedField(
            labelRes = R.string.label_sharp,
            selectedValue = state.sharp?.name,
            options = uiData.aftertasteOptions,
            field = ReviewSelectionField.SHARP,
            onAction = onAction,
        )
    }
    addSweetDrynessField(state = state, onAction = onAction)
}

private fun LazyListScope.addSweetDrynessField(
    state: ReviewEditUiState,
    onAction: (ReviewEditAction) -> Unit,
) {
    if (state.isItemEnabled(ReviewItemId.TASTE_SWEET_DRYNESS)) {
        steppedResourceField(
            labelRes = R.string.label_taste_sweet_dryness,
            selectedValue = state.tasteSweetDryness?.name,
            options = sweetDrynessOptions(),
            field = ReviewSelectionField.TASTE_SWEET_DRYNESS,
            onAction = onAction,
        )
    }
}

internal fun LazyListScope.addTasteTextureFields(
    state: ReviewEditUiState,
    onAction: (ReviewEditAction) -> Unit,
) {
    if (state.isItemEnabled(ReviewItemId.TASTE_ATTACK)) {
        steppedResourceField(
            labelRes = R.string.label_taste_attack,
            selectedValue = state.tasteAttack?.name,
            options = attackOptions(),
            field = ReviewSelectionField.TASTE_ATTACK,
            onAction = onAction,
        )
    }
    if (state.isItemEnabled(ReviewItemId.TASTE_TEXTURE_ROUNDNESS)) {
        steppedResourceField(
            labelRes = R.string.label_taste_texture_roundness,
            selectedValue = state.tasteTextureRoundness?.name,
            options = textureRoundnessOptions(),
            field = ReviewSelectionField.TASTE_TEXTURE_ROUNDNESS,
            onAction = onAction,
        )
    }
    if (state.isItemEnabled(ReviewItemId.TASTE_TEXTURE_SMOOTHNESS)) {
        steppedResourceField(
            labelRes = R.string.label_taste_texture_smoothness,
            selectedValue = state.tasteTextureSmoothness?.name,
            options = textureSmoothnessOptions(),
            field = ReviewSelectionField.TASTE_TEXTURE_SMOOTHNESS,
            onAction = onAction,
        )
    }
    if (state.isItemEnabled(ReviewItemId.TASTE_TEXTURE_NOTE)) {
        textField(
            state = state,
            labelRes = R.string.label_taste_texture_note,
            onAction = onAction,
            ui = ReviewTextFieldUi(value = state.tasteTextureNote, field = ReviewTextField.TASTE_TEXTURE_NOTE),
            singleLine = false,
        )
    }
}
