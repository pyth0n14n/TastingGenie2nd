package io.github.pyth0n14n.tastinggenie.feature.review.edit

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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.feature.review.ReviewSection
import io.github.pyth0n14n.tastinggenie.ui.common.DropdownOption
import io.github.pyth0n14n.tastinggenie.ui.common.LoadingContent
import io.github.pyth0n14n.tastinggenie.ui.common.RequiredFieldHint

private const val SCREEN_PADDING = 16
private const val ITEM_SPACING = 12
private const val REVIEW_DATE_INDEX = 2
private const val REVIEW_PRICE_INDEX = 4
private const val REVIEW_VOLUME_INDEX = 5

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ReviewEditRoute(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: ReviewEditViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedSectionName by rememberSaveable { mutableStateOf(ReviewSection.BASIC.name) }
    val selectedSection = ReviewSection.valueOf(selectedSectionName)
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            viewModel.consumeSaved()
            onSaved()
        }
    }
    ReviewEditScreen(
        onBack = onBack,
        content =
            ReviewEditScreenContent(
                state = state,
                onAction = viewModel::onAction,
                onSave = viewModel::save,
                viscosityOptions = viscosityOptions(),
                volumeShortcutOptions = volumeShortcutOptions(),
                selectedSection = selectedSection,
                onSectionSelected = { next -> selectedSectionName = next.name },
            ),
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ReviewEditScreen(
    onBack: () -> Unit,
    content: ReviewEditScreenContent,
) {
    if (content.state.isLoading) {
        LoadingContent()
        return
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (content.state.sakeName.isBlank()) {
                            stringResource(R.string.screen_review_edit)
                        } else {
                            "${stringResource(R.string.label_sake)}: ${content.state.sakeName}"
                        },
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(stringResource(R.string.action_back))
                    }
                },
            )
        },
    ) { padding ->
        ReviewEditBody(
            content = content,
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
private fun viscosityOptions(): List<DropdownOption> =
    listOf(
        DropdownOption(value = "1", label = stringResource(R.string.label_viscosity_1)),
        DropdownOption(value = "2", label = stringResource(R.string.label_viscosity_2)),
        DropdownOption(value = "3", label = stringResource(R.string.label_viscosity_3)),
        DropdownOption(value = "4", label = stringResource(R.string.label_viscosity_4)),
        DropdownOption(value = "5", label = stringResource(R.string.label_viscosity_5)),
    )

@Composable
private fun ReviewEditBody(
    content: ReviewEditScreenContent,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(content.state.validationFailureCount) {
        val targetIndex = content.state.firstInvalidFieldIndex()
        if (targetIndex != null) {
            listState.animateScrollToItem(index = targetIndex)
        }
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(SCREEN_PADDING.dp),
        verticalArrangement = Arrangement.spacedBy(ITEM_SPACING.dp),
    ) {
        reviewEditHeaderItems()
        val formUiData =
            ReviewEditFormUiData(
                singleChoiceUiData =
                    SingleChoiceUiData(
                        temperatureOptions = content.state.temperatureOptions.toOptions(),
                        colorOptions = content.state.colorOptions.toOptions(),
                        intensityOptions = content.state.intensityOptions.toOptions(),
                        viscosityOptions = content.viscosityOptions,
                    ),
                tasteOptions = content.state.tasteOptions.toOptions(),
                overallReviewOptions = content.state.overallReviewOptions.toOptions(),
                aromaUiData =
                    AromaUiData(
                        categories = content.state.aromaCategories,
                    ),
                volumeShortcutOptions = content.volumeShortcutOptions,
            )
        reviewEditFormContent(
            state = content.state,
            onAction = content.onAction,
            uiData = formUiData,
            selectedSection = content.selectedSection,
            onSectionSelected = content.onSectionSelected,
        )
        reviewEditFooterItems(
            state = content.state,
            onSave = content.onSave,
        )
    }
}

@Composable
private fun volumeShortcutOptions(): List<DropdownOption> =
    listOf(
        DropdownOption(value = "120", label = stringResource(R.string.label_volume_glass)),
        DropdownOption(value = "180", label = stringResource(R.string.label_volume_one_jo)),
        DropdownOption(value = "720", label = stringResource(R.string.label_volume_four_go)),
        DropdownOption(value = "1800", label = stringResource(R.string.label_volume_one_sho)),
    )

private fun ReviewEditUiState.firstInvalidFieldIndex(): Int? {
    if (validationErrors.isEmpty()) {
        return null
    }
    return when {
        validationErrors.containsKey(ReviewValidationField.DATE) -> REVIEW_DATE_INDEX
        validationErrors.containsKey(ReviewValidationField.PRICE) -> REVIEW_PRICE_INDEX
        validationErrors.containsKey(ReviewValidationField.VOLUME) -> REVIEW_VOLUME_INDEX
        else -> null
    }
}

data class ReviewEditScreenContent(
    val state: ReviewEditUiState,
    val onAction: (ReviewEditAction) -> Unit,
    val onSave: () -> Unit,
    val viscosityOptions: List<DropdownOption>,
    val volumeShortcutOptions: List<DropdownOption>,
    val selectedSection: ReviewSection,
    val onSectionSelected: (ReviewSection) -> Unit,
)

private fun androidx.compose.foundation.lazy.LazyListScope.reviewEditHeaderItems() {
    item(key = "required_hint", contentType = "hint") {
        RequiredFieldHint()
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.reviewEditFooterItems(
    state: ReviewEditUiState,
    onSave: () -> Unit,
) {
    item(key = "error", contentType = "error") {
        ReviewEditError(state = state)
    }
    item(key = "save", contentType = "save") {
        ReviewEditSaveButton(
            isSaving = state.isSaving,
            isInputLocked = state.isInputLocked,
            onSave = onSave,
        )
    }
}

@Composable
private fun ReviewEditError(state: ReviewEditUiState) {
    if (state.error == null) {
        return
    }
    Text(
        text = stringResource(state.error.messageResId),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error,
    )
}

@Composable
private fun ReviewEditSaveButton(
    isSaving: Boolean,
    isInputLocked: Boolean,
    onSave: () -> Unit,
) {
    Button(
        onClick = onSave,
        modifier = Modifier.fillMaxWidth(),
        enabled = !isSaving && !isInputLocked,
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
