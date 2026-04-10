package io.github.pyth0n14n.tastinggenie.feature.review.edit

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.AromaCategoryMaster
import io.github.pyth0n14n.tastinggenie.domain.model.MasterOption
import io.github.pyth0n14n.tastinggenie.ui.common.DropdownOption
import io.github.pyth0n14n.tastinggenie.ui.common.FormFieldState
import io.github.pyth0n14n.tastinggenie.ui.common.GroupedMultiSelectDropdown
import io.github.pyth0n14n.tastinggenie.ui.common.LabeledTextField
import io.github.pyth0n14n.tastinggenie.ui.common.SimpleDropdown
import io.github.pyth0n14n.tastinggenie.ui.common.validationErrorText

fun LazyListScope.reviewEditFormContent(
    state: ReviewEditUiState,
    onAction: (ReviewEditAction) -> Unit,
    uiData: ReviewEditFormUiData,
) {
    addBasicFields(state = state, onAction = onAction)
    addChoiceFields(
        state = state,
        uiData = uiData.singleChoiceUiData,
        onAction = onAction,
    )
    addAromaFields(state = state, aromaUiData = uiData.aromaUiData, onAction = onAction)
    steppedField(
        labelRes = R.string.label_sweet,
        selectedValue = state.sweet?.name,
        options = uiData.tasteOptions,
        field = ReviewSelectionField.SWEET,
        onAction = onAction,
    )
    steppedField(
        labelRes = R.string.label_sour,
        selectedValue = state.sour?.name,
        options = uiData.tasteOptions,
        field = ReviewSelectionField.SOUR,
        onAction = onAction,
    )
    steppedField(
        labelRes = R.string.label_bitter,
        selectedValue = state.bitter?.name,
        options = uiData.tasteOptions,
        field = ReviewSelectionField.BITTER,
        onAction = onAction,
    )
    steppedField(
        labelRes = R.string.label_umami,
        selectedValue = state.umami?.name,
        options = uiData.tasteOptions,
        field = ReviewSelectionField.UMAMI,
        onAction = onAction,
    )
    steppedField(
        labelRes = R.string.label_sharp,
        selectedValue = state.sharp?.name,
        options = uiData.tasteOptions,
        field = ReviewSelectionField.SHARP,
        onAction = onAction,
    )
    addNoteFields(state = state, overallReviewOptions = uiData.overallReviewOptions, onAction = onAction)
}

private fun LazyListScope.addBasicFields(
    state: ReviewEditUiState,
    onAction: (ReviewEditAction) -> Unit,
) {
    item {
        Text(
            text = "${reviewTextResource(R.string.label_sake)}: ${state.sakeName}",
            style = MaterialTheme.typography.titleMedium,
        )
    }
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
    textField(
        state = state,
        labelRes = R.string.label_volume,
        onAction = onAction,
        ui =
            ReviewTextFieldUi(
                value = state.volume,
                field = ReviewTextField.VOLUME,
                validationField = ReviewValidationField.VOLUME,
            ),
    )
}

private fun LazyListScope.addChoiceFields(
    state: ReviewEditUiState,
    uiData: SingleChoiceUiData,
    onAction: (ReviewEditAction) -> Unit,
) {
    dropdownField(
        R.string.label_temperature,
        state.temperature?.name,
        uiData.temperatureOptions,
        ReviewSelectionField.TEMPERATURE,
        onAction,
    )
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
    steppedField(
        R.string.label_intensity,
        state.intensity?.name,
        uiData.intensityOptions,
        ReviewSelectionField.INTENSITY,
        onAction,
    )
}

private fun LazyListScope.addAromaFields(
    state: ReviewEditUiState,
    aromaUiData: AromaUiData,
    onAction: (ReviewEditAction) -> Unit,
) {
    aromaField(
        labelRes = R.string.label_scent_top,
        selectedValues = state.scentTop.map { it.name },
        field = ReviewAromaField.TOP,
        aromaUiData = aromaUiData,
        onAction = onAction,
    )
    aromaField(
        labelRes = R.string.label_scent_base,
        selectedValues = state.scentBase.map { it.name },
        field = ReviewAromaField.BASE,
        aromaUiData = aromaUiData,
        onAction = onAction,
    )
    aromaField(
        labelRes = R.string.label_scent_mouth,
        selectedValues = state.scentMouth.map { it.name },
        field = ReviewAromaField.MOUTH,
        aromaUiData = aromaUiData,
        onAction = onAction,
    )
}

private fun LazyListScope.addNoteFields(
    state: ReviewEditUiState,
    overallReviewOptions: List<DropdownOption>,
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
    item {
        LabeledTextField(
            label = reviewTextResource(R.string.label_comment),
            value = state.comment,
            onValueChange = { next ->
                onAction(ReviewEditAction.TextChanged(field = ReviewTextField.COMMENT, value = next))
            },
            singleLine = false,
        )
    }
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
                                validationErrorText(label = label, error = error)
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

fun List<MasterOption>.toOptions(): List<DropdownOption> =
    map { option ->
        DropdownOption(
            value = option.value,
            label = option.label,
        )
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
)

private data class ReviewTextFieldUi(
    val value: String,
    val field: ReviewTextField,
    val validationField: ReviewValidationField? = null,
)
