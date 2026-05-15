package io.github.pyth0n14n.tastinggenie.feature.sake.edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.pyth0n14n.tastinggenie.R

private val DetailHelpIconTouchSize = 24.dp

@Composable
internal fun SakeEditDetailInfoSection(
    state: SakeEditUiState,
    callbacks: SakeEditCallbacks,
) {
    var isHelpSheetVisible by remember { mutableStateOf(false) }
    SakeEditSection(
        title = stringResource(R.string.label_sake_section_detail),
        titleAction =
            if (state.showHelpHints) {
                {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                        contentDescription = stringResource(R.string.cd_sake_detail_help),
                        modifier =
                            Modifier
                                .size(DetailHelpIconTouchSize)
                                .clickable(onClick = { isHelpSheetVisible = true }),
                    )
                }
            } else {
                null
            },
    ) {
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
    if (isHelpSheetVisible) {
        SakeDetailHelpBottomSheet(onDismiss = { isHelpSheetVisible = false })
    }
}

@Composable
private fun SakeDegreeField(
    state: SakeEditUiState,
    callbacks: SakeEditCallbacks,
) {
    Column {
        SakeDegreeTasteLabel(state.sakeDegree)
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
                            prefixText = sakeDegreePrefix(state.sakeDegree),
                            keyboardType = KeyboardType.Decimal,
                        ),
                ),
        )
    }
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
                        keyboardType = KeyboardType.Decimal,
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
