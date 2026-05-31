package io.github.pyth0n14n.tastinggenie.feature.sake.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.ui.common.ConfirmationDialog
import io.github.pyth0n14n.tastinggenie.ui.common.FabCoachMark
import io.github.pyth0n14n.tastinggenie.ui.common.LoadingContent
import io.github.pyth0n14n.tastinggenie.ui.common.MessageContent
import io.github.pyth0n14n.tastinggenie.ui.common.TastingMediumFab
import io.github.pyth0n14n.tastinggenie.ui.common.TastingTopAppBar

private const val LIST_SPACING = 8
private const val SCREEN_HORIZONTAL_PADDING = 16

data class SakeListTopBarActions(
    val onOpenSettings: () -> Unit,
)

data class SakeListRouteActions(
    val onCreateSake: () -> Unit,
    val onOpenSake: (Long) -> Unit,
    val onEditSake: (Long) -> Unit,
    val onOpenSakeImage: (Long) -> Unit,
    val topBarActions: SakeListTopBarActions,
)

data class SakeListItemActions(
    val onOpenSake: (Long) -> Unit,
    val onEditSake: (Long) -> Unit,
    val onOpenSakeImage: (Long) -> Unit,
    val onDeleteSake: (Long) -> Unit,
    val onTogglePinned: (Long, Boolean) -> Unit = { _, _ -> },
)

data class SakeListDeleteDialogActions(
    val onDismiss: () -> Unit,
    val onConfirm: () -> Unit,
)

data class SakeListScreenActions(
    val onCreateSake: () -> Unit,
    val itemActions: SakeListItemActions,
    val topBarActions: SakeListTopBarActions,
    val onSearchQueryChanged: (String) -> Unit = {},
    val onSortModeSelected: (SakeListSortMode) -> Unit = {},
    val onDismissEmptyFabCoachmark: () -> Unit = {},
)

