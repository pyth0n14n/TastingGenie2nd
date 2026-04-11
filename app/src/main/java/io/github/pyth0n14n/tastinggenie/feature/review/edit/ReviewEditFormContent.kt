@file:Suppress("TooManyFunctions")

package io.github.pyth0n14n.tastinggenie.feature.review.edit

import androidx.compose.foundation.lazy.LazyListScope
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.AromaCategoryMaster
import io.github.pyth0n14n.tastinggenie.feature.review.ReviewSection
import io.github.pyth0n14n.tastinggenie.feature.review.ReviewSectionTabs
import io.github.pyth0n14n.tastinggenie.ui.common.DiscreteSliderField
import io.github.pyth0n14n.tastinggenie.ui.common.DropdownOption
import io.github.pyth0n14n.tastinggenie.ui.common.FormFieldState
import io.github.pyth0n14n.tastinggenie.ui.common.GroupedMultiSelectDropdown
import io.github.pyth0n14n.tastinggenie.ui.common.LabeledTextField
import io.github.pyth0n14n.tastinggenie.ui.common.ShortcutTextField
import io.github.pyth0n14n.tastinggenie.ui.common.SimpleDropdown
import io.github.pyth0n14n.tastinggenie.ui.common.validationErrorText

fun LazyListScope.reviewEditFormContent(
    state: ReviewEditUiState,
    onAction: (ReviewEditAction) -> Unit,
    uiData: ReviewEditFormUiData,
    selectedSection: ReviewSection,
    onSectionSelected: (ReviewSection) -> Unit,
) {
    item(key = "review_section_tabs", contentType = "tabs") {
        ReviewSectionTabs(
            selectedSection = selectedSection,
            onSectionSelected = onSectionSelected,
        )
    }
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
    addBasicMetadataFields(state = state, onAction = onAction)
    addBasicVolumeAndTemperatureFields(state = state, uiData = uiData, onAction = onAction)
    addBasicSceneAndDishFields(state = state, onAction = onAction)
}

private fun LazyListScope.addBasicMetadataFields(
    state: ReviewEditUiState,
    onAction: (ReviewEditAction) -> Unit,
) {
    dateField(
        state = state,
        value = state.date,
        onAction = onAction,
    )
    textField(
        state = state,
        labelRes = R.string.label_bar,
        onAction = onAction,
        ui = ReviewTextFieldUi(value = state.bar, field = ReviewTextField.BAR),
    )
    textField(
        state = state,
        labelRes = R.string.label_price,
        onAction = onAction,
        ui =
            ReviewTextFieldUi(
                value = state.price,
                field = ReviewTextField.PRICE,
                validationField = ReviewValidationField.PRICE,
            ),
    )
}

