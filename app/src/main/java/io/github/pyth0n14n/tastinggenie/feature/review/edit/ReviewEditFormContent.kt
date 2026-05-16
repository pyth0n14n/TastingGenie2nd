@file:Suppress("TooManyFunctions")

package io.github.pyth0n14n.tastinggenie.feature.review.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.AromaCategoryMaster
import io.github.pyth0n14n.tastinggenie.domain.model.ReviewItemId
import io.github.pyth0n14n.tastinggenie.domain.model.enums.FlavorProfileType
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeColor
import io.github.pyth0n14n.tastinggenie.feature.review.ReviewSection
import io.github.pyth0n14n.tastinggenie.ui.common.DropdownOption
import io.github.pyth0n14n.tastinggenie.ui.common.FormFieldState
import io.github.pyth0n14n.tastinggenie.ui.common.LabeledTextField
import io.github.pyth0n14n.tastinggenie.ui.common.ShortcutChips
import io.github.pyth0n14n.tastinggenie.ui.common.validationErrorText

fun LazyListScope.reviewEditFormContent(
    state: ReviewEditUiState,
    onAction: (ReviewEditAction) -> Unit,
    uiData: ReviewEditFormUiData,
    selectedSection: ReviewSection,
) {
    when (selectedSection) {
        ReviewSection.BASIC ->
            addBasicInfoFields(
                state = state,
                uiData = uiData,
                onAction = onAction,
            )

        ReviewSection.APPEARANCE ->
            addChoiceFields(
                state = state,
                uiData = uiData.singleChoiceUiData,
                onAction = onAction,
            )

        ReviewSection.AROMA ->
            addAromaFields(
                state = state,
                uiData = uiData.singleChoiceUiData,
                aromaUiData = uiData.aromaUiData,
                onAction = onAction,
            )

        ReviewSection.TASTE ->
            addTasteFields(state = state, uiData = uiData, onAction = onAction)

        ReviewSection.OTHER ->
            addNoteFields(state = state, overallReviewOptions = uiData.overallReviewOptions, onAction = onAction)
    }
}

private fun LazyListScope.addBasicInfoFields(
    state: ReviewEditUiState,
    uiData: ReviewEditFormUiData,
    onAction: (ReviewEditAction) -> Unit,
) {
    addBasicMetadataFields(state = state, uiData = uiData, onAction = onAction)
    addBasicVolumeAndTemperatureFields(state = state, uiData = uiData, onAction = onAction)
    addBasicBarField(state = state, onAction = onAction)
}

private fun LazyListScope.addBasicMetadataFields(
    state: ReviewEditUiState,
    uiData: ReviewEditFormUiData,
    onAction: (ReviewEditAction) -> Unit,
) {
    if (state.isItemEnabled(ReviewItemId.DATE)) {
        dateField(
            state = state,
            value = state.date,
            onAction = onAction,
        )
    }
    if (state.isItemEnabled(ReviewItemId.PRICE) || state.isItemEnabled(ReviewItemId.VOLUME)) {
        priceAndVolumeFields(
            state = state,
            shortcuts = uiData.volumeShortcutOptions,
            onAction = onAction,
        )
    }
}

private fun LazyListScope.addBasicVolumeAndTemperatureFields(
    state: ReviewEditUiState,
    uiData: ReviewEditFormUiData,
    onAction: (ReviewEditAction) -> Unit,
) {
    if (!state.isItemEnabled(ReviewItemId.TEMPERATURE)) {
        return
    }
    item {
        val label = reviewTextResource(R.string.label_temperature)
        TemperaturePickerField(
            label = label,
            options = uiData.singleChoiceUiData.temperatureOptions,
            selectedValue = state.temperature?.name,
            onValueChanged = { next ->
                onAction(
                    ReviewEditAction.SelectionChanged(
                        field = ReviewSelectionField.TEMPERATURE,
                        value = next.orEmpty(),
                    ),
                )
            },
        )
    }
}

