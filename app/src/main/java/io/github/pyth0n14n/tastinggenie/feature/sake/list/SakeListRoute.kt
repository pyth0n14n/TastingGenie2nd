package io.github.pyth0n14n.tastinggenie.feature.sake.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.Sake
import io.github.pyth0n14n.tastinggenie.ui.common.LoadingContent
import io.github.pyth0n14n.tastinggenie.ui.common.MessageContent

private const val LIST_SPACING = 8

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
    viewModel: SakeListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SakeListScreen(
        state = uiState,
        onCreateSake = onCreateSake,
        onOpenSake = onOpenSake,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SakeListScreen(
    state: SakeListUiState,
    onCreateSake: () -> Unit,
    onOpenSake: (Long) -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.screen_sake_list)) }) },
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
                    items = state.sakes,
                    gradeLabels = state.gradeLabels,
                    onOpenSake = onOpenSake,
                    modifier = Modifier.padding(padding),
                )
        }
    }
}

@Composable
private fun SakeList(
    items: List<Sake>,
    gradeLabels: Map<String, String>,
    onOpenSake: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(LIST_SPACING.dp),
        verticalArrangement = Arrangement.spacedBy(LIST_SPACING.dp),
    ) {
        items(items = items, key = { sake -> sake.id }) { sake ->
            ListItem(
                headlineContent = { Text(sake.name) },
                supportingContent = {
                    Text(
                        text = gradeLabels[sake.grade.name] ?: sake.grade.name,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable { onOpenSake(sake.id) },
            )
        }
    }
}
