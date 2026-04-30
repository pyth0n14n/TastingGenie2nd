package io.github.pyth0n14n.tastinggenie.feature.review.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import io.github.pyth0n14n.tastinggenie.ui.common.TastingTopAppBar

private val ScreenHorizontalPadding = 22.dp

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
            TastingTopAppBar(
                title =
                    if (state.sakeName.isBlank()) {
                        stringResource(R.string.screen_review_list)
                    } else {
                        state.sakeName
                    },
                onBack = onBack,
            )
        },
        floatingActionButton = {
            val addActionLabel = stringResource(R.string.action_add)
            FloatingActionButton(
                onClick = { state.sakeId?.let(onAddReview) },
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = addActionLabel,
                )
            }
        },
    ) { padding ->
        when {
            state.isLoading -> LoadingContent()
            state.loadError != null -> MessageContent(text = stringResource(state.loadError.messageResId))
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
                .padding(horizontal = ScreenHorizontalPadding),
    ) {
        ReviewStatsPanel(state = state)
        state.deleteError?.let { error ->
            Text(
                text = stringResource(error.messageResId),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        if (state.reviews.isEmpty()) {
            MessageContent(text = stringResource(R.string.message_no_reviews))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 6.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                items(items = state.reviews, key = { review -> review.id }) { review ->
                    ReviewTimelineItem(
                        review = review,
                        state = state,
                        actions = actions,
                        onDeleteRequest = { onDeleteRequest(review) },
                    )
                }
            }
        }
    }
}