private fun LazyListScope.addChoiceFields(
    state: ReviewEditUiState,
    uiData: SingleChoiceUiData,
    onAction: (ReviewEditAction) -> Unit,
) {
    if (state.showReviewSoundness && state.isItemEnabled(ReviewItemId.APPEARANCE_SOUNDNESS)) {
        steppedResourceField(
            ui =
                ReviewStepFieldUi(
                    labelRes = R.string.label_soundness,
                    selectedValue = state.appearanceSoundness?.name,
                    field = ReviewSelectionField.APPEARANCE_SOUNDNESS,
                    helpItemId = ReviewItemId.APPEARANCE_SOUNDNESS,
                ),
            options = reviewSoundnessOptions(),
            showHelpHints = state.showHelpHints,
            onAction = onAction,
        )
    }
    if (state.isItemEnabled(ReviewItemId.APPEARANCE_COLOR)) {
        colorField(
            state = state,
            options = uiData.colorOptions,
            onAction = onAction,
        )
    }
    if (state.isItemEnabled(ReviewItemId.APPEARANCE_VISCOSITY)) {
        steppedField(
            ui =
                ReviewStepFieldUi(
                    labelRes = R.string.label_viscosity,
                    selectedValue = state.viscosity?.toString(),
                    field = ReviewSelectionField.VISCOSITY,
                    helpItemId = ReviewItemId.APPEARANCE_VISCOSITY,
                ),
            options = uiData.viscosityOptions,
            showHelpHints = state.showHelpHints,
            onAction = onAction,
        )
    }
}

private fun LazyListScope.colorField(
    state: ReviewEditUiState,
    options: List<DropdownOption>,
    onAction: (ReviewEditAction) -> Unit,
) {
    item {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            ColorPickerField(
                label = reviewTextResource(R.string.label_color),
                options = options,
                selectedValue = state.color?.name,
                onValueChanged = { next ->
                    onAction(
                        ReviewEditAction.SelectionChanged(
                            field = ReviewSelectionField.COLOR,
                            value = next.orEmpty(),
                        ),
                    )
                },
            )
        }
    }
    if (state.color == SakeColor.OTHER) {
        item {
            LabeledTextField(
                label = reviewTextResource(R.string.label_color_other),
                value = state.colorOther,
                onValueChange = { next ->
                    onAction(ReviewEditAction.TextChanged(field = ReviewTextField.COLOR_OTHER, value = next))
                },
            )
        }
    }
}

private fun LazyListScope.addAromaFields(
    state: ReviewEditUiState,
    uiData: SingleChoiceUiData,
    aromaUiData: AromaUiData,
    onAction: (ReviewEditAction) -> Unit,
) {
    val isSoundnessVisible = state.showReviewSoundness && state.isItemEnabled(ReviewItemId.AROMA_SOUNDNESS)
    if (isSoundnessVisible) {
        steppedResourceField(
            ui =
                ReviewStepFieldUi(
                    labelRes = R.string.label_soundness,
                    selectedValue = state.aromaSoundness?.name,
                    field = ReviewSelectionField.AROMA_SOUNDNESS,
                ),
            options = reviewSoundnessOptions(),
            onAction = onAction,
        )
    }
    addAromaTopFields(
        state = state,
        uiData = uiData,
        aromaUiData = aromaUiData,
        isFirstSubheader = !isSoundnessVisible,
        onAction = onAction,
    )
    if (state.isItemEnabled(ReviewItemId.AROMA_COMPLEXITY)) {
        steppedResourceField(
            ui =
                ReviewStepFieldUi(
                    labelRes = R.string.label_aroma_complexity,
                    selectedValue = state.aromaComplexity?.name,
                    field = ReviewSelectionField.AROMA_COMPLEXITY,
                    helpItemId = ReviewItemId.AROMA_COMPLEXITY,
                ),
            options = complexityOptions(),
            showHelpHints = state.showHelpHints,
            onAction = onAction,
        )
    }
}