private fun LazyListScope.addBasicVolumeAndTemperatureFields(
    state: ReviewEditUiState,
    uiData: ReviewEditFormUiData,
    onAction: (ReviewEditAction) -> Unit,
) {
    item {
        val label = reviewTextResource(R.string.label_volume)
        ShortcutTextField(
            label = label,
            value = state.volume,
            shortcuts = uiData.volumeShortcutOptions,
            onValueChange = { next ->
                onAction(ReviewEditAction.TextChanged(field = ReviewTextField.VOLUME, value = next))
            },
            fieldState =
                FormFieldState(
                    errorText =
                        state.validationErrors[ReviewValidationField.VOLUME]?.let { error ->
                            val range = reviewValidationRange(ReviewValidationField.VOLUME)
                            validationErrorText(
                                label = label,
                                error = error,
                                minValue = range?.first,
                                maxValue = range?.last,
                            )
                        },
                ),
        )
    }
    item {
        val label = reviewTextResource(R.string.label_temperature)
        DiscreteSliderField(
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
    textField(
        state = state,
        labelRes = R.string.label_scene,
        onAction = onAction,
        ui = ReviewTextFieldUi(value = state.scene, field = ReviewTextField.SCENE),
    )
    textField(
        state = state,
        labelRes = R.string.label_dish,
        onAction = onAction,
        ui = ReviewTextFieldUi(value = state.dish, field = ReviewTextField.DISH),
    )
}

private fun LazyListScope.addChoiceFields(
    state: ReviewEditUiState,
    uiData: SingleChoiceUiData,
    onAction: (ReviewEditAction) -> Unit,
) {
    if (state.showReviewSoundness) {
        steppedField(
            labelRes = R.string.label_soundness,
            selectedValue = state.appearanceSoundness.name,
            options = reviewSoundnessOptions(),
            field = ReviewSelectionField.APPEARANCE_SOUNDNESS,
            onAction = onAction,
        )
    }
    dropdownField(
        R.string.label_color,
        state.color?.name,
        uiData.colorOptions,
        ReviewSelectionField.COLOR,
        onAction,
    )
    steppedField(
        R.string.label_viscosity,
        state.viscosity?.toString(),
        uiData.viscosityOptions,
        ReviewSelectionField.VISCOSITY,
        onAction,
    )
}

private fun LazyListScope.addAromaFields(
    state: ReviewEditUiState,
    uiData: SingleChoiceUiData,
    aromaUiData: AromaUiData,
    onAction: (ReviewEditAction) -> Unit,
) {
    if (state.showReviewSoundness) {
        steppedField(
            labelRes = R.string.label_soundness,
            selectedValue = state.aromaSoundness.name,
            options = reviewSoundnessOptions(),
            field = ReviewSelectionField.AROMA_SOUNDNESS,
            onAction = onAction,
        )
    }
    steppedField(
        labelRes = R.string.label_intensity,
        selectedValue = state.intensity?.name,
        options = uiData.intensityOptions,
        field = ReviewSelectionField.INTENSITY,
        onAction = onAction,
    )
    aromaField(
        labelRes = R.string.label_scent_top,
        selectedValues = state.scentTop.map { it.name },
        field = ReviewAromaField.TOP,
        aromaUiData = aromaUiData,
        onAction = onAction,
    )
    textField(
        state = state,
        labelRes = R.string.label_aroma_main_note,
        onAction = onAction,
        ui = ReviewTextFieldUi(value = state.aromaMainNote, field = ReviewTextField.AROMA_MAIN_NOTE),
    )
    steppedField(
        labelRes = R.string.label_aroma_complexity,
        selectedValue = state.aromaComplexity?.name,
        options = complexityOptions(),
        field = ReviewSelectionField.AROMA_COMPLEXITY,
        onAction = onAction,
    )
}

private fun LazyListScope.addTasteFields(
    state: ReviewEditUiState,
    uiData: ReviewEditFormUiData,
    onAction: (ReviewEditAction) -> Unit,
) {
    addTasteEvaluationFields(state = state, tasteOptions = uiData.tasteOptions, onAction = onAction)
    addTasteTextureFields(state = state, onAction = onAction)
    aromaField(
        labelRes = R.string.label_scent_mouth,
        selectedValues = state.scentMouth.map { it.name },
        field = ReviewAromaField.MOUTH,
        aromaUiData = uiData.aromaUiData,
        onAction = onAction,
    )
    textField(
        state = state,
        labelRes = R.string.label_taste_main_note,
        onAction = onAction,
        ui = ReviewTextFieldUi(value = state.tasteMainNote, field = ReviewTextField.TASTE_MAIN_NOTE),
    )
    steppedField(
        labelRes = R.string.label_taste_complexity,
        selectedValue = state.tasteComplexity?.name,
        options = complexityOptions(),
        field = ReviewSelectionField.TASTE_COMPLEXITY,
        onAction = onAction,
    )
}

private fun LazyListScope.addNoteFields(
    state: ReviewEditUiState,
    overallReviewOptions: List<DropdownOption>,
    onAction: (ReviewEditAction) -> Unit,
) {
    item {
        LabeledTextField(
            label = reviewTextResource(R.string.label_cautions),
            value = state.comment,
            onValueChange = { next ->
                onAction(ReviewEditAction.TextChanged(field = ReviewTextField.COMMENT, value = next))
            },
            singleLine = false,
        )
    }
    textField(
        state = state,
        labelRes = R.string.label_other_individuality,
        onAction = onAction,
        ui = ReviewTextFieldUi(value = state.otherIndividuality, field = ReviewTextField.OTHER_INDIVIDUALITY),
    )
    overallReviewField(
        selectedValue = state.review?.name,
        options = overallReviewOptions,
        onAction = onAction,
    )
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

private fun LazyListScope.addBasicSceneAndDishFields(
    state: ReviewEditUiState,
    onAction: (ReviewEditAction) -> Unit,
) {
    textField(
        state = state,
        labelRes = R.string.label_scene,
        onAction = onAction,
        ui = ReviewTextFieldUi(value = state.scene, field = ReviewTextField.SCENE),
    )
    textField(
        state = state,
        labelRes = R.string.label_dish,
        onAction = onAction,
        ui = ReviewTextFieldUi(value = state.dish, field = ReviewTextField.DISH),
    )
}

private fun LazyListScope.textField(
    state: ReviewEditUiState,
    labelRes: Int,
    onAction: (ReviewEditAction) -> Unit,
    ui: ReviewTextFieldUi,
) {
    item {
        val label = reviewTextResource(labelRes)
        LabeledTextField(
            label = label,
            value = ui.value,
            onValueChange = { next ->
                onAction(ReviewEditAction.TextChanged(field = ui.field, value = next))
            },
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
    }
}

private fun LazyListScope.dropdownField(
    labelRes: Int,
    selectedValue: String?,
    options: List<DropdownOption>,
    field: ReviewSelectionField,
    onAction: (ReviewEditAction) -> Unit,
) {
    item {
        SimpleDropdown(
            label = reviewTextResource(labelRes),
            selectedLabel = options.firstOrNull { it.value == selectedValue }?.label.orEmpty(),
            options = options,
            onSelected = { next ->
                onAction(ReviewEditAction.SelectionChanged(field = field, value = next))
            },
        )
    }
}

private fun LazyListScope.aromaField(
    labelRes: Int,
    selectedValues: List<String>,
    field: ReviewAromaField,
    aromaUiData: AromaUiData,
    onAction: (ReviewEditAction) -> Unit,
) {
    item {
        GroupedMultiSelectDropdown(
            label = reviewTextResource(labelRes),
            groups = aromaUiData.categories.toDropdownGroups(),
            selectedValues = selectedValues,
            onToggle = { value ->
                onAction(ReviewEditAction.AromaToggled(field = field, value = value))
            },
        )
    }
}

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
    val overallReviewOptions: List<DropdownOption>,
    val aromaUiData: AromaUiData,
    val volumeShortcutOptions: List<DropdownOption>,
)

private data class ReviewTextFieldUi(
    val value: String,
    val field: ReviewTextField,
    val validationField: ReviewValidationField? = null,
)
