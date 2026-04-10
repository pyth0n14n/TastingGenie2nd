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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.pyth0n14n.tastinggenie.R
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
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            viewModel.consumeSaved()
            onSaved()
        }
    }
    ReviewEditScreen(
        state = state,
        onAction = viewModel::onAction,
        onSave = viewModel::save,
        onBack = onBack,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ReviewEditScreen(
    state: ReviewEditUiState,
    onAction: (ReviewEditAction) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
) {
    if (state.isLoading) {
        LoadingContent()
        return
    }
    val viscosityOptions = viscosityOptions()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.sakeName.isBlank()) {
                            stringResource(R.string.screen_review_edit)
                        } else {
                            state.sakeName
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
            state = state,
            onAction = onAction,
            onSave = onSave,
            viscosityOptions = viscosityOptions,
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
    state: ReviewEditUiState,
    onAction: (ReviewEditAction) -> Unit,
    onSave: () -> Unit,
    viscosityOptions: List<DropdownOption>,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(state.validationFailureCount) {
        val targetIndex = state.firstInvalidFieldIndex()
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
                        temperatureOptions = state.temperatureOptions.toOptions(),
                        colorOptions = state.colorOptions.toOptions(),
                        intensityOptions = state.intensityOptions.toOptions(),
                        viscosityOptions = viscosityOptions,
                    ),
                tasteOptions = state.tasteOptions.toOptions(),
                overallReviewOptions = state.overallReviewOptions.toOptions(),
                aromaUiData =
                    AromaUiData(
                        categories = state.aromaCategories,
                    ),
            )
        reviewEditFormContent(
            state = state,
            onAction = onAction,
            uiData = formUiData,
        )
        reviewEditFooterItems(
            state = state,
            onSave = onSave,
        )
    }
}

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