private fun LazyListScope.addAromaTopFields(
    state: ReviewEditUiState,
    uiData: SingleChoiceUiData,
    aromaUiData: AromaUiData,
    isFirstSubheader: Boolean,
    onAction: (ReviewEditAction) -> Unit,
) {
    addGroupIfAny(
        headingRes = R.string.detail_heading_aroma_top,
        hasAnyField =
            state.isItemEnabled(ReviewItemId.AROMA_INTENSITY) ||
                state.isItemEnabled(ReviewItemId.AROMA_EXAMPLES) ||
                state.isItemEnabled(ReviewItemId.AROMA_MAIN_NOTE),
        isFirstSubheader = isFirstSubheader,
        showHelpHints = state.showHelpHints,
        helpItemId = ReviewItemId.AROMA_INTENSITY,
    ) {
        if (state.isItemEnabled(ReviewItemId.AROMA_INTENSITY)) {
            ReviewChoiceField(
                ui =
                    ReviewDropdownChoiceFieldUi(
                        labelRes = R.string.detail_label_strength,
                        selectedValue = state.intensity?.name,
                        options = uiData.intensityOptions,
                        field = ReviewSelectionField.INTENSITY,
                    ),
                showHelpHints = state.showHelpHints,
                onAction = onAction,
            )
        }
        if (state.isItemEnabled(ReviewItemId.AROMA_EXAMPLES)) {
            AromaPickerField(
                label = reviewTextResource(R.string.detail_label_examples),
                title = reviewTextResource(R.string.detail_label_aroma_top_examples),
                selectedValues = state.scentTop,
                fallbackLabels = aromaUiData.categories.toAromaLabelMap(),
                helpItemId = ReviewItemId.AROMA_EXAMPLES,
                showHelpHints = state.showHelpHints,
                onSave = { values ->
                    onAction(ReviewEditAction.AromaSelectionChanged(field = ReviewAromaField.TOP, values = values))
                },
            )
        }
        if (state.isItemEnabled(ReviewItemId.AROMA_MAIN_NOTE)) {
            ReviewStandaloneHelpTextField(
                ui =
                    ReviewHelpTextFieldUi(
                        label = reviewTextResource(R.string.label_aroma_main_note),
                        value = state.aromaMainNote,
                        showHelpHints = state.showHelpHints,
                        helpItemId = ReviewItemId.AROMA_MAIN_NOTE,
                    ),
                onValueChange = { next ->
                    onAction(ReviewEditAction.TextChanged(field = ReviewTextField.AROMA_MAIN_NOTE, value = next))
                },
            )
        }
    }
}

private fun LazyListScope.addTasteFields(
    state: ReviewEditUiState,
    uiData: ReviewEditFormUiData,
    onAction: (ReviewEditAction) -> Unit,
) {
    val isSoundnessVisible = state.showReviewSoundness && state.isItemEnabled(ReviewItemId.TASTE_SOUNDNESS)
    val isAttackVisible = state.isItemEnabled(ReviewItemId.TASTE_ATTACK)
    val isTextureVisible = state.isTasteTextureVisible()
    val isInPalateAromaVisible = state.isInPalateAromaVisible()
    addTasteSoundnessField(state = state, onAction = onAction)
    addTasteAttackField(state = state, onAction = onAction)
    addTasteTextureFields(
        state = state,
        isFirstSubheader = !isSoundnessVisible && !isAttackVisible,
        onAction = onAction,
    )
    addSpecificTasteFields(
        state = state,
        uiData = uiData,
        isAfterSubheaderGroup = isTextureVisible,
        onAction = onAction,
    )
    addSweetDrynessField(state = state, onAction = onAction)
    addInPalateAromaFields(state = state, uiData = uiData, onAction = onAction)
    addTasteAftertasteFields(
        state = state,
        uiData = uiData,
        isAfterSubheaderGroup = isInPalateAromaVisible,
        onAction = onAction,
    )
    addTasteComplexityField(state = state, onAction = onAction)
}

