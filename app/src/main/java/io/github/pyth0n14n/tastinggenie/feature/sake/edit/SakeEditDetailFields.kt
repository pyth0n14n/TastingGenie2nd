package io.github.pyth0n14n.tastinggenie.feature.sake.edit

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import io.github.pyth0n14n.tastinggenie.R

@Composable
internal fun SakeEditDetailInfoSection(
    state: SakeEditUiState,
    callbacks: SakeEditCallbacks,
) {
    SakeEditSection(title = stringResource(R.string.label_sake_section_detail)) {
        SakeEditResponsiveFieldGrid {
            field { SakeKojiMaiField(state = state, callbacks = callbacks) }
            field { SakeKojiPolishField(state = state, callbacks = callbacks) }
            field { SakeKakeMaiField(state = state, callbacks = callbacks) }
            field { SakeKakePolishField(state = state, callbacks = callbacks) }
            field { SakeDegreeField(state = state, callbacks = callbacks) }
            field { SakeAcidityField(state = state, callbacks = callbacks) }
            field { SakeAminoField(state = state, callbacks = callbacks) }
            field { SakeAlcoholField(state = state, callbacks = callbacks) }
            field {
                SakeSourceField(
                    labelRes = R.string.label_yeast,
                    value = state.yeast,
                    field = SakeTextField.YEAST,
                    state = state,
                    callbacks = callbacks,
                )
            }
            field {
                SakeSourceField(
                    labelRes = R.string.label_water,
                    value = state.water,
                    field = SakeTextField.WATER,
                    state = state,
                    callbacks = callbacks,
                )
            }
        }
    }
}

@Composable
private fun SakeDegreeField(
    state: SakeEditUiState,
    callbacks: SakeEditCallbacks,
) {
    SakeTextFieldContent(
        labelRes = R.string.label_sake_degree,
        state = state,
        callbacks = callbacks,
        ui =
            SakeTextFieldUi(
                value = state.sakeDegree,
                field = SakeTextField.SAKE_DEGREE,
                presentation =
                    SakeFieldPresentation(
                        validationField = SakeValidationField.SAKE_DEGREE,
                        suffixRes = R.string.suffix_degree,
                        keyboardType = KeyboardType.Decimal,
                    ),
            ),
    )
}

@Composable
private fun SakeAcidityField(
    state: SakeEditUiState,
    callbacks: SakeEditCallbacks,
) {
    SakeTextFieldContent(
        labelRes = R.string.label_acidity,
        state = state,
        callbacks = callbacks,
        ui =
            SakeTextFieldUi(
                value = state.acidity,
                field = SakeTextField.ACIDITY,
                presentation =
                    SakeFieldPresentation(
                        validationField = SakeValidationField.ACIDITY,
                        suffixRes = R.string.suffix_degree,
                        keyboardType = KeyboardType.Decimal,
                    ),
            ),
    )
}

@Composable
private fun SakeAminoField(
    state: SakeEditUiState,
    callbacks: SakeEditCallbacks,
) {
    SakeTextFieldContent(
        labelRes = R.string.label_amino,
        state = state,
        callbacks = callbacks,
        ui =
            SakeTextFieldUi(
                value = state.amino,
                field = SakeTextField.AMINO,
                presentation =
                    SakeFieldPresentation(
                        validationField = SakeValidationField.AMINO,
                        suffixRes = R.string.suffix_degree,
                        keyboardType = KeyboardType.Decimal,
                    ),
            ),
    )
}

@Composable
private fun SakeAlcoholField(
    state: SakeEditUiState,
    callbacks: SakeEditCallbacks,
) {
    SakeTextFieldContent(
        labelRes = R.string.label_alcohol,
        state = state,
        callbacks = callbacks,
        ui =
            SakeTextFieldUi(
                value = state.alcohol,
                field = SakeTextField.ALCOHOL,
                presentation =
                    SakeFieldPresentation(
                        validationField = SakeValidationField.ALCOHOL,
                        suffixRes = R.string.suffix_degree,
                        keyboardType = KeyboardType.Number,
                    ),
            ),
    )
}

@Composable
private fun SakeKojiMaiField(
    state: SakeEditUiState,
    callbacks: SakeEditCallbacks,
) {
    SakeTextFieldContent(
        labelRes = R.string.label_koji_mai,
        state = state,
        callbacks = callbacks,
        ui = SakeTextFieldUi(value = state.kojiMai, field = SakeTextField.KOJI_MAI),
    )
}

@Composable
private fun SakeKojiPolishField(
    state: SakeEditUiState,
    callbacks: SakeEditCallbacks,
) {
    SakeTextFieldContent(
        labelRes = R.string.label_koji_polish,
        state = state,
        callbacks = callbacks,
        ui =
            SakeTextFieldUi(
                value = state.kojiPolish,
                field = SakeTextField.KOJI_POLISH,
                presentation =
                    SakeFieldPresentation(
                        validationField = SakeValidationField.KOJI_POLISH,
                        suffixRes = R.string.suffix_percent,
                        keyboardType = KeyboardType.Number,
                    ),
            ),
    )
}

@Composable
private fun SakeKakeMaiField(
    state: SakeEditUiState,
    callbacks: SakeEditCallbacks,
) {
    SakeTextFieldContent(
        labelRes = R.string.label_kake_mai,
        state = state,
        callbacks = callbacks,
        ui = SakeTextFieldUi(value = state.kakeMai, field = SakeTextField.KAKE_MAI),
    )
}

@Composable
private fun SakeKakePolishField(
    state: SakeEditUiState,
    callbacks: SakeEditCallbacks,
) {
    SakeTextFieldContent(
        labelRes = R.string.label_kake_polish,
        state = state,
        callbacks = callbacks,
        ui =
            SakeTextFieldUi(
                value = state.kakePolish,
                field = SakeTextField.KAKE_POLISH,
                presentation =
                    SakeFieldPresentation(
                        validationField = SakeValidationField.KAKE_POLISH,
                        suffixRes = R.string.suffix_percent,
                        keyboardType = KeyboardType.Number,
                    ),
            ),
    )
}

@Composable
private fun SakeSourceField(
    labelRes: Int,
    value: String,
    field: SakeTextField,
    state: SakeEditUiState,
    callbacks: SakeEditCallbacks,
) {
    SakeTextFieldContent(
        labelRes = labelRes,
        state = state,
        callbacks = callbacks,
        ui = SakeTextFieldUi(value = value, field = field),
    )
}
