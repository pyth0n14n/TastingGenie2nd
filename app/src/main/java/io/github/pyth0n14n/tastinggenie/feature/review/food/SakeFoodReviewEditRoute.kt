package io.github.pyth0n14n.tastinggenie.feature.review.food

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.feature.review.edit.ReviewEditChoiceField
import io.github.pyth0n14n.tastinggenie.feature.review.edit.ReviewEditChoiceFieldUi
import io.github.pyth0n14n.tastinggenie.feature.review.edit.TemperaturePickerField
import io.github.pyth0n14n.tastinggenie.feature.review.edit.foodCompatibilityOptions
import io.github.pyth0n14n.tastinggenie.feature.review.edit.toDatePickerInitialMillisOrNull
import io.github.pyth0n14n.tastinggenie.feature.review.edit.toOptions
import io.github.pyth0n14n.tastinggenie.ui.common.DatePickerField
import io.github.pyth0n14n.tastinggenie.ui.common.DiscardDraftDialog
import io.github.pyth0n14n.tastinggenie.ui.common.FormFieldState
import io.github.pyth0n14n.tastinggenie.ui.common.LabeledTextField
import io.github.pyth0n14n.tastinggenie.ui.common.LoadingContent
import io.github.pyth0n14n.tastinggenie.ui.common.RequiredFieldHint
import io.github.pyth0n14n.tastinggenie.ui.common.TastingTopAppBar

private val ScreenPadding = 16.dp
private val ItemSpacing = 12.dp

@Composable
fun SakeFoodReviewEditRoute(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: SakeFoodReviewEditViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            viewModel.consumeSaved()
            onSaved()
        }
    }
    SakeFoodReviewEditScreen(
        state = state,
        onBack = onBack,
        onDateSelected = viewModel::onDateSelected,
        onBarChanged = viewModel::onBarChanged,
        onDishChanged = viewModel::onDishChanged,
        onCompatibilityChanged = viewModel::onCompatibilityChanged,
        onTemperatureChanged = viewModel::onTemperatureChanged,
        onCommentChanged = viewModel::onCommentChanged,
        onSave = viewModel::save,
    )
}

@Composable
@Suppress("LongParameterList")
fun SakeFoodReviewEditScreen(
    state: SakeFoodReviewEditUiState,
    onBack: () -> Unit,
    onDateSelected: (Long) -> Unit,
    onBarChanged: (String) -> Unit,
    onDishChanged: (String) -> Unit,
    onCompatibilityChanged: (String?) -> Unit,
    onTemperatureChanged: (String?) -> Unit,
    onCommentChanged: (String) -> Unit,
    onSave: () -> Unit,
) {
    if (state.isLoading) {
        LoadingContent()
        return
    }
    var isDiscardDraftDialogVisible by rememberSaveable { mutableStateOf(false) }
    val initialDraft = remember(state.reviewId, state.sakeId, state.isEditTargetMissing) { state.toDraftSnapshot() }
    val hasUnsavedChanges = state.toDraftSnapshot() != initialDraft

    fun requestBack() {
        if (hasUnsavedChanges) {
            isDiscardDraftDialogVisible = true
        } else {
            onBack()
        }
    }
    BackHandler(enabled = hasUnsavedChanges) {
        isDiscardDraftDialogVisible = true
    }
    if (isDiscardDraftDialogVisible) {
        DiscardDraftDialog(
            onConfirm = {
                isDiscardDraftDialogVisible = false
                onBack()
            },
            onDismiss = { isDiscardDraftDialogVisible = false },
        )
    }

    Scaffold(
        topBar = {
            TastingTopAppBar(
                title = state.sakeName.ifBlank { stringResource(R.string.screen_food_review_edit) },
                onBack = ::requestBack,
            )
        },
        bottomBar = {
            FoodReviewEditBottomBar(
                state = state,
                onSave = onSave,
            )
        },
    ) { padding ->
        SakeFoodReviewEditForm(
            state = state,
            onDateSelected = onDateSelected,
            onBarChanged = onBarChanged,
            onDishChanged = onDishChanged,
            onCompatibilityChanged = onCompatibilityChanged,
            onTemperatureChanged = onTemperatureChanged,
            onCommentChanged = onCommentChanged,
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
@Suppress("LongParameterList", "LongMethod")
private fun SakeFoodReviewEditForm(
    state: SakeFoodReviewEditUiState,
    onDateSelected: (Long) -> Unit,
    onBarChanged: (String) -> Unit,
    onDishChanged: (String) -> Unit,
    onCompatibilityChanged: (String?) -> Unit,
    onTemperatureChanged: (String?) -> Unit,
    onCommentChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier =
            modifier
                .fillMaxSize(),
        contentPadding = PaddingValues(ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(ItemSpacing),
    ) {
        item { RequiredFieldHint() }
        item {
            DatePickerField(
                label = stringResource(R.string.label_review_date),
                value = state.date,
                onDateSelected = onDateSelected,
                initialSelectedDateMillis = state.date.toDatePickerInitialMillisOrNull(),
                fieldState = FormFieldState(required = true),
            )
        }
        item {
            TemperaturePickerField(
                label = stringResource(R.string.label_sake_temperature),
                options = state.temperatureOptions.toOptions(),
                selectedValue = state.temperature?.name,
                onValueChanged = onTemperatureChanged,
            )
        }
        item {
            LabeledTextField(
                label = stringResource(R.string.label_dish),
                value = state.dish,
                onValueChange = onDishChanged,
                fieldState = FormFieldState(required = true),
            )
        }
        item {
            ReviewEditChoiceField(
                ui =
                    ReviewEditChoiceFieldUi(
                        label = stringResource(R.string.label_scene_required),
                        options = foodCompatibilityOptions(),
                        selectedValue = state.foodCompatibility?.name,
                        showHelpHints = false,
                    ),
                onValueChanged = onCompatibilityChanged,
            )
        }
        item {
            LabeledTextField(
                label = stringResource(R.string.label_bar),
                value = state.bar,
                onValueChange = onBarChanged,
            )
        }
        item {
            LabeledTextField(
                label = stringResource(R.string.label_comment),
                value = state.freeComment,
                onValueChange = onCommentChanged,
                singleLine = false,
            )
        }
    }
}

@Composable
private fun FoodReviewEditBottomBar(
    state: SakeFoodReviewEditUiState,
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
                    .padding(horizontal = ScreenPadding, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            state.error?.let { error ->
                Text(
                    text = stringResource(error.messageResId),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
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

private fun SakeFoodReviewEditUiState.toDraftSnapshot(): List<Any?> =
    listOf(date, bar, dish, foodCompatibility, temperature, freeComment)
