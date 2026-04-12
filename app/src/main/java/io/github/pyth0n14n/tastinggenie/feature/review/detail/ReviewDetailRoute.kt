package io.github.pyth0n14n.tastinggenie.feature.review.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.feature.review.ReviewSection
import io.github.pyth0n14n.tastinggenie.feature.review.ReviewSectionTabs
import io.github.pyth0n14n.tastinggenie.ui.common.LoadingContent
import io.github.pyth0n14n.tastinggenie.ui.common.MessageContent
import kotlinx.coroutines.launch

private const val REVIEW_DETAIL_PAGER_TAG = "review_detail_pager"

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
    var selectedSectionName by rememberSaveable { mutableStateOf(ReviewSection.BASIC.name) }
    val selectedSection = ReviewSection.valueOf(selectedSectionName)
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
                selectedSection = selectedSection,
                onSectionSelected = { next -> selectedSectionName = next.name },
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
    val pagerState = rememberPagerState(initialPage = content.selectedSection.ordinal) { ReviewSection.entries.size }
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(content.selectedSection) {
        if (pagerState.currentPage != content.selectedSection.ordinal) {
            pagerState.animateScrollToPage(content.selectedSection.ordinal)
        }
    }
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            val section = ReviewSection.entries[page]
            if (section != content.selectedSection) {
                content.onSectionSelected(section)
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (content.state.sakeName.isBlank()) {
                            stringResource(R.string.screen_review_detail)
                        } else {
                            "${stringResource(R.string.label_sake)}: ${content.state.sakeName}"
                        },
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(stringResource(R.string.action_back))
                    }
                },
                actions = {
                    val review = content.state.review
                    if (review != null) {
                        TextButton(onClick = { content.onEditReview(review.sakeId, review.id) }) {
                            Text(stringResource(R.string.action_edit))
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
                Column(modifier = Modifier.padding(padding)) {
                    ReviewSectionTabs(
                        selectedSection = content.selectedSection,
                        onSectionSelected = { next ->
                            content.onSectionSelected(next)
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(next.ordinal)
                            }
                        },
                    )
                    HorizontalPager(
                        state = pagerState,
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .testTag(REVIEW_DETAIL_PAGER_TAG),
                    ) { page ->
                        ReviewDetailContent(
                            content =
                                ReviewDetailContentState(
                                    review = content.state.review,
                                    sakeName = content.state.sakeName,
                                    labels = content.state.toLabels(),
                                    selectedSection = ReviewSection.entries[page],
                                ),
                        )
                    }
                }
        }
    }
}

data class ReviewDetailScreenContent(
    val state: ReviewDetailUiState,
    val onEditReview: (Long, Long) -> Unit,
    val selectedSection: ReviewSection,
    val onSectionSelected: (ReviewSection) -> Unit,
)
