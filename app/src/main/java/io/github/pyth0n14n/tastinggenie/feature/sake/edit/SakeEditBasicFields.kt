package io.github.pyth0n14n.tastinggenie.feature.sake.edit

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeClassification
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeGrade
import io.github.pyth0n14n.tastinggenie.ui.common.FormFieldState
import io.github.pyth0n14n.tastinggenie.ui.common.GroupedMultiSelectDropdown
import io.github.pyth0n14n.tastinggenie.ui.common.GroupedSingleSelectDropdown
import io.github.pyth0n14n.tastinggenie.ui.common.RequiredFieldHint
import io.github.pyth0n14n.tastinggenie.ui.common.SimpleDropdown
import io.github.pyth0n14n.tastinggenie.ui.common.validationErrorText

@Composable
internal fun SakeEditBasicInfoSection(
    state: SakeEditUiState,
    uiData: SakeEditFormUiData,
    callbacks: SakeEditCallbacks,
) {
    SakeEditSection(title = stringResource(R.string.label_review_section_basic)) {
        RequiredFieldHint()
        SakeEditResponsiveFieldGrid {
            field { SakeNameField(state = state, callbacks = callbacks) }
            field { SakeGradeField(state = state, uiData = uiData, callbacks = callbacks) }
            if (state.grade == SakeGrade.OTHER) {
                field { SakeGradeOtherField(state = state, callbacks = callbacks) }
            }
            fullWidthField { SakeClassificationField(state = state, uiData = uiData, callbacks = callbacks) }
            if (state.classifications.contains(SakeClassification.OTHER)) {
                fullWidthField { SakeClassificationOtherField(state = state, callbacks = callbacks) }
            }
            fullWidthField { SakeMakerField(state = state, callbacks = callbacks) }
            field { SakePrefectureField(state = state, uiData = uiData, callbacks = callbacks) }
            field { SakeCityField(state = state, callbacks = callbacks) }
        }
    }
}

@Composable
private fun SakeNameField(
    state: SakeEditUiState,
    callbacks: SakeEditCallbacks,
) {
    SakeTextFieldContent(
        labelRes = R.string.label_sake_name,
        state = state,
        callbacks = callbacks,
        ui =
            SakeTextFieldUi(
                value = state.name,
                field = SakeTextField.NAME,
                presentation = SakeFieldPresentation(validationField = SakeValidationField.NAME, required = true),
            ),
    )
}

@Composable
private fun SakeGradeField(
    state: SakeEditUiState,
    uiData: SakeEditFormUiData,
    callbacks: SakeEditCallbacks,
) {
    val label = stringResource(R.string.label_grade)
    SimpleDropdown(
        label = label,
        selectedLabel = state.gradeOptions.selectedLabel(state.grade?.name),
        options = uiData.gradeOptions,
        onSelected = callbacks.onGradeSelected,
        fieldState =
            FormFieldState(
                required = true,
                errorText =
                    state.validationErrors[SakeValidationField.GRADE]?.let { error ->
                        validationErrorText(label = label, error = error)
                    },
            ),
    )
}

@Composable
private fun SakeGradeOtherField(
    state: SakeEditUiState,
    callbacks: SakeEditCallbacks,
) {
    SakeTextFieldContent(
        labelRes = R.string.label_grade_other,
        state = state,
        callbacks = callbacks,
        ui = SakeTextFieldUi(value = state.gradeOther, field = SakeTextField.GRADE_OTHER),
    )
}

@Composable
private fun SakeClassificationField(
    state: SakeEditUiState,
    uiData: SakeEditFormUiData,
    callbacks: SakeEditCallbacks,
) {
    GroupedMultiSelectDropdown(
        label = stringResource(R.string.label_classification),
        groups = uiData.classificationGroups,
        selectedValues = state.classifications.map { classification -> classification.name },
        onToggle = callbacks.onClassificationToggled,
    )
}

@Composable
private fun SakeClassificationOtherField(
    state: SakeEditUiState,
    callbacks: SakeEditCallbacks,
) {
    SakeTextFieldContent(
        labelRes = R.string.label_classification_other,
        state = state,
        callbacks = callbacks,
        ui = SakeTextFieldUi(value = state.typeOther, field = SakeTextField.TYPE_OTHER),
    )
}

@Composable
private fun SakeMakerField(
    state: SakeEditUiState,
    callbacks: SakeEditCallbacks,
) {
    SakeTextFieldContent(
        labelRes = R.string.label_maker,
        state = state,
        callbacks = callbacks,
        ui = SakeTextFieldUi(value = state.maker, field = SakeTextField.MAKER),
    )
}

@Composable
private fun SakePrefectureField(
    state: SakeEditUiState,
    uiData: SakeEditFormUiData,
    callbacks: SakeEditCallbacks,
) {
    GroupedSingleSelectDropdown(
        label = stringResource(R.string.label_prefecture),
        groups = uiData.prefectureGroups,
        selectedValue = state.prefecture?.name,
        onSelected = callbacks.onPrefectureSelected,
    )
}

@Composable
private fun SakeCityField(
    state: SakeEditUiState,
    callbacks: SakeEditCallbacks,
) {
    SakeTextFieldContent(
        labelRes = R.string.label_city,
        state = state,
        callbacks = callbacks,
        ui = SakeTextFieldUi(value = state.city, field = SakeTextField.CITY),
    )
}
