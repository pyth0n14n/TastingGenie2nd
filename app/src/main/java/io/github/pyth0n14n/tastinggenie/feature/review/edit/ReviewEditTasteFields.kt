package io.github.pyth0n14n.tastinggenie.feature.review.edit

import androidx.compose.foundation.lazy.LazyListScope
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.ui.common.DropdownOption

internal fun LazyListScope.addTasteEvaluationFields(
    state: ReviewEditUiState,
    tasteOptions: List<DropdownOption>,
    onAction: (ReviewEditAction) -> Unit,
) {
    steppedField(
        labelRes = R.string.label_soundness,
        selectedValue = state.tasteSoundness.name,
        options = reviewSoundnessOptions(),
        field = ReviewSelectionField.TASTE_SOUNDNESS,
        onAction = onAction,
    )
    steppedField(
        labelRes = R.string.label_sweet,
        selectedValue = state.sweet?.name,
        options = tasteOptions,
        field = ReviewSelectionField.SWEET,
        onAction = onAction,
    )
    steppedField(
        labelRes = R.string.label_sour,
        selectedValue = state.sour?.name,
        options = tasteOptions,
        field = ReviewSelectionField.SOUR,
        onAction = onAction,
    )
    steppedField(
        labelRes = R.string.label_bitter,
        selectedValue = state.bitter?.name,
        options = tasteOptions,
        field = ReviewSelectionField.BITTER,
        onAction = onAction,
    )
    steppedField(
        labelRes = R.string.label_umami,
        selectedValue = state.umami?.name,
        options = tasteOptions,
        field = ReviewSelectionField.UMAMI,
        onAction = onAction,
    )
    steppedField(
        labelRes = R.string.label_sharp,
        selectedValue = state.sharp?.name,
        options = tasteOptions,
        field = ReviewSelectionField.SHARP,
        onAction = onAction,
    )
}

internal fun LazyListScope.addTasteTextureFields(
    state: ReviewEditUiState,
    onAction: (ReviewEditAction) -> Unit,
) {
    steppedField(
        labelRes = R.string.label_taste_attack,
        selectedValue = state.tasteAttack?.name,
        options = attackOptions(),
        field = ReviewSelectionField.TASTE_ATTACK,
        onAction = onAction,
    )
    steppedField(
        labelRes = R.string.label_taste_texture_roundness,
        selectedValue = state.tasteTextureRoundness?.name,
        options = textureRoundnessOptions(),
        field = ReviewSelectionField.TASTE_TEXTURE_ROUNDNESS,
        onAction = onAction,
    )
    steppedField(
        labelRes = R.string.label_taste_texture_smoothness,
        selectedValue = state.tasteTextureSmoothness?.name,
        options = textureSmoothnessOptions(),
        field = ReviewSelectionField.TASTE_TEXTURE_SMOOTHNESS,
        onAction = onAction,
    )
}
