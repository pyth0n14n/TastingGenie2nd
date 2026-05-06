package io.github.pyth0n14n.tastinggenie.feature.review.edit

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.ReviewItemId

private const val TASTE_GROUP_BOTTOM_SPACE = 8

internal fun LazyListScope.addTasteSoundnessField(
    state: ReviewEditUiState,
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
}

internal fun LazyListScope.addTasteAttackField(
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
}

internal fun LazyListScope.addTasteTextureFields(
    state: ReviewEditUiState,
    onAction: (ReviewEditAction) -> Unit,
) {
    addGroupIfAny(
        headingRes = R.string.detail_heading_texture,
        hasAnyField =
            state.isItemEnabled(ReviewItemId.TASTE_TEXTURE_ROUNDNESS) ||
                state.isItemEnabled(ReviewItemId.TASTE_TEXTURE_SMOOTHNESS) ||
                state.isItemEnabled(ReviewItemId.TASTE_TEXTURE_NOTE),
    ) {
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
}

internal fun LazyListScope.addSpecificTasteFields(
    state: ReviewEditUiState,
    uiData: ReviewEditFormUiData,
    onAction: (ReviewEditAction) -> Unit,
) {
    addGroupIfAny(
        headingRes = R.string.detail_heading_taste,
        hasAnyField =
            state.isItemEnabled(ReviewItemId.TASTE_SWEETNESS) ||
                state.isItemEnabled(ReviewItemId.TASTE_SOURNESS) ||
                state.isItemEnabled(ReviewItemId.TASTE_BITTERNESS) ||
                state.isItemEnabled(ReviewItemId.TASTE_UMAMI) ||
                state.isItemEnabled(ReviewItemId.TASTE_DESCRIPTION),
    ) {
        addTasteScaleFields(state = state, uiData = uiData, onAction = onAction)
        if (state.isItemEnabled(ReviewItemId.TASTE_DESCRIPTION)) {
            textField(
                state = state,
                labelRes = R.string.label_taste_main_note,
                onAction = onAction,
                ui = ReviewTextFieldUi(value = state.tasteMainNote, field = ReviewTextField.TASTE_MAIN_NOTE),
            )
        }
    }
}

private fun LazyListScope.addTasteScaleFields(
    state: ReviewEditUiState,
    uiData: ReviewEditFormUiData,
    onAction: (ReviewEditAction) -> Unit,
) {
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
}

internal fun LazyListScope.addSweetDrynessField(
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

internal fun LazyListScope.addTasteAftertasteFields(
    state: ReviewEditUiState,
    uiData: ReviewEditFormUiData,
    onAction: (ReviewEditAction) -> Unit,
) {
    if (state.isItemEnabled(ReviewItemId.TASTE_AFTERTASTE_LENGTH)) {
        steppedField(
            labelRes = R.string.label_sharp,
            selectedValue = state.sharp?.name,
            options = uiData.aftertasteOptions,
            field = ReviewSelectionField.SHARP,
            onAction = onAction,
        )
    }
    if (state.isItemEnabled(ReviewItemId.TASTE_AFTERTASTE_NOTE)) {
        textField(
            state = state,
            labelRes = R.string.label_taste_aftertaste_note,
            onAction = onAction,
            ui = ReviewTextFieldUi(value = state.tasteAftertasteNote, field = ReviewTextField.TASTE_AFTERTASTE_NOTE),
            singleLine = false,
        )
    }
}

internal fun LazyListScope.addTasteComplexityField(
    state: ReviewEditUiState,
    onAction: (ReviewEditAction) -> Unit,
) {
    if (state.isItemEnabled(ReviewItemId.TASTE_COMPLEXITY)) {
        steppedResourceField(
            labelRes = R.string.label_taste_complexity,
            selectedValue = state.tasteComplexity?.name,
            options = complexityOptions(),
            field = ReviewSelectionField.TASTE_COMPLEXITY,
            onAction = onAction,
        )
    }
}

internal fun LazyListScope.addGroupIfAny(
    headingRes: Int,
    hasAnyField: Boolean,
    content: LazyListScope.() -> Unit,
) {
    if (hasAnyField) {
        item {
            Text(
                text = reviewTextResource(headingRes),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        content()
        item {
            Spacer(modifier = Modifier.height(TASTE_GROUP_BOTTOM_SPACE.dp))
        }
    }
}