/**
 * Route for sake list screen.
 *
 * Collects state from [SakeListViewModel] and delegates rendering to [SakeListScreen].
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SakeListRoute(
    actions: SakeListRouteActions,
    viewModel: SakeListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SakeListScreen(
        state = uiState,
        actions =
            SakeListScreenActions(
                onCreateSake = {
                    viewModel.markEmptyFabCoachmarkSeen()
                    actions.onCreateSake()
                },
                itemActions =
                    SakeListItemActions(
                        onOpenSake = actions.onOpenSake,
                        onEditSake = actions.onEditSake,
                        onOpenSakeImage = actions.onOpenSakeImage,
                        onDeleteSake = viewModel::requestDeleteSake,
                        onTogglePinned = viewModel::togglePinned,
                    ),
                topBarActions = actions.topBarActions,
                onSearchQueryChanged = viewModel::updateSearchQuery,
                onSortModeSelected = viewModel::selectSortMode,
                onDismissEmptyFabCoachmark = viewModel::markEmptyFabCoachmarkSeen,
            ),
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
    actions: SakeListScreenActions,
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

    Box(modifier = Modifier.fillMaxSize()) {
        SakeListScaffold(state = state, actions = actions)
        if (state.shouldShowEmptyFabCoachmark) {
            FabCoachMark(
                title = stringResource(R.string.sake_empty_fab_coachmark_title),
                message = stringResource(R.string.sake_empty_fab_coachmark_message),
                onDismiss = actions.onDismissEmptyFabCoachmark,
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .navigationBarsPadding()
                        .padding(end = 24.dp, bottom = 104.dp),
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SakeListScaffold(
    state: SakeListUiState,
    actions: SakeListScreenActions,
) {
    Scaffold(
        topBar = {
            TastingTopAppBar(
                title = stringResource(R.string.screen_sake_list_title),
                actions = {
                    SakeListSortMenu(
                        selectedSortMode = state.sortMode,
                        onSortModeSelected = actions.onSortModeSelected,
                    )
                    SakeListTopOverflowMenu(actions = actions.topBarActions)
                },
            )
        },
        floatingActionButton = {
            val addActionLabel = stringResource(R.string.action_add)
            TastingMediumFab(
                icon = Icons.Filled.Add,
                contentDescription = addActionLabel,
                onClick = actions.onCreateSake,
            )
        },
    ) { padding ->
        when {
            state.isLoading -> LoadingContent()
            state.error != null -> MessageContent(text = stringResource(state.error.messageResId))
            else ->
                SakeListContent(
                    state = state,
                    actions = actions,
                    modifier = Modifier.padding(padding),
                )
        }
    }
}

@Composable
private fun SakeListContent(
    state: SakeListUiState,
    actions: SakeListScreenActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = SCREEN_HORIZONTAL_PADDING.dp),
        verticalArrangement = Arrangement.spacedBy(LIST_SPACING.dp),
    ) {
        SakeSearchField(
            query = state.searchQuery,
            onQueryChanged = actions.onSearchQueryChanged,
            modifier = Modifier.padding(top = LIST_SPACING.dp),
        )
        state.deleteError?.let { error ->
            Text(
                text = stringResource(error.messageResId),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        SakeListBody(state = state, itemActions = actions.itemActions)
    }
}

@Composable
private fun SakeListBody(
    state: SakeListUiState,
    itemActions: SakeListItemActions,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            state.sakes.isEmpty() ->
                MessageContent(
                    title = stringResource(R.string.title_no_sakes),
                    text = stringResource(R.string.message_no_sakes_empty),
                )
            state.displayedSakes.isEmpty() -> MessageContent(text = stringResource(R.string.message_no_sakes_found))
            else -> SakeListItems(state = state, itemActions = itemActions)
        }
    }
}

@Composable
private fun SakeListItems(
    state: SakeListUiState,
    itemActions: SakeListItemActions,
) {
    var isPinnedSectionExpanded by rememberSaveable { mutableStateOf(true) }
    var isOtherSectionExpanded by rememberSaveable { mutableStateOf(true) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = LIST_SPACING.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        val pinnedSakes = state.displayedSakes.filter { item -> item.sake.isPinned }
        val otherSakes = state.displayedSakes.filterNot { item -> item.sake.isPinned }
        sakeListSection(
            section =
                SakeListSectionState(
                    keyPrefix = "pinned",
                    titleRes = R.string.section_pinned_sakes,
                    items = pinnedSakes,
                    expanded = isPinnedSectionExpanded,
                    onToggleExpanded = { isPinnedSectionExpanded = !isPinnedSectionExpanded },
                ),
            state = state,
            itemActions = itemActions,
        )
        sakeListSection(
            section =
                SakeListSectionState(
                    keyPrefix = "other",
                    titleRes = R.string.section_other_sakes,
                    items = otherSakes,
                    expanded = isOtherSectionExpanded,
                    onToggleExpanded = { isOtherSectionExpanded = !isOtherSectionExpanded },
                ),
            state = state,
            itemActions = itemActions,
        )
    }
}

@Composable
private fun SakeSearchField(
    query: String,
    onQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier =
            modifier
                .fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        colors =
            TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                disabledIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
            ),
        placeholder = { Text(text = stringResource(R.string.hint_sake_search)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
            )
        },
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(onClick = { onQueryChanged("") }) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.action_clear),
                    )
                }
            }
        },
    )
}

@Composable
private fun SakeListSortMenu(
    selectedSortMode: SakeListSortMode,
    onSortModeSelected: (SakeListSortMode) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = { expanded = true }) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.Sort,
            contentDescription = stringResource(R.string.action_sort_sakes),
        )
    }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
    ) {
        SakeListSortMode.entries.forEach { mode ->
            DropdownMenuItem(
                text = { Text(text = stringResource(mode.labelRes())) },
                onClick = {
                    expanded = false
                    onSortModeSelected(mode)
                },
                enabled = mode != selectedSortMode,
            )
        }
    }
}

@Composable
private fun SakeListTopOverflowMenu(actions: SakeListTopBarActions) {
    IconButton(onClick = actions.onOpenSettings) {
        Icon(
            imageVector = Icons.Outlined.Settings,
            contentDescription = stringResource(R.string.screen_settings),
        )
    }
}

private fun SakeListSortMode.labelRes(): Int =
    when (this) {
        SakeListSortMode.DEFAULT -> R.string.sort_sakes_default
        SakeListSortMode.NAME_ASC -> R.string.sort_sakes_name
        SakeListSortMode.RATING_DESC -> R.string.sort_sakes_rating
    }
