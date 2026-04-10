package io.github.pyth0n14n.tastinggenie.feature.sake.edit

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import io.github.pyth0n14n.tastinggenie.ui.common.FormFieldState
import io.github.pyth0n14n.tastinggenie.ui.common.GroupedMultiSelectDropdown
import io.github.pyth0n14n.tastinggenie.ui.common.GroupedSingleSelectDropdown
import io.github.pyth0n14n.tastinggenie.ui.common.LoadingContent
import io.github.pyth0n14n.tastinggenie.ui.common.RequiredFieldHint
import io.github.pyth0n14n.tastinggenie.ui.common.SimpleDropdown
import io.github.pyth0n14n.tastinggenie.ui.common.validationErrorText

private const val SCREEN_PADDING = 16
private const val ITEM_SPACING = 12

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SakeEditRoute(
    onBack: () -> Unit,
    viewModel: SakeEditViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val imagePickerLauncher =
        rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
            uri?.toString()?.let(viewModel::onImageSelected)
        }
    val callbacks =
        SakeEditCallbacks(
            onTextChanged = viewModel::onTextChanged,
            onGradeSelected = viewModel::onGradeSelected,
            onClassificationToggled = viewModel::onClassificationToggled,
            onPrefectureSelected = viewModel::onPrefectureSelected,
            onPickImageRequest = {
                imagePickerLauncher.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
            },
            onDeleteImage = viewModel::removeImage,
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
    var isDeleteImageDialogVisible by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val gradeOptions = state.gradeOptions.toOptions()
    val classificationGroups = state.classificationOptions.toClassificationGroups()
    val prefectureGroups = state.prefectureOptions.toPrefectureGroups()
    LaunchedEffect(state.validationFailureCount) {
        val targetIndex = state.firstInvalidFieldIndex()
        if (targetIndex != null) {
            listState.animateScrollToItem(index = targetIndex)
        }
    }
    Scaffold(
        topBar = { SakeEditTopBar(onBack = onBack) },
    ) { padding ->
        DeleteSakeImageDialog(
            isVisible = isDeleteImageDialogVisible,
            onConfirm = {
                callbacks.onDeleteImage()
                isDeleteImageDialogVisible = false
            },
            onDismiss = { isDeleteImageDialogVisible = false },
        )
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
            state = listState,
            contentPadding = PaddingValues(SCREEN_PADDING.dp),
            verticalArrangement = Arrangement.spacedBy(ITEM_SPACING.dp),
        ) {
            sakeEditHeaderItems()
            formFields(
                state = state,
                uiData =
                    SakeEditFormUiData(
                        gradeOptions = gradeOptions,
                        classificationGroups = classificationGroups,
                        prefectureGroups = prefectureGroups,
                    ),
                callbacks = callbacks,
                onDeleteImageRequest = { isDeleteImageDialogVisible = true },
            )
            sakeEditFooterItems(
                state = state,
                onSave = onSave,
            )
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.sakeEditHeaderItems() {
    item(key = SAKE_ROW_REQUIRED_HINT, contentType = "hint") {
        RequiredFieldHint()
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.sakeEditFooterItems(
    state: SakeEditUiState,
    onSave: () -> Unit,
) {
    item(key = SAKE_ROW_ERROR, contentType = "error") {
        if (state.error != null) {
            Text(
                text = stringResource(state.error.messageResId),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
    item(key = SAKE_ROW_SAVE, contentType = "save") {
        SaveButton(
            isSaving = state.isSaving,
            isEnabled = !state.isEditTargetMissing,
            onSave = onSave,
        )
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.formFields(
    state: SakeEditUiState,
    uiData: SakeEditFormUiData,
    callbacks: SakeEditCallbacks,
    onDeleteImageRequest: () -> Unit,
) {
    basicFields(
        state = state,
        uiData = uiData,
        callbacks = callbacks,
        onDeleteImageRequest = onDeleteImageRequest,
    )
    classificationFields(state = state, uiData = uiData, callbacks = callbacks)
    metadataFields(state = state, callbacks = callbacks)
}

private fun androidx.compose.foundation.lazy.LazyListScope.basicFields(
    state: SakeEditUiState,
    uiData: SakeEditFormUiData,
    callbacks: SakeEditCallbacks,
    onDeleteImageRequest: () -> Unit,
) {
    textFieldItem(
        labelRes = R.string.label_sake_name,
        state = state,
        callbacks = callbacks,
        ui =
            SakeTextFieldUi(
                value = state.name,
                field = SakeTextField.NAME,
                presentation = SakeFieldPresentation(validationField = SakeValidationField.NAME, required = true),
            ),
        itemKey = SAKE_ROW_NAME,
    )
    item(key = SAKE_ROW_GRADE) {
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
    item(key = SAKE_ROW_IMAGE) {
        SakeImageField(
            imageUri = state.imagePreviewUri,
            isSaving = state.isSaving,
            onPickImage = callbacks.onPickImageRequest,
            onDeleteImageRequest = onDeleteImageRequest,
        )
    }
    if (state.grade == SakeGrade.OTHER) {
        textFieldItem(
            labelRes = R.string.label_grade_other,
            state = state,
            callbacks = callbacks,
            ui = SakeTextFieldUi(value = state.gradeOther, field = SakeTextField.GRADE_OTHER),
            itemKey = SAKE_ROW_GRADE_OTHER,
        )
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.classificationFields(
    state: SakeEditUiState,
    uiData: SakeEditFormUiData,
    callbacks: SakeEditCallbacks,
) {
    item(key = SAKE_ROW_CLASSIFICATION) {
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
            state = state,
            callbacks = callbacks,
            ui = SakeTextFieldUi(value = state.typeOther, field = SakeTextField.TYPE_OTHER),
            itemKey = SAKE_ROW_CLASSIFICATION_OTHER,
        )
    }
    textFieldItem(
        labelRes = R.string.label_maker,
        state = state,
        callbacks = callbacks,
        ui = SakeTextFieldUi(value = state.maker, field = SakeTextField.MAKER),
        itemKey = SAKE_ROW_MAKER,
    )
    item(key = SAKE_ROW_PREFECTURE) {
        GroupedSingleSelectDropdown(
            label = stringResource(R.string.label_prefecture),
            groups = uiData.prefectureGroups,
            selectedValue = state.prefecture?.name,
            onSelected = callbacks.onPrefectureSelected,
        )
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
    val onPickImageRequest: () -> Unit,
    val onDeleteImage: () -> Unit,
)

data class SakeEditFormUiData(
    val gradeOptions: List<DropdownOption>,
    val classificationGroups: List<DropdownOptionGroup>,
    val prefectureGroups: List<DropdownOptionGroup>,
)
