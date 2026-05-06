@file:Suppress("TooManyFunctions")

package io.github.pyth0n14n.tastinggenie.feature.review.edit

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.ReviewItemId
import io.github.pyth0n14n.tastinggenie.ui.common.DropdownOption
import io.github.pyth0n14n.tastinggenie.ui.common.FormFieldState
import io.github.pyth0n14n.tastinggenie.ui.common.LabeledTextField
import io.github.pyth0n14n.tastinggenie.ui.common.validationErrorText

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
    isFirstSubheader: Boolean,
    onAction: (ReviewEditAction) -> Unit,
) {
    addGroupIfAny(
        headingRes = R.string.detail_heading_texture,
        hasAnyField =
            state.isItemEnabled(ReviewItemId.TASTE_TEXTURE_ROUNDNESS) ||
                state.isItemEnabled(ReviewItemId.TASTE_TEXTURE_SMOOTHNESS) ||
                state.isItemEnabled(ReviewItemId.TASTE_TEXTURE_NOTE),
        isFirstSubheader = isFirstSubheader,
    ) {
        if (state.isItemEnabled(ReviewItemId.TASTE_TEXTURE_ROUNDNESS)) {
            ReviewResourceChoiceField(
                labelRes = R.string.label_taste_texture_roundness,
                selectedValue = state.tasteTextureRoundness?.name,
                options = textureRoundnessOptions(),
                field = ReviewSelectionField.TASTE_TEXTURE_ROUNDNESS,
                onAction = onAction,
            )
        }
        if (state.isItemEnabled(ReviewItemId.TASTE_TEXTURE_SMOOTHNESS)) {
            ReviewResourceChoiceField(
                labelRes = R.string.label_taste_texture_smoothness,
                selectedValue = state.tasteTextureSmoothness?.name,
                options = textureSmoothnessOptions(),
                field = ReviewSelectionField.TASTE_TEXTURE_SMOOTHNESS,
                onAction = onAction,
            )
        }
        if (state.isItemEnabled(ReviewItemId.TASTE_TEXTURE_NOTE)) {
            ReviewGroupTextField(
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
    isAfterSubheaderGroup: Boolean,
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
        isAfterSubheaderGroup = isAfterSubheaderGroup,
    ) {
        addTasteScaleFields(state = state, uiData = uiData, onAction = onAction)
        if (state.isItemEnabled(ReviewItemId.TASTE_DESCRIPTION)) {
            ReviewGroupTextField(
                state = state,
                labelRes = R.string.label_taste_main_note,
                onAction = onAction,
                ui = ReviewTextFieldUi(value = state.tasteMainNote, field = ReviewTextField.TASTE_MAIN_NOTE),
            )
        }
    }
}

@Composable
private fun addTasteScaleFields(
    state: ReviewEditUiState,
    uiData: ReviewEditFormUiData,
    onAction: (ReviewEditAction) -> Unit,
) {
    if (state.isItemEnabled(ReviewItemId.TASTE_SWEETNESS)) {
        ReviewChoiceField(
            labelRes = R.string.label_sweet,
            selectedValue = state.sweet?.name,
            options = uiData.tasteOptions,
            field = ReviewSelectionField.SWEET,
            onAction = onAction,
        )
    }
    if (state.isItemEnabled(ReviewItemId.TASTE_SOURNESS)) {
        ReviewChoiceField(
            labelRes = R.string.label_sour,
            selectedValue = state.sour?.name,
            options = uiData.tasteOptions,
            field = ReviewSelectionField.SOUR,
            onAction = onAction,
        )
    }
    if (state.isItemEnabled(ReviewItemId.TASTE_BITTERNESS)) {
        ReviewChoiceField(
            labelRes = R.string.label_bitter,
            selectedValue = state.bitter?.name,
            options = uiData.tasteOptions,
            field = ReviewSelectionField.BITTER,
            onAction = onAction,
        )
    }
    if (state.isItemEnabled(ReviewItemId.TASTE_UMAMI)) {
        ReviewChoiceField(
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
    isAfterSubheaderGroup: Boolean,
    onAction: (ReviewEditAction) -> Unit,
) {
    addGroupIfAny(
        headingRes = R.string.detail_heading_aftertaste,
        hasAnyField =
            state.isItemEnabled(ReviewItemId.TASTE_AFTERTASTE_LENGTH) ||
                state.isItemEnabled(ReviewItemId.TASTE_AFTERTASTE_NOTE),
        isAfterSubheaderGroup = isAfterSubheaderGroup,
    ) {
        if (state.isItemEnabled(ReviewItemId.TASTE_AFTERTASTE_LENGTH)) {
            ReviewChoiceField(
                labelRes = R.string.detail_label_length,
                selectedValue = state.sharp?.name,
                options = uiData.aftertasteOptions,
                field = ReviewSelectionField.SHARP,
                onAction = onAction,
            )
        }
        if (state.isItemEnabled(ReviewItemId.TASTE_AFTERTASTE_NOTE)) {
            ReviewGroupTextField(
                state = state,
                labelRes = R.string.detail_label_free_note,
                onAction = onAction,
                ui =
                    ReviewTextFieldUi(
                        value = state.tasteAftertasteNote,
                        field = ReviewTextField.TASTE_AFTERTASTE_NOTE,
                    ),
                singleLine = false,
            )
        }
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
    isFirstSubheader: Boolean = false,
    isAfterSubheaderGroup: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (hasAnyField) {
        item {
            ReviewFieldGroup(
                heading = reviewTextResource(headingRes),
                topSpacing =
                    when {
                        isFirstSubheader -> ReviewEditFirstSubheaderTopSpacing
                        isAfterSubheaderGroup -> ReviewEditSubheaderAfterGroupTopSpacing
                        else -> ReviewEditSubheaderTopSpacing
                    },
            ) {
                content()
            }
        }
    }
}

@Composable
internal fun ReviewChoiceField(
    @StringRes labelRes: Int,
    selectedValue: String?,
    options: List<DropdownOption>,
    field: ReviewSelectionField,
    onAction: (ReviewEditAction) -> Unit,
) {
    ReviewEditChoiceField(
        label = reviewTextResource(labelRes),
        options = options,
        selectedValue = selectedValue,
        onValueChanged = { nextValue ->
            onAction(ReviewEditAction.SelectionChanged(field = field, value = nextValue ?: ""))
        },
    )
}

@Composable
internal fun ReviewResourceChoiceField(
    @StringRes labelRes: Int,
    selectedValue: String?,
    options: List<ReviewResourceOption>,
    field: ReviewSelectionField,
    onAction: (ReviewEditAction) -> Unit,
) {
    ReviewChoiceField(
        labelRes = labelRes,
        selectedValue = selectedValue,
        options =
            options.map { option ->
                DropdownOption(
                    value = option.value,
                    label = stringResource(option.labelRes),
                )
            },
        field = field,
        onAction = onAction,
    )
}

@Composable
internal fun ReviewGroupTextField(
    state: ReviewEditUiState,
    @StringRes labelRes: Int,
    onAction: (ReviewEditAction) -> Unit,
    ui: ReviewTextFieldUi,
    singleLine: Boolean = true,
) {
    LabeledTextField(
        label = reviewTextResource(labelRes),
        value = ui.value,
        onValueChange = { next ->
            onAction(ReviewEditAction.TextChanged(field = ui.field, value = next))
        },
        fieldState =
            ui.validationField
                ?.let { validationField ->
                    state.validationErrors[validationField]?.let { error ->
                        val range = reviewValidationRange(validationField)
                        FormFieldState(
                            errorText =
                                validationErrorText(
                                    label = reviewTextResource(labelRes),
                                    error = error,
                                    minValue = range?.first,
                                    maxValue = range?.last,
                                ),
                        )
                    }
                } ?: FormFieldState(),
        singleLine = singleLine,
    )
}
