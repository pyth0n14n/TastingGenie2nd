package io.github.pyth0n14n.tastinggenie.feature.sake.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.pyth0n14n.tastinggenie.R
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
            ),
        topBarActions = topBarActions,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SakeListScreen(
    state: SakeListUiState,
    onCreateSake: () -> Unit,
    itemActions: SakeListItemActions,
    topBarActions: SakeListTopBarActions,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.screen_sake_list)) },
                actions = {
                    TextButton(onClick = topBarActions.onOpenHelp) {
                        Text(stringResource(R.string.screen_help))
                    }
                    TextButton(onClick = topBarActions.onOpenSettings) {
                        Text(stringResource(R.string.screen_settings))
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
            state.sakes.isEmpty() -> MessageContent(text = stringResource(R.string.message_no_sakes))
            else ->
                SakeList(
                    state = state,
                    itemActions = itemActions,
                    modifier = Modifier.padding(padding),
                )
        }
    }
}

@Composable
private fun SakeList(
    state: SakeListUiState,
    itemActions: SakeListItemActions,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(LIST_COLUMNS),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(LIST_SPACING.dp),
        verticalArrangement = Arrangement.spacedBy(LIST_SPACING.dp),
        horizontalArrangement = Arrangement.spacedBy(LIST_SPACING.dp),
    ) {
        items(items = state.sakes, key = { sake -> sake.id }) { sake ->
            SakeListCard(
                sake = sake,
                gradeLabel = state.gradeLabels[sake.grade.name] ?: sake.grade.name,
                showImagePreview = state.showImagePreview,
                itemActions = itemActions,
            )
        }
    }
}
