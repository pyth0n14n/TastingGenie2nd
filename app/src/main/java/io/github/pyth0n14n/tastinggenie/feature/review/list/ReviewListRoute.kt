package io.github.pyth0n14n.tastinggenie.feature.review.list

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
import io.github.pyth0n14n.tastinggenie.domain.model.Review
import io.github.pyth0n14n.tastinggenie.ui.common.LoadingContent
import io.github.pyth0n14n.tastinggenie.ui.common.MessageContent

private const val LIST_SPACING = 8

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
        onOpenReview = onOpenReview,
        onOpenImage = onOpenImage,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ReviewListScreen(
    state: ReviewListUiState,
    onBack: () -> Unit,
    onAddReview: (Long) -> Unit,
    onOpenReview: (Long) -> Unit,
    onOpenImage: (Long) -> Unit,
) {
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
            state.error != null -> MessageContent(text = stringResource(state.error.messageResId))
            state.reviews.isEmpty() -> MessageContent(text = stringResource(R.string.message_no_reviews))
            else ->
                ReviewList(
                    reviews = state.reviews,
                    overallReviewLabels = state.overallReviewLabels,
                    callbacks =
                        ReviewListCallbacks(
                            onOpenReview = onOpenReview,
                            onOpenImage = onOpenImage,
                        ),
                    hasSakeImage = state.hasSakeImage,
                    modifier = Modifier.padding(padding),
                )
        }
    }
}

@Composable
private fun ReviewList(
    reviews: List<Review>,
    overallReviewLabels: Map<String, String>,
    callbacks: ReviewListCallbacks,
    hasSakeImage: Boolean,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(LIST_SPACING.dp),
        verticalArrangement = Arrangement.spacedBy(LIST_SPACING.dp),
    ) {
        items(items = reviews, key = { review -> review.id }) { review ->
            ListItem(
                headlineContent = { Text(review.date.toString()) },
                supportingContent = {
                    val text =
                        review.review
                            ?.name
                            ?.let { overallReviewLabels[it] }
                            .orEmpty()
                    if (text.isNotBlank()) {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                },
                trailingContent = {
                    if (hasSakeImage) {
                        TextButton(onClick = { callbacks.onOpenImage(review.id) }) {
                            Text(stringResource(R.string.action_view_image))
                        }
                    }
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable { callbacks.onOpenReview(review.id) },
            )
        }
    }
}

private data class ReviewListCallbacks(
    val onOpenReview: (Long) -> Unit,
    val onOpenImage: (Long) -> Unit,
)
