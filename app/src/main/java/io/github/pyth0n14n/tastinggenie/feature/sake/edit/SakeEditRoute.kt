package io.github.pyth0n14n.tastinggenie.feature.sake.edit

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.image.createPendingSakeCameraCapture
import io.github.pyth0n14n.tastinggenie.image.deletePendingSakeCameraCapture
import io.github.pyth0n14n.tastinggenie.ui.common.DropdownOption
import io.github.pyth0n14n.tastinggenie.ui.common.DropdownOptionGroup
import io.github.pyth0n14n.tastinggenie.ui.common.LoadingContent

private const val SCREEN_PADDING = 16
private const val ITEM_SPACING = 12
private const val SAKE_EDIT_FORM_TAG = "sake_edit_form"

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SakeEditRoute(
    onBack: () -> Unit,
    viewModel: SakeEditViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var pendingCameraCaptureSourceUri by rememberSaveable { mutableStateOf<String?>(null) }
    val imagePickerLauncher =
        rememberLauncherForActivityResult(PickMultipleVisualMedia()) { uris ->
            uris.forEach { uri ->
                viewModel.onImageSelected(uri.toString())
            }
        }
    val cameraCaptureLauncher =
        rememberLauncherForActivityResult(TakePicture()) { isSuccess ->
            val sourceUri = pendingCameraCaptureSourceUri
            pendingCameraCaptureSourceUri = null
            if (sourceUri == null) {
                return@rememberLauncherForActivityResult
            }
            if (isSuccess) {
                viewModel.onImageSelected(sourceUri)
            } else {
                context.deletePendingSakeCameraCapture(sourceUri)
            }
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
            onCaptureImageRequest = {
                val capture = context.createPendingSakeCameraCapture()
                pendingCameraCaptureSourceUri = capture.sourceUri
                cameraCaptureLauncher.launch(capture.launchUri)
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
    var deleteTargetImageUri by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val gradeOptions = state.gradeOptions.toOptions()
    val classificationGroups = state.classificationOptions.toClassificationGroups()
    val prefectureGroups = state.prefectureOptions.toPrefectureGroups()
    LaunchedEffect(state.validationFailureCount) {
        val targetIndex = state.firstInvalidSectionIndex()
        if (targetIndex != null) {
            listState.animateScrollToItem(index = targetIndex)
        }
    }
    Scaffold(
        topBar = { SakeEditTopBar(onBack = onBack) },
        bottomBar = {
            SakeEditBottomBar(
                state = state,
                onSave = onSave,
            )
        },
    ) { padding ->
        DeleteSakeImageDialog(
            isVisible = deleteTargetImageUri != null,
            onConfirm = {
                deleteTargetImageUri?.let(callbacks.onDeleteImage)
                deleteTargetImageUri = null
            },
            onDismiss = { deleteTargetImageUri = null },
        )
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .testTag(SAKE_EDIT_FORM_TAG),
            state = listState,
            contentPadding = PaddingValues(SCREEN_PADDING.dp),
            verticalArrangement = Arrangement.spacedBy(ITEM_SPACING.dp),
        ) {
            sakeEditSectionItems(
                state = state,
                uiData =
                    SakeEditFormUiData(
                        gradeOptions = gradeOptions,
                        classificationGroups = classificationGroups,
                        prefectureGroups = prefectureGroups,
                    ),
                callbacks = callbacks,
                onDeleteImageRequest = { imageUri -> deleteTargetImageUri = imageUri },
            )
        }
    }
}

@Composable
private fun SakeEditBottomBar(
    state: SakeEditUiState,
    onSave: () -> Unit,
) {
    Surface(
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = SCREEN_PADDING.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            state.error?.let { error ->
                Text(
                    text = stringResource(error.messageResId),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            SaveButton(
                isSaving = state.isSaving,
                isEnabled = !state.isEditTargetMissing,
                onSave = onSave,
            )
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.sakeEditSectionItems(
    state: SakeEditUiState,
    uiData: SakeEditFormUiData,
    callbacks: SakeEditCallbacks,
    onDeleteImageRequest: (String) -> Unit,
) {
    item(key = SAKE_SECTION_IMAGE) {
        SakeEditImageSection(
            state = state,
            callbacks = callbacks,
            onDeleteImageRequest = onDeleteImageRequest,
        )
    }
    item(key = SAKE_SECTION_BASIC) {
        SakeEditBasicInfoSection(
            state = state,
            uiData = uiData,
            callbacks = callbacks,
        )
    }
    item(key = SAKE_SECTION_DETAIL) {
        SakeEditDetailInfoSection(
            state = state,
            callbacks = callbacks,
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
    val onCaptureImageRequest: () -> Unit,
    val onDeleteImage: (String) -> Unit,
)

data class SakeEditFormUiData(
    val gradeOptions: List<DropdownOption>,
    val classificationGroups: List<DropdownOptionGroup>,
    val prefectureGroups: List<DropdownOptionGroup>,
)
