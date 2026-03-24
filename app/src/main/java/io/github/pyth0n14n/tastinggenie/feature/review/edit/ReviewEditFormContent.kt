package io.github.pyth0n14n.tastinggenie.feature.review.edit

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.AromaCategoryMaster
import io.github.pyth0n14n.tastinggenie.domain.model.MasterOption
import io.github.pyth0n14n.tastinggenie.ui.common.DropdownOption
import io.github.pyth0n14n.tastinggenie.ui.common.GroupedMultiSelectDropdown
import io.github.pyth0n14n.tastinggenie.ui.common.LabeledTextField
import io.github.pyth0n14n.tastinggenie.ui.common.SimpleDropdown

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
    dropdownField(
        labelRes = R.string.label_sweet,
        selectedValue = state.sweet?.name,
        options = uiData.tasteOptions,
        field = ReviewSelectionField.SWEET,
        onAction = onAction,
    )
    dropdownField(
        labelRes = R.string.label_sour,
        selectedValue = state.sour?.name,
        options = uiData.tasteOptions,
        field = ReviewSelectionField.SOUR,
        onAction = onAction,
    )
    dropdownField(
        labelRes = R.string.label_bitter,
        selectedValue = state.bitter?.name,
        options = uiData.tasteOptions,
        field = ReviewSelectionField.BITTER,
        onAction = onAction,
    )
    dropdownField(
        labelRes = R.string.label_umami,
        selectedValue = state.umami?.name,
        options = uiData.tasteOptions,
        field = ReviewSelectionField.UMAMI,
        onAction = onAction,
    )
    dropdownField(
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
    textField(
        labelRes = R.string.label_review_date,
        value = state.date,
        field = ReviewTextField.DATE,
        onAction = onAction,
    )
    item {
        Text(
            text = reviewTextResource(R.string.label_date_format_hint),
            style = MaterialTheme.typography.bodySmall,
        )
    }
    textField(
        labelRes = R.string.label_bar,
        value = state.bar,
        field = ReviewTextField.BAR,
        onAction = onAction,
    )
    textField(
        labelRes = R.string.label_price,
        value = state.price,
        field = ReviewTextField.PRICE,
        onAction = onAction,
    )
    textField(
        labelRes = R.string.label_volume,
        value = state.volume,
        field = ReviewTextField.VOLUME,
        onAction = onAction,
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
    dropdownField(
        R.string.label_viscosity,
        state.viscosity?.toString(),
        uiData.viscosityOptions,
        ReviewSelectionField.VISCOSITY,
        onAction,
    )
    dropdownField(
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
        labelRes = R.string.label_scene,
        value = state.scene,
        field = ReviewTextField.SCENE,
        onAction = onAction,
    )
    textField(
        labelRes = R.string.label_dish,
        value = state.dish,
        field = ReviewTextField.DISH,
        onAction = onAction,
    )
    textField(
        labelRes = R.string.label_comment,
        value = state.comment,
        field = ReviewTextField.COMMENT,
        onAction = onAction,
        singleLine = false,
    )
    dropdownField(
        labelRes = R.string.label_overall_review,
        selectedValue = state.review?.name,
        options = overallReviewOptions,
        field = ReviewSelectionField.OVERALL_REVIEW,
        onAction = onAction,
    )
}

private fun LazyListScope.textField(
    labelRes: Int,
    value: String,
    field: ReviewTextField,
    onAction: (ReviewEditAction) -> Unit,
    singleLine: Boolean = true,
) {
    item {
        LabeledTextField(
            label = reviewTextResource(labelRes),
            value = value,
            onValueChange = { next ->
                onAction(ReviewEditAction.TextChanged(field = field, value = next))
            },
            singleLine = singleLine,
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
