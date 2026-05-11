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
import io.github.pyth0n14n.tastinggenie.ui.common.validationErrorText

internal fun LazyListScope.addTasteSoundnessField(
    state: ReviewEditUiState,
    onAction: (ReviewEditAction) -> Unit,
) {
    if (state.showReviewSoundness && state.isItemEnabled(ReviewItemId.TASTE_SOUNDNESS)) {
        steppedResourceField(
            ui =
                ReviewStepFieldUi(
                    labelRes = R.string.label_soundness,
                    selectedValue = state.tasteSoundness.name,
                    field = ReviewSelectionField.TASTE_SOUNDNESS,
                ),
            options = reviewSoundnessOptions(),
            showHelpHints = state.showHelpHints,
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
            ui =
                ReviewStepFieldUi(
                    labelRes = R.string.label_taste_attack,
                    selectedValue = state.tasteAttack?.name,
                    field = ReviewSelectionField.TASTE_ATTACK,
                    helpItemId = ReviewItemId.TASTE_ATTACK,
                ),
            options = attackOptions(),
            showHelpHints = state.showHelpHints,
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
        showHelpHints = state.showHelpHints,
        helpItemId = ReviewItemId.TASTE_TEXTURE_ROUNDNESS,
    ) {
        if (state.isItemEnabled(ReviewItemId.TASTE_TEXTURE_ROUNDNESS)) {
            ReviewResourceChoiceField(
                ui =
                    ReviewResourceChoiceFieldUi(
                        labelRes = R.string.label_taste_texture_roundness,
                        selectedValue = state.tasteTextureRoundness?.name,
                        field = ReviewSelectionField.TASTE_TEXTURE_ROUNDNESS,
                    ),
                options = textureRoundnessOptions(),
                showHelpHints = state.showHelpHints,
                onAction = onAction,
            )
        }
        if (state.isItemEnabled(ReviewItemId.TASTE_TEXTURE_SMOOTHNESS)) {
            ReviewResourceChoiceField(
                ui =
                    ReviewResourceChoiceFieldUi(
                        labelRes = R.string.label_taste_texture_smoothness,
                        selectedValue = state.tasteTextureSmoothness?.name,
                        field = ReviewSelectionField.TASTE_TEXTURE_SMOOTHNESS,
                    ),
                options = textureSmoothnessOptions(),
                showHelpHints = state.showHelpHints,
                onAction = onAction,
            )
        }
        if (state.isItemEnabled(ReviewItemId.TASTE_TEXTURE_NOTE)) {
            ReviewGroupTextField(
                state = state,
                labelRes = R.string.label_taste_texture_note,
                onAction = onAction,
                ui =
                    ReviewTextFieldUi(
                        value = state.tasteTextureNote,
                        field = ReviewTextField.TASTE_TEXTURE_NOTE,
                        singleLine = false,
                    ),
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
                ui =
                    ReviewTextFieldUi(
                        value = state.tasteMainNote,
                        field = ReviewTextField.TASTE_MAIN_NOTE,
                        helpItemId = ReviewItemId.TASTE_DESCRIPTION,
                    ),
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
            ui =
                ReviewDropdownChoiceFieldUi(
                    labelRes = R.string.label_sweet,
                    selectedValue = state.sweet?.name,
                    options = uiData.tasteOptions,
                    field = ReviewSelectionField.SWEET,
                    helpItemId = ReviewItemId.TASTE_SWEETNESS,
                ),
            showHelpHints = state.showHelpHints,
            onAction = onAction,
        )
    }
    if (state.isItemEnabled(ReviewItemId.TASTE_SOURNESS)) {
        ReviewChoiceField(
            ui =
                ReviewDropdownChoiceFieldUi(
                    labelRes = R.string.label_sour,
                    selectedValue = state.sour?.name,
                    options = uiData.tasteOptions,
                    field = ReviewSelectionField.SOUR,
                    helpItemId = ReviewItemId.TASTE_SOURNESS,
                ),
            showHelpHints = state.showHelpHints,
            onAction = onAction,
        )
    }
    if (state.isItemEnabled(ReviewItemId.TASTE_BITTERNESS)) {
        ReviewChoiceField(
            ui =
                ReviewDropdownChoiceFieldUi(
                    labelRes = R.string.label_bitter,
                    selectedValue = state.bitter?.name,
                    options = uiData.tasteOptions,
                    field = ReviewSelectionField.BITTER,
                    helpItemId = ReviewItemId.TASTE_BITTERNESS,
                ),
            showHelpHints = state.showHelpHints,
            onAction = onAction,
        )
    }
    if (state.isItemEnabled(ReviewItemId.TASTE_UMAMI)) {
        ReviewChoiceField(
            ui =
                ReviewDropdownChoiceFieldUi(
                    labelRes = R.string.label_umami,
                    selectedValue = state.umami?.name,
                    options = uiData.tasteOptions,
                    field = ReviewSelectionField.UMAMI,
                    helpItemId = ReviewItemId.TASTE_UMAMI,
                ),
            showHelpHints = state.showHelpHints,
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
            ui =
                ReviewStepFieldUi(
                    labelRes = R.string.label_taste_sweet_dryness,
                    selectedValue = state.tasteSweetDryness?.name,
                    field = ReviewSelectionField.TASTE_SWEET_DRYNESS,
                    helpItemId = ReviewItemId.TASTE_SWEET_DRYNESS,
                ),
            options = sweetDrynessOptions(),
            showHelpHints = state.showHelpHints,
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
        showHelpHints = state.showHelpHints,
        helpItemId = ReviewItemId.TASTE_AFTERTASTE_LENGTH,
    ) {
        if (state.isItemEnabled(ReviewItemId.TASTE_AFTERTASTE_LENGTH)) {
            ReviewChoiceField(
                ui =
                    ReviewDropdownChoiceFieldUi(
                        labelRes = R.string.detail_label_length,
                        selectedValue = state.sharp?.name,
                        options = uiData.aftertasteOptions,
                        field = ReviewSelectionField.SHARP,
                    ),
                showHelpHints = state.showHelpHints,
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
                        helpItemId = ReviewItemId.TASTE_AFTERTASTE_NOTE,
                        singleLine = false,
                    ),
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
            ui =
                ReviewStepFieldUi(
                    labelRes = R.string.label_taste_complexity,
                    selectedValue = state.tasteComplexity?.name,
                    field = ReviewSelectionField.TASTE_COMPLEXITY,
                    helpItemId = ReviewItemId.TASTE_COMPLEXITY,
                ),
            options = complexityOptions(),
            showHelpHints = state.showHelpHints,
            onAction = onAction,
        )
    }
}

@Suppress("LongParameterList")
internal fun LazyListScope.addGroupIfAny(
    headingRes: Int,
    hasAnyField: Boolean,
    isFirstSubheader: Boolean = false,
    isAfterSubheaderGroup: Boolean = false,
    showHelpHints: Boolean = false,
    helpItemId: ReviewItemId? = null,
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
                showHelpHints = showHelpHints,
                helpItemId = helpItemId,
            ) {
                content()
            }
        }
    }
}

