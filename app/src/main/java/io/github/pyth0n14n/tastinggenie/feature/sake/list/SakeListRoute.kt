package io.github.pyth0n14n.tastinggenie.feature.sake.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.ui.common.ConfirmationDialog
import io.github.pyth0n14n.tastinggenie.ui.common.LoadingContent
import io.github.pyth0n14n.tastinggenie.ui.common.MessageContent

private const val LIST_SPACING = 12
private const val LIST_COLUMNS = 2

data class SakeListTopBarActions(
    val onOpenHelp: () -> Unit,
    val onOpenSettings: () -> Unit,
)

data class SakeListItemActions(
    val onOpenSake: (Long) -> Unit,
    val onEditSake: (Long) -> Unit,
    val onDeleteSake: (Long) -> Unit,
    val onTogglePinned: (Long, Boolean) -> Unit = { _, _ -> },
)

data class SakeListDeleteDialogActions(
    val onDismiss: () -> Unit,
    val onConfirm: () -> Unit,
)

/**
 * Route for sake list screen.
 *
 * Collects state from [SakeListViewModel] and delegates rendering to [SakeListScreen].
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SakeListRoute(
    onCreateSake: () -> Unit,
    onOpenSake: (Long) -> Unit,
    onEditSake: (Long) -> Unit,
    topBarActions: SakeListTopBarActions,
    viewModel: SakeListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SakeListScreen(
        state = uiState,
        onCreateSake = onCreateSake,
        itemActions =
            SakeListItemActions(
                onOpenSake = onOpenSake,
                onEditSake = onEditSake,
                onDeleteSake = viewModel::requestDeleteSake,
                onTogglePinned = viewModel::togglePinned,
            ),
        topBarActions = topBarActions,
        deleteDialogActions =
            SakeListDeleteDialogActions(
                onDismiss = viewModel::dismissDeleteSakeDialog,
                onConfirm = viewModel::confirmDeleteSake,
            ),
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SakeListScreen(
    state: SakeListUiState,
    onCreateSake: () -> Unit,
    itemActions: SakeListItemActions,
    topBarActions: SakeListTopBarActions,
    deleteDialogActions: SakeListDeleteDialogActions = SakeListDeleteDialogActions(onDismiss = {}, onConfirm = {}),
) {
    state.pendingDeleteSake?.let { pending ->
        ConfirmationDialog(
            title = stringResource(R.string.title_delete_sake),
            message =
                stringResource(
                    if (pending.hasImage) {
                        R.string.message_confirm_delete_sake_with_image
                    } else {
                        R.string.message_confirm_delete_sake_without_image
                    },
                    pending.reviewCount,
                ),
            onConfirm = deleteDialogActions.onConfirm,
            onDismiss = deleteDialogActions.onDismiss,
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.screen_sake_list)) },
                actions = {
                    if (state.showHelpHints) {
                        IconButton(onClick = topBarActions.onOpenHelp) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                                contentDescription = stringResource(R.string.screen_help),
                            )
                        }
                    }
                    IconButton(onClick = topBarActions.onOpenSettings) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.screen_settings),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateSake) {
                Text(text = stringResource(R.string.action_add))
            }
        },
    ) { padding ->
        when {
            state.isLoading -> LoadingContent()
            state.error != null -> MessageContent(text = stringResource(state.error.messageResId))
            else ->
                SakeListContent(
                    state = state,
                    itemActions = itemActions,
                    modifier = Modifier.padding(padding),
                )
        }
    }
}

@Composable
private fun SakeListContent(
    state: SakeListUiState,
    itemActions: SakeListItemActions,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .padding(LIST_SPACING.dp),
    ) {
        state.deleteError?.let { error ->
            Text(
                text = stringResource(error.messageResId),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth(),
            )
        }
        if (state.sakes.isEmpty()) {
            MessageContent(text = stringResource(R.string.message_no_sakes))
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(LIST_COLUMNS),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 24.dp, bottom = LIST_SPACING.dp),
                verticalArrangement = Arrangement.spacedBy(LIST_SPACING.dp),
                horizontalArrangement = Arrangement.spacedBy(LIST_SPACING.dp),
            ) {
                items(items = state.sakes, key = { item -> item.sake.id }) { item ->
                    SakeListCard(
                        sake = item.sake,
                        labels =
                            SakeListCardLabels(
                                grade = state.gradeLabels[item.sake.grade.name] ?: item.sake.grade.name,
                                latestOverallReview = item.latestOverallReview,
                                latestOverallReviewLabel =
                                    item.latestOverallReview
                                        ?.name
                                        ?.let { key -> state.overallReviewLabels[key] },
                            ),
                        showImagePreview = state.showImagePreview,
                        itemActions = itemActions,
                    )
                }
            }
        }
    }
}
