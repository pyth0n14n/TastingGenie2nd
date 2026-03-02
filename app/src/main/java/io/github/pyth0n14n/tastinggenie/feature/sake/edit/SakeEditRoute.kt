package io.github.pyth0n14n.tastinggenie.feature.sake.edit

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
import io.github.pyth0n14n.tastinggenie.domain.model.MasterOption
import io.github.pyth0n14n.tastinggenie.ui.common.DropdownOption
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
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            viewModel.consumeSaved()
            onBack()
        }
    }
    SakeEditScreen(
        state = state,
        onNameChanged = viewModel::onNameChanged,
        onGradeSelected = viewModel::onGradeSelected,
        onSave = viewModel::save,
        onBack = onBack,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SakeEditScreen(
    state: SakeEditUiState,
    onNameChanged: (String) -> Unit,
    onGradeSelected: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
) {
    if (state.isLoading) {
        LoadingContent()
        return
    }
    val gradeOptions = state.gradeOptions.toOptions()
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
            item {
                LabeledTextField(
                    label = stringResource(R.string.label_sake_name),
                    value = state.name,
                    onValueChange = onNameChanged,
                )
            }
            item {
                SimpleDropdown(
                    label = stringResource(R.string.label_grade),
                    selectedLabel = selectedGradeLabel(state.grade?.name, gradeOptions),
                    options = gradeOptions,
                    onSelected = onGradeSelected,
                )
            }
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
                SaveButton(
                    isSaving = state.isSaving,
                    onSave = onSave,
                )
            }
        }
    }
}

@Composable
private fun SaveButton(
    isSaving: Boolean,
    onSave: () -> Unit,
) {
    Button(
        onClick = onSave,
        modifier = Modifier.fillMaxWidth(),
        enabled = !isSaving,
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

private fun selectedGradeLabel(
    value: String?,
    options: List<DropdownOption>,
): String {
    val found = options.firstOrNull { option -> option.value == value }
    return found?.label.orEmpty()
}

private fun List<MasterOption>.toOptions(): List<DropdownOption> =
    map { option ->
        DropdownOption(
            value = option.value,
            label = option.label,
        )
    }