@Composable
internal fun ReviewChoiceField(
    ui: ReviewDropdownChoiceFieldUi,
    showHelpHints: Boolean = false,
    onAction: (ReviewEditAction) -> Unit,
) {
    ReviewEditChoiceField(
        ui =
            ReviewEditChoiceFieldUi(
                label = reviewTextResource(ui.labelRes),
                options = ui.options,
                selectedValue = ui.selectedValue,
                showHelpHints = showHelpHints,
                helpItemId = ui.helpItemId,
            ),
        onValueChanged = { nextValue ->
            onAction(ReviewEditAction.SelectionChanged(field = ui.field, value = nextValue ?: ""))
        },
    )
}

@Composable
internal fun ReviewResourceChoiceField(
    ui: ReviewResourceChoiceFieldUi,
    options: List<ReviewResourceOption>,
    showHelpHints: Boolean = false,
    onAction: (ReviewEditAction) -> Unit,
) {
    ReviewChoiceField(
        ui =
            ReviewDropdownChoiceFieldUi(
                labelRes = ui.labelRes,
                selectedValue = ui.selectedValue,
                options =
                    options.map { option ->
                        DropdownOption(
                            value = option.value,
                            label = stringResource(option.labelRes),
                        )
                    },
                field = ui.field,
                helpItemId = ui.helpItemId,
            ),
        showHelpHints = showHelpHints,
        onAction = onAction,
    )
}

internal data class ReviewDropdownChoiceFieldUi(
    @StringRes val labelRes: Int,
    val selectedValue: String?,
    val options: List<DropdownOption>,
    val field: ReviewSelectionField,
    val helpItemId: ReviewItemId? = null,
)

internal data class ReviewResourceChoiceFieldUi(
    @StringRes val labelRes: Int,
    val selectedValue: String?,
    val field: ReviewSelectionField,
    val helpItemId: ReviewItemId? = null,
)

@Composable
internal fun ReviewGroupTextField(
    state: ReviewEditUiState,
    @StringRes labelRes: Int,
    onAction: (ReviewEditAction) -> Unit,
    ui: ReviewTextFieldUi,
) {
    val label = reviewTextResource(labelRes)
    ReviewHelpTextField(
        ui =
            ReviewHelpTextFieldUi(
                label = label,
                value = ui.value,
                showHelpHints = state.showHelpHints,
                helpItemId = ui.helpItemId,
                singleLine = ui.singleLine,
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
            ),
        onValueChange = { next ->
            onAction(ReviewEditAction.TextChanged(field = ui.field, value = next))
        },
    )
}
