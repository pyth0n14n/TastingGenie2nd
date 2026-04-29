package io.github.pyth0n14n.tastinggenie.feature.sake.edit

import androidx.annotation.StringRes
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.ui.common.FormFieldState
import io.github.pyth0n14n.tastinggenie.ui.common.LabeledTextField
import io.github.pyth0n14n.tastinggenie.ui.common.validationErrorText

fun LazyListScope.textFieldItem(
    @StringRes labelRes: Int,
    state: SakeEditUiState,
    callbacks: SakeEditCallbacks,
    ui: SakeTextFieldUi,
    itemKey: Any? = null,
) {
    if (itemKey == null) {
        item {
            SakeTextFieldContent(
                labelRes = labelRes,
                state = state,
                callbacks = callbacks,
                ui = ui,
            )
        }
    } else {
        item(key = itemKey) {
            SakeTextFieldContent(
                labelRes = labelRes,
                state = state,
                callbacks = callbacks,
                ui = ui,
            )
        }
    }
}

@androidx.compose.runtime.Composable
internal fun SakeTextFieldContent(
    @StringRes labelRes: Int,
    state: SakeEditUiState,
    callbacks: SakeEditCallbacks,
    ui: SakeTextFieldUi,
) {
    val label = stringResource(labelRes)
    val suffix = ui.presentation.suffixRes?.let { suffixRes -> stringResource(suffixRes) }
    LabeledTextField(
        label = label,
        value = ui.value,
        onValueChange = { updated -> callbacks.onTextChanged(ui.field, updated) },
        fieldState =
            FormFieldState(
                required = ui.presentation.required,
                errorText =
                    ui.presentation.validationField?.let { validationField ->
                        state.validationErrors[validationField]?.let { error ->
                            validationErrorText(label = label, error = error)
                        }
                    },
                suffixText = suffix,
                keyboardOptions =
                    ui.presentation.keyboardType?.let { keyboardType ->
                        KeyboardOptions(keyboardType = keyboardType)
                    } ?: KeyboardOptions.Default,
            ),
    )
}

data class SakeFieldPresentation(
    val validationField: SakeValidationField? = null,
    val required: Boolean = false,
    @param:StringRes val suffixRes: Int? = null,
    val keyboardType: KeyboardType? = null,
)

data class SakeTextFieldUi(
    val value: String,
    val field: SakeTextField,
    val presentation: SakeFieldPresentation = SakeFieldPresentation(),
)

fun LazyListScope.metadataFields(
    state: SakeEditUiState,
    callbacks: SakeEditCallbacks,
) {
    numericMetadataFields(state = state, callbacks = callbacks)
    sourceMetadataFields(state = state, callbacks = callbacks)
}

private fun LazyListScope.numericMetadataFields(
    state: SakeEditUiState,
    callbacks: SakeEditCallbacks,
) {
    numericValueFields(state = state, callbacks = callbacks)
    riceMetadataFields(state = state, callbacks = callbacks)
}

private fun LazyListScope.numericValueFields(
    state: SakeEditUiState,
    callbacks: SakeEditCallbacks,
) {
    textFieldItem(
        R.string.label_sake_degree,
        state,
        callbacks,
        SakeTextFieldUi(
            value = state.sakeDegree,
            field = SakeTextField.SAKE_DEGREE,
            presentation = SakeFieldPresentation(validationField = SakeValidationField.SAKE_DEGREE),
        ),
        itemKey = SAKE_ROW_SAKE_DEGREE,
    )
    textFieldItem(
        R.string.label_acidity,
        state,
        callbacks,
        SakeTextFieldUi(
            value = state.acidity,
            field = SakeTextField.ACIDITY,
            presentation = SakeFieldPresentation(validationField = SakeValidationField.ACIDITY),
        ),
        itemKey = SAKE_ROW_ACIDITY,
    )
    textFieldItem(
        R.string.label_alcohol,
        state,
        callbacks,
        SakeTextFieldUi(
            value = state.alcohol,
            field = SakeTextField.ALCOHOL,
            presentation = SakeFieldPresentation(validationField = SakeValidationField.ALCOHOL),
        ),
        itemKey = SAKE_ROW_ALCOHOL,
    )
}

private fun LazyListScope.riceMetadataFields(
    state: SakeEditUiState,
    callbacks: SakeEditCallbacks,
) {
    textFieldItem(
        R.string.label_koji_mai,
        state,
        callbacks,
        SakeTextFieldUi(value = state.kojiMai, field = SakeTextField.KOJI_MAI),
        itemKey = SAKE_ROW_KOJI_MAI,
    )
    textFieldItem(
        R.string.label_koji_polish,
        state,
        callbacks,
        SakeTextFieldUi(
            value = state.kojiPolish,
            field = SakeTextField.KOJI_POLISH,
            presentation = SakeFieldPresentation(validationField = SakeValidationField.KOJI_POLISH),
        ),
        itemKey = SAKE_ROW_KOJI_POLISH,
    )
    textFieldItem(
        R.string.label_kake_mai,
        state,
        callbacks,
        SakeTextFieldUi(value = state.kakeMai, field = SakeTextField.KAKE_MAI),
        itemKey = SAKE_ROW_KAKE_MAI,
    )
    textFieldItem(
        R.string.label_kake_polish,
        state,
        callbacks,
        SakeTextFieldUi(
            value = state.kakePolish,
            field = SakeTextField.KAKE_POLISH,
            presentation = SakeFieldPresentation(validationField = SakeValidationField.KAKE_POLISH),
        ),
        itemKey = SAKE_ROW_KAKE_POLISH,
    )
}

private fun LazyListScope.sourceMetadataFields(
    state: SakeEditUiState,
    callbacks: SakeEditCallbacks,
) {
    textFieldItem(
        R.string.label_yeast,
        state,
        callbacks,
        SakeTextFieldUi(value = state.yeast, field = SakeTextField.YEAST),
        itemKey = SAKE_ROW_YEAST,
    )
    textFieldItem(
        R.string.label_water,
        state,
        callbacks,
        SakeTextFieldUi(value = state.water, field = SakeTextField.WATER),
        itemKey = SAKE_ROW_WATER,
    )
}
