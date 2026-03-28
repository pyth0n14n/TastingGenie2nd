package io.github.pyth0n14n.tastinggenie.feature.sake.edit

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeClassification
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeGrade
import io.github.pyth0n14n.tastinggenie.ui.common.DropdownOption
import io.github.pyth0n14n.tastinggenie.ui.common.DropdownOptionGroup
import io.github.pyth0n14n.tastinggenie.ui.common.GroupedMultiSelectDropdown
import io.github.pyth0n14n.tastinggenie.ui.common.GroupedSingleSelectDropdown
import io.github.pyth0n14n.tastinggenie.ui.common.LabeledTextField
import io.github.pyth0n14n.tastinggenie.ui.common.LoadingContent
import io.github.pyth0n14n.tastinggenie.ui.common.SimpleDropdown

private const val SCREEN_PADDING = 16
private const val ITEM_SPACING = 12

/**
 * Route for sake edit/create screen.
 *
 * Collects state from [SakeEditViewModel] and delegates rendering to [SakeEditScreen].
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SakeEditRoute(
    onBack: () -> Unit,
    viewModel: SakeEditViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val callbacks =
        SakeEditCallbacks(
            onTextChanged = viewModel::onTextChanged,
            onGradeSelected = viewModel::onGradeSelected,
            onClassificationToggled = viewModel::onClassificationToggled,
            onPrefectureSelected = viewModel::onPrefectureSelected,
        )
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            viewModel.consumeSaved()
            onBack()
        }
    }
    SakeEditScreen(
        state = state,
        callbacks = callbacks,
        onSave = viewModel::save,
        onBack = onBack,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SakeEditScreen(
    state: SakeEditUiState,
    callbacks: SakeEditCallbacks,
    onSave: () -> Unit,
    onBack: () -> Unit,
) {
    if (state.isLoading) {
        LoadingContent()
        return
    }
    val gradeOptions = state.gradeOptions.toOptions()
    val classificationGroups = state.classificationOptions.toClassificationGroups()
    val prefectureGroups = state.prefectureOptions.toPrefectureGroups()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.screen_sake_edit)) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(stringResource(R.string.action_back))
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
            contentPadding = PaddingValues(SCREEN_PADDING.dp),
            verticalArrangement = Arrangement.spacedBy(ITEM_SPACING.dp),
        ) {
            formFields(
                state = state,
                uiData =
                    SakeEditFormUiData(
                        gradeOptions = gradeOptions,
                        classificationGroups = classificationGroups,
                        prefectureGroups = prefectureGroups,
                    ),
                callbacks = callbacks,
            )
            errorMessage(state = state)
            item {
                SaveButton(
                    isSaving = state.isSaving,
                    isEnabled = !state.isEditTargetMissing,
                    onSave = onSave,
                )
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.formFields(
    state: SakeEditUiState,
    uiData: SakeEditFormUiData,
    callbacks: SakeEditCallbacks,
) {
    basicFields(state = state, uiData = uiData, callbacks = callbacks)
    classificationFields(state = state, uiData = uiData, callbacks = callbacks)
    metadataFields(state = state, callbacks = callbacks)
}

private fun androidx.compose.foundation.lazy.LazyListScope.basicFields(
    state: SakeEditUiState,
    uiData: SakeEditFormUiData,
    callbacks: SakeEditCallbacks,
) {
    textFieldItem(
        labelRes = R.string.label_sake_name,
        value = state.name,
        callbacks = callbacks,
        field = SakeTextField.NAME,
    )
    item {
        SimpleDropdown(
            label = stringResource(R.string.label_grade),
            selectedLabel = state.gradeOptions.selectedLabel(state.grade?.name),
            options = uiData.gradeOptions,
            onSelected = callbacks.onGradeSelected,
        )
    }
    if (state.grade == SakeGrade.OTHER) {
        textFieldItem(
            labelRes = R.string.label_grade_other,
            value = state.gradeOther,
            callbacks = callbacks,
            field = SakeTextField.GRADE_OTHER,
        )
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.classificationFields(
    state: SakeEditUiState,
    uiData: SakeEditFormUiData,
    callbacks: SakeEditCallbacks,
) {
    item {
        GroupedMultiSelectDropdown(
            label = stringResource(R.string.label_classification),
            groups = uiData.classificationGroups,
            selectedValues = state.classifications.map { classification -> classification.name },
            onToggle = callbacks.onClassificationToggled,
        )
    }
    if (state.classifications.contains(SakeClassification.OTHER)) {
        textFieldItem(
            labelRes = R.string.label_classification_other,
            value = state.typeOther,
            callbacks = callbacks,
            field = SakeTextField.TYPE_OTHER,
        )
    }
    textFieldItem(
        labelRes = R.string.label_maker,
        value = state.maker,
        callbacks = callbacks,
        field = SakeTextField.MAKER,
    )
    item {
        GroupedSingleSelectDropdown(
            label = stringResource(R.string.label_prefecture),
            groups = uiData.prefectureGroups,
            selectedValue = state.prefecture?.name,
            onSelected = callbacks.onPrefectureSelected,
        )
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.metadataFields(
    state: SakeEditUiState,
    callbacks: SakeEditCallbacks,
) {
    textFieldItem(R.string.label_sake_degree, state.sakeDegree, callbacks, SakeTextField.SAKE_DEGREE)
    textFieldItem(R.string.label_acidity, state.acidity, callbacks, SakeTextField.ACIDITY)
    textFieldItem(R.string.label_koji_mai, state.kojiMai, callbacks, SakeTextField.KOJI_MAI)
    textFieldItem(R.string.label_koji_polish, state.kojiPolish, callbacks, SakeTextField.KOJI_POLISH)
    textFieldItem(R.string.label_kake_mai, state.kakeMai, callbacks, SakeTextField.KAKE_MAI)
    textFieldItem(R.string.label_kake_polish, state.kakePolish, callbacks, SakeTextField.KAKE_POLISH)
    textFieldItem(R.string.label_alcohol, state.alcohol, callbacks, SakeTextField.ALCOHOL)
    textFieldItem(R.string.label_yeast, state.yeast, callbacks, SakeTextField.YEAST)
    textFieldItem(R.string.label_water, state.water, callbacks, SakeTextField.WATER)
}

private fun androidx.compose.foundation.lazy.LazyListScope.textFieldItem(
    @StringRes labelRes: Int,
    value: String,
    callbacks: SakeEditCallbacks,
    field: SakeTextField,
) {
    item {
        LabeledTextField(
            label = stringResource(labelRes),
            value = value,
            onValueChange = { updated -> callbacks.onTextChanged(field, updated) },
        )
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.errorMessage(state: SakeEditUiState) {
    item {
        if (state.error != null) {
            Text(
                text = stringResource(state.error.messageResId),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun SaveButton(
    isSaving: Boolean,
    isEnabled: Boolean,
    onSave: () -> Unit,
) {
    Button(
        onClick = onSave,
        modifier = Modifier.fillMaxWidth(),
        enabled = isEnabled && !isSaving,
    ) {
        Text(
            text =
                if (isSaving) {
                    stringResource(R.string.message_saving)
                } else {
                    stringResource(R.string.action_save)
                },
        )
    }
}

data class SakeEditCallbacks(
    val onTextChanged: (SakeTextField, String) -> Unit,
    val onGradeSelected: (String) -> Unit,
    val onClassificationToggled: (String) -> Unit,
    val onPrefectureSelected: (String?) -> Unit,
)

data class SakeEditFormUiData(
    val gradeOptions: List<DropdownOption>,
    val classificationGroups: List<DropdownOptionGroup>,
    val prefectureGroups: List<DropdownOptionGroup>,
)
