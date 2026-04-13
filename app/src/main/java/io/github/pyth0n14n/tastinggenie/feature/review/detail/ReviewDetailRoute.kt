package io.github.pyth0n14n.tastinggenie.feature.review.detail

import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.stopScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
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
import kotlin.math.absoluteValue

private const val REVIEW_DETAIL_PAGER_TAG = "review_detail_pager"
private const val PAGER_SETTLE_TOLERANCE = 0.001f

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
    val visibleSection = pagerState.visibleSection(content.selectedSection)
    LaunchedEffect(content.selectedSection) {
        if (
            pagerState.currentPage != content.selectedSection.ordinal ||
            pagerState.currentPageOffsetFraction.absoluteValue > PAGER_SETTLE_TOLERANCE
        ) {
            pagerState.takeOverAndMoveToPage(content.selectedSection.ordinal)
        }
    }
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
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
                        TextButton(
                            onClick = {
                                content.onEditReview(review.sakeId, review.id, visibleSection)
                            },
                        ) {
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
                        selectedSection = visibleSection,
                        onSectionSelected = { next ->
                            content.onSectionSelected(next)
                            coroutineScope.launch {
                                pagerState.takeOverAndMoveToPage(next.ordinal)
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
    val onEditReview: (Long, Long, ReviewSection) -> Unit,
    val selectedSection: ReviewSection,
    val onSectionSelected: (ReviewSection) -> Unit,
)

private suspend fun PagerState.takeOverAndMoveToPage(targetPage: Int) {
    stopScroll(MutatePriority.PreventUserInput)
    if (currentPageOffsetFraction.absoluteValue > PAGER_SETTLE_TOLERANCE) {
        scrollToPage(targetPage)
    } else {
        animateScrollToPage(targetPage)
    }
}

private fun PagerState.visibleSection(fallbackSection: ReviewSection): ReviewSection {
    val sections = ReviewSection.entries
    return when {
        targetPage in sections.indices && targetPage != settledPage -> sections[targetPage]
        currentPage in sections.indices -> sections[currentPage]
        else -> fallbackSection
    }
}
