package io.github.pyth0n14n.tastinggenie.feature.review.edit

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
import io.github.pyth0n14n.tastinggenie.ui.common.DropdownOption
import io.github.pyth0n14n.tastinggenie.ui.common.LoadingContent

private const val SCREEN_PADDING = 16
private const val ITEM_SPACING = 12

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
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(SCREEN_PADDING.dp),
        verticalArrangement = Arrangement.spacedBy(ITEM_SPACING.dp),
    ) {
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
        item {
            if (state.error != null) {
                Text(
                    text = stringResource(state.error.messageResId),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
        item {
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSaving && !state.isInputLocked,
            ) {
                Text(
                    text =
                        if (state.isSaving) {
                            stringResource(R.string.message_saving)
                        } else {
                            stringResource(R.string.action_save)
                        },
                )
            }
        }
    }
}
