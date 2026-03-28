package io.github.pyth0n14n.tastinggenie.feature.review.detail

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.ui.common.LoadingContent
import io.github.pyth0n14n.tastinggenie.ui.common.MessageContent

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ReviewDetailRoute(
    onBack: () -> Unit,
    onEditReview: (Long, Long) -> Unit,
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
        state = state,
        onBack = onBack,
        onEditReview = onEditReview,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ReviewDetailScreen(
    state: ReviewDetailUiState,
    onBack: () -> Unit,
    onEditReview: (Long, Long) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.screen_review_detail)) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(stringResource(R.string.action_back))
                    }
                },
                actions = {
                    val review = state.review
                    if (review != null) {
                        TextButton(onClick = { onEditReview(review.sakeId, review.id) }) {
                            Text(stringResource(R.string.action_edit))
                        }
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.isLoading -> LoadingContent()
            state.error != null -> MessageContent(text = stringResource(state.error.messageResId))
            state.review != null ->
                ReviewDetailContent(
                    review = state.review,
                    sakeName = state.sakeName,
                    labels = state.toLabels(),
                    modifier = Modifier.padding(padding),
                )
        }
    }
}