private fun ReviewEditUiState.isTasteTextureVisible(): Boolean =
    isItemEnabled(ReviewItemId.TASTE_TEXTURE_ROUNDNESS) ||
        isItemEnabled(ReviewItemId.TASTE_TEXTURE_SMOOTHNESS) ||
        isItemEnabled(ReviewItemId.TASTE_TEXTURE_NOTE)

private fun ReviewEditUiState.isInPalateAromaVisible(): Boolean =
    isItemEnabled(ReviewItemId.TASTE_IN_PALATE_AROMA_INTENSITY) ||
        isItemEnabled(ReviewItemId.TASTE_IN_PALATE_AROMA_EXAMPLES)

private fun LazyListScope.addInPalateAromaFields(
    state: ReviewEditUiState,
    uiData: ReviewEditFormUiData,
    onAction: (ReviewEditAction) -> Unit,
) {
    addGroupIfAny(
        headingRes = R.string.detail_heading_in_palate_aroma,
        hasAnyField =
            state.isItemEnabled(ReviewItemId.TASTE_IN_PALATE_AROMA_INTENSITY) ||
                state.isItemEnabled(ReviewItemId.TASTE_IN_PALATE_AROMA_EXAMPLES),
        showHelpHints = state.showHelpHints,
        helpItemId = ReviewItemId.TASTE_IN_PALATE_AROMA_INTENSITY,
    ) {
        if (state.isItemEnabled(ReviewItemId.TASTE_IN_PALATE_AROMA_INTENSITY)) {
            ReviewChoiceField(
                ui =
                    ReviewDropdownChoiceFieldUi(
                        labelRes = R.string.detail_label_strength,
                        selectedValue = state.tasteInPalateAromaIntensity?.name,
                        options = uiData.singleChoiceUiData.intensityOptions,
                        field = ReviewSelectionField.TASTE_IN_PALATE_AROMA_INTENSITY,
                    ),
                showHelpHints = state.showHelpHints,
                onAction = onAction,
            )
        }
        if (state.isItemEnabled(ReviewItemId.TASTE_IN_PALATE_AROMA_EXAMPLES)) {
            AromaPickerField(
                label = reviewTextResource(R.string.detail_label_examples),
                title = reviewTextResource(R.string.detail_label_in_palate_aroma_examples),
                selectedValues = state.scentMouth,
                fallbackLabels = uiData.aromaUiData.categories.toAromaLabelMap(),
                onSave = { values ->
                    onAction(ReviewEditAction.AromaSelectionChanged(field = ReviewAromaField.MOUTH, values = values))
                },
            )
        }
    }
}

private fun LazyListScope.addNoteFields(
    state: ReviewEditUiState,
    overallReviewOptions: List<DropdownOption>,
    onAction: (ReviewEditAction) -> Unit,
) {
    if (state.isItemEnabled(ReviewItemId.OTHER_INDIVIDUALITY)) {
        item {
            ReviewStandaloneHelpTextField(
                ui =
                    ReviewHelpTextFieldUi(
                        label = reviewTextResource(R.string.label_other_individuality),
                        value = state.otherIndividuality,
                        showHelpHints = state.showHelpHints,
                        helpItemId = ReviewItemId.OTHER_INDIVIDUALITY,
                        singleLine = false,
                    ),
                onValueChange = { next ->
                    onAction(ReviewEditAction.TextChanged(field = ReviewTextField.OTHER_INDIVIDUALITY, value = next))
                },
            )
        }
    }
    if (state.isItemEnabled(ReviewItemId.OTHER_CAUTIONS)) {
        textField(
            state = state,
            labelRes = R.string.label_cautions,
            onAction = onAction,
            ui =
                ReviewTextFieldUi(
                    value = state.otherCautions,
                    field = ReviewTextField.OTHER_CAUTIONS,
                    helpItemId = ReviewItemId.OTHER_CAUTIONS,
                    singleLine = false,
                ),
        )
    }
    if (state.isItemEnabled(ReviewItemId.OTHER_SAKE_TYPES)) {
        sakeTypeField(
            state = state,
            selectedValue = state.otherSakeTypes.firstOrNull(),
            onAction = onAction,
        )
    }
    addFreeCommentFieldIfEnabled(state = state, onAction = onAction)
    if (state.isItemEnabled(ReviewItemId.OTHER_OVERALL_REVIEW)) {
        overallReviewField(
            selectedValue = state.review?.name,
            options = overallReviewOptions,
            showHelpHints = state.showHelpHints,
            onAction = onAction,
        )
    }
}

