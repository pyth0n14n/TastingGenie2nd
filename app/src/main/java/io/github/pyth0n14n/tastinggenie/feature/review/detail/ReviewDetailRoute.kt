package io.github.pyth0n14n.tastinggenie.feature.review.detail

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.feature.review.ReviewSection
import io.github.pyth0n14n.tastinggenie.ui.common.LoadingContent
import io.github.pyth0n14n.tastinggenie.ui.common.MessageContent
import io.github.pyth0n14n.tastinggenie.ui.common.TastingTopAppBar

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ReviewDetailRoute(
    onBack: () -> Unit,
    onEditReview: (Long, Long, ReviewSection) -> Unit,
    refreshRequested: Boolean = false,
    onRefreshConsumed: () -> Unit = {},
    viewModel: ReviewDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(refreshRequested) {
        if (refreshRequested) {
            viewModel.refresh()
            onRefreshConsumed()
        }
    }
    ReviewDetailScreen(
        onBack = onBack,
        content =
            ReviewDetailScreenContent(
                state = state,
                onEditReview = onEditReview,
            ),
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongMethod")
fun ReviewDetailScreen(
    onBack: () -> Unit,
    content: ReviewDetailScreenContent,
) {
    Scaffold(
        topBar = {
            TastingTopAppBar(
                title =
                    if (content.state.sakeName.isBlank()) {
                        stringResource(R.string.screen_review_detail)
                    } else {
                        content.state.sakeName
                    },
                onBack = onBack,
                actions = {
                    val review = content.state.review
                    if (review != null) {
                        TextButton(
                            onClick = {
                                content.onEditReview(review.sakeId, review.id, ReviewSection.BASIC)
                            },
                            colors =
                                ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                        ) {
                            Text(text = stringResource(R.string.action_edit))
                        }
                    }
                },
            )
        },
    ) { padding ->
        when {
            content.state.isLoading -> LoadingContent()
            content.state.error != null -> MessageContent(text = stringResource(content.state.error.messageResId))
            content.state.review != null ->
                ReviewDetailContent(
                    content =
                        ReviewDetailContentState(
                            review = content.state.review,
                            sakeName = content.state.sakeName,
                            labels = content.state.toLabels(),
                            showReviewSoundness = content.state.showReviewSoundness,
                        ),
                    modifier = Modifier.padding(padding),
                )
        }
    }
}

data class ReviewDetailScreenContent(
    val state: ReviewDetailUiState,
    val onEditReview: (Long, Long, ReviewSection) -> Unit,
)
