package io.github.pyth0n14n.tastinggenie.feature.review.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.Review
import io.github.pyth0n14n.tastinggenie.ui.common.ConfirmationDialog
import io.github.pyth0n14n.tastinggenie.ui.common.LoadingContent
import io.github.pyth0n14n.tastinggenie.ui.common.MessageContent

private const val LIST_SPACING = 8

data class ReviewListActionHandlers(
    val onOpenReview: (Long) -> Unit,
    val onOpenImage: (Long) -> Unit,
    val onDeleteReview: (Long) -> Unit,
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ReviewListRoute(
    onBack: () -> Unit,
    onAddReview: (Long) -> Unit,
    onOpenReview: (Long) -> Unit,
    onOpenImage: (Long) -> Unit,
    viewModel: ReviewListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ReviewListScreen(
        state = state,
        onBack = onBack,
        onAddReview = onAddReview,
        actions =
            ReviewListActionHandlers(
                onOpenReview = onOpenReview,
                onOpenImage = onOpenImage,
                onDeleteReview = viewModel::deleteReview,
            ),
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ReviewListScreen(
    state: ReviewListUiState,
    onBack: () -> Unit,
    onAddReview: (Long) -> Unit,
    actions: ReviewListActionHandlers,
) {
    var pendingDeleteReview by remember(state.reviews) { mutableStateOf<Review?>(null) }

    pendingDeleteReview?.let { review ->
        ConfirmationDialog(
            title = stringResource(R.string.title_delete_review),
            message = stringResource(R.string.message_confirm_delete_review),
            onConfirm = {
                actions.onDeleteReview(review.id)
                pendingDeleteReview = null
            },
            onDismiss = { pendingDeleteReview = null },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.sakeName.isBlank()) {
                            stringResource(R.string.screen_review_list)
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { state.sakeId?.let(onAddReview) },
            ) {
                Text(text = stringResource(R.string.action_add))
            }
        },
    ) { padding ->
        when {
            state.isLoading -> LoadingContent()
            state.loadError != null -> MessageContent(text = stringResource(state.loadError.messageResId))
            state.reviews.isEmpty() -> MessageContent(text = stringResource(R.string.message_no_reviews))
            else ->
                ReviewListContent(
                    state = state,
                    actions = actions,
                    onDeleteRequest = { review -> pendingDeleteReview = review },
                    modifier = Modifier.padding(padding),
                )
        }
    }
}

@Composable
private fun ReviewListContent(
    state: ReviewListUiState,
    actions: ReviewListActionHandlers,
    onDeleteRequest: (Review) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = LIST_SPACING.dp),
        verticalArrangement = Arrangement.spacedBy(LIST_SPACING.dp),
    ) {
        state.deleteError?.let { error ->
            Text(
                text = stringResource(error.messageResId),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = LIST_SPACING.dp),
            verticalArrangement = Arrangement.spacedBy(LIST_SPACING.dp),
        ) {
            items(items = state.reviews, key = { review -> review.id }) { review ->
                ListItem(
                    headlineContent = { Text(review.date.toString()) },
                    supportingContent = {
                        val text =
                            review.otherOverallReview
                                ?.name
                                ?.let { state.overallReviewLabels[it] }
                                .orEmpty()
                        if (text.isNotBlank()) {
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    },
                    trailingContent = {
                        ReviewListItemActions(
                            hasSakeImage = state.hasSakeImage,
                            onOpenImage = { actions.onOpenImage(review.id) },
                            onDeleteRequest = { onDeleteRequest(review) },
                        )
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable { actions.onOpenReview(review.id) },
                )
            }
        }
    }
}

@Composable
private fun ReviewListItemActions(
    hasSakeImage: Boolean,
    onOpenImage: () -> Unit,
    onDeleteRequest: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        if (hasSakeImage) {
            TextButton(onClick = onOpenImage) {
                Text(stringResource(R.string.action_view_image))
            }
        }
        IconButton(onClick = onDeleteRequest) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = stringResource(R.string.content_delete_review),
            )
        }
    }
}