private fun LazyListScope.addFreeCommentFieldIfEnabled(
    state: ReviewEditUiState,
    onAction: (ReviewEditAction) -> Unit,
) {
    if (!state.isItemEnabled(ReviewItemId.OTHER_FREE_COMMENT)) {
        return
    }
    item {
        ReviewStandaloneHelpTextField(
            ui =
                ReviewHelpTextFieldUi(
                    label = reviewTextResource(R.string.label_comment),
                    value = state.comment,
                    showHelpHints = state.showHelpHints,
                    helpItemId = ReviewItemId.OTHER_FREE_COMMENT,
                    singleLine = false,
                ),
            onValueChange = { next ->
                onAction(ReviewEditAction.TextChanged(field = ReviewTextField.COMMENT, value = next))
            },
        )
    }
}

private fun LazyListScope.sakeTypeField(
    state: ReviewEditUiState,
    selectedValue: FlavorProfileType?,
    onAction: (ReviewEditAction) -> Unit,
) {
    item {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            ReviewHelpLabel(
                label = reviewTextResource(R.string.label_other_sake_types),
                itemId = ReviewItemId.OTHER_SAKE_TYPES,
                showHelpHints = state.showHelpHints,
            )
            SakeTypeQuadrantSelector(
                selectedType = selectedValue,
                onTypeSelected = { value ->
                    onAction(
                        ReviewEditAction.SakeTypeSelected(value),
                    )
                },
            )
        }
    }
}

private fun LazyListScope.dateField(
    state: ReviewEditUiState,
    value: String,
    onAction: (ReviewEditAction) -> Unit,
) {
    item {
        val label = reviewTextResource(R.string.label_review_date)
        io.github.pyth0n14n.tastinggenie.ui.common.DatePickerField(
            label = label,
            value = value,
            onDateSelected = { epochMillis ->
                onAction(ReviewEditAction.DateSelected(epochMillis = epochMillis))
            },
            initialSelectedDateMillis = value.toDatePickerInitialMillisOrNull(),
            fieldState =
                FormFieldState(
                    required = true,
                    errorText =
                        state.validationErrors[ReviewValidationField.DATE]?.let { error ->
                            validationErrorText(label = label, error = error)
                        },
                ),
        )
    }
}

private fun LazyListScope.priceAndVolumeFields(
    state: ReviewEditUiState,
    shortcuts: List<DropdownOption>,
    onAction: (ReviewEditAction) -> Unit,
) {
    item {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    ReviewBasicNumberField(
                        ui =
                            ReviewNumberFieldUi(
                                labelRes = R.string.label_price,
                                value = state.price,
                                field = ReviewTextField.PRICE,
                                validationField = ReviewValidationField.PRICE,
                                suffixRes = R.string.suffix_yen,
                            ),
                        state = state,
                        onAction = onAction,
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    ReviewBasicNumberField(
                        ui =
                            ReviewNumberFieldUi(
                                labelRes = R.string.label_volume,
                                value = state.volume,
                                field = ReviewTextField.VOLUME,
                                validationField = ReviewValidationField.VOLUME,
                                suffixRes = R.string.suffix_ml,
                            ),
                        state = state,
                        onAction = onAction,
                    )
                }
            }
            ShortcutChips(
                shortcuts = shortcuts,
                selectedValue = state.volume,
                onSelected = { next ->
                    onAction(ReviewEditAction.TextChanged(field = ReviewTextField.VOLUME, value = next))
                },
            )
        }
    }
}

@Composable
private fun ReviewBasicNumberField(
    ui: ReviewNumberFieldUi,
    state: ReviewEditUiState,
    onAction: (ReviewEditAction) -> Unit,
) {
    if (!state.isItemEnabled(ReviewItemId.PRICE) && ui.validationField == ReviewValidationField.PRICE) return
    if (!state.isItemEnabled(ReviewItemId.VOLUME) && ui.validationField == ReviewValidationField.VOLUME) return
    val label = reviewTextResource(ui.labelRes)
    LabeledTextField(
        label = label,
        value = ui.value,
        onValueChange = { next ->
            onAction(ReviewEditAction.TextChanged(field = ui.field, value = next))
        },
        fieldState =
            FormFieldState(
                errorText =
                    state.validationErrors[ui.validationField]?.let { error ->
                        val range = reviewValidationRange(ui.validationField)
                        validationErrorText(
                            label = label,
                            error = error,
                            minValue = range?.first,
                            maxValue = range?.last,
                        )
                    },
                suffixText = stringResource(ui.suffixRes),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            ),
    )
}

private data class ReviewNumberFieldUi(
    val labelRes: Int,
    val value: String,
    val field: ReviewTextField,
    val validationField: ReviewValidationField,
    val suffixRes: Int,
)

private fun LazyListScope.addBasicBarField(
    state: ReviewEditUiState,
    onAction: (ReviewEditAction) -> Unit,
) {
    if (state.isItemEnabled(ReviewItemId.BAR)) {
        textField(
            state = state,
            labelRes = R.string.label_bar,
            onAction = onAction,
            ui = ReviewTextFieldUi(value = state.bar, field = ReviewTextField.BAR),
        )
    }
}

internal fun LazyListScope.textField(
    state: ReviewEditUiState,
    labelRes: Int,
    onAction: (ReviewEditAction) -> Unit,
    ui: ReviewTextFieldUi,
) {
    item {
        val label = reviewTextResource(labelRes)
        val textFieldUi =
            ReviewHelpTextFieldUi(
                label = label,
                value = ui.value,
                showHelpHints = state.showHelpHints,
                helpItemId = ui.helpItemId,
                singleLine = ui.singleLine,
                fieldState =
                    FormFieldState(
                        errorText =
                            ui.validationField?.let { validationField ->
                                state.validationErrors[validationField]?.let { error ->
                                    val range = reviewValidationRange(validationField)
                                    validationErrorText(
                                        label = label,
                                        error = error,
                                        minValue = range?.first,
                                        maxValue = range?.last,
                                    )
                                }
                            },
                    ),
            )
        val onValueChange = { next: String ->
            onAction(ReviewEditAction.TextChanged(field = ui.field, value = next))
        }
        if (ui.helpItemId in StandaloneTextHelpItemIds) {
            ReviewStandaloneHelpTextField(ui = textFieldUi, onValueChange = onValueChange)
        } else {
            ReviewHelpTextField(ui = textFieldUi, onValueChange = onValueChange)
        }
    }
}

private val StandaloneTextHelpItemIds =
    setOf(
        ReviewItemId.AROMA_MAIN_NOTE,
        ReviewItemId.OTHER_CAUTIONS,
    )

data class AromaUiData(
    val categories: List<AromaCategoryMaster>,
)

data class SingleChoiceUiData(
    val temperatureOptions: List<DropdownOption>,
    val colorOptions: List<DropdownOption>,
    val intensityOptions: List<DropdownOption>,
    val viscosityOptions: List<DropdownOption>,
)

data class ReviewEditFormUiData(
    val singleChoiceUiData: SingleChoiceUiData,
    val tasteOptions: List<DropdownOption>,
    val aftertasteOptions: List<DropdownOption>,
    val overallReviewOptions: List<DropdownOption>,
    val aromaUiData: AromaUiData,
    val volumeShortcutOptions: List<DropdownOption>,
    val pairingOptions: List<DropdownOption>,
)

internal data class ReviewTextFieldUi(
    val value: String,
    val field: ReviewTextField,
    val validationField: ReviewValidationField? = null,
    val helpItemId: ReviewItemId? = null,
    val singleLine: Boolean = true,
)
