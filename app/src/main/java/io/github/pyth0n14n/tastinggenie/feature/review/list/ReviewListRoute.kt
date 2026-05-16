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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import io.github.pyth0n14n.tastinggenie.domain.model.SakeFoodReview
import io.github.pyth0n14n.tastinggenie.ui.common.ConfirmationDialog
import io.github.pyth0n14n.tastinggenie.ui.common.LoadingContent
import io.github.pyth0n14n.tastinggenie.ui.common.MessageContent
import io.github.pyth0n14n.tastinggenie.ui.common.TastingMediumFab
import io.github.pyth0n14n.tastinggenie.ui.common.TastingTopAppBar

private val ScreenHorizontalPadding = 22.dp

data class ReviewListActionHandlers(
    val onOpenReview: (Long) -> Unit,
    val onOpenFoodReview: (Long, Long) -> Unit = { _, _ -> },
    val onOpenSakeImage: (Long) -> Unit,
    val onDeleteReview: (Long) -> Unit,
    val onDeleteFoodReview: (Long) -> Unit = {},
)

private enum class ReviewListTab {
    SAKE_REVIEW,
    FOOD_REVIEW,
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongParameterList")
fun ReviewListRoute(
    onBack: () -> Unit,
    onAddReview: (Long) -> Unit,
    onAddFoodReview: (Long) -> Unit,
    onOpenReview: (Long) -> Unit,
    onOpenFoodReview: (Long, Long) -> Unit,
    onOpenSakeImage: (Long) -> Unit,
    viewModel: ReviewListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ReviewListScreen(
        state = state,
        onBack = onBack,
        onAddReview = onAddReview,
        onAddFoodReview = onAddFoodReview,
        actions =
            ReviewListActionHandlers(
                onOpenReview = onOpenReview,
                onOpenFoodReview = onOpenFoodReview,
                onOpenSakeImage = onOpenSakeImage,
                onDeleteReview = viewModel::deleteReview,
                onDeleteFoodReview = viewModel::deleteFoodReview,
            ),
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongMethod")
fun ReviewListScreen(
    state: ReviewListUiState,
    onBack: () -> Unit,
    onAddReview: (Long) -> Unit,
    onAddFoodReview: (Long) -> Unit = {},
    actions: ReviewListActionHandlers,
) {
    var pendingDeleteReview by remember(state.reviews) { mutableStateOf<Review?>(null) }
    var pendingDeleteFoodReview by remember(state.foodReviews) { mutableStateOf<SakeFoodReview?>(null) }
    var selectedTab by remember { mutableStateOf(ReviewListTab.SAKE_REVIEW) }

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
    pendingDeleteFoodReview?.let { review ->
        ConfirmationDialog(
            title = stringResource(R.string.title_delete_food_review),
            message = stringResource(R.string.message_confirm_delete_food_review),
            onConfirm = {
                actions.onDeleteFoodReview(review.id)
                pendingDeleteFoodReview = null
            },
            onDismiss = { pendingDeleteFoodReview = null },
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
            TastingMediumFab(
                icon = Icons.Filled.Add,
                contentDescription = addActionLabel,
                onClick = {
                    state.sakeId?.let { sakeId ->
                        when (selectedTab) {
                            ReviewListTab.SAKE_REVIEW -> onAddReview(sakeId)
                            ReviewListTab.FOOD_REVIEW -> onAddFoodReview(sakeId)
                        }
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.isLoading -> LoadingContent()
            state.loadError != null -> MessageContent(text = stringResource(state.loadError.messageResId))
            else ->
                ReviewListContent(
                    state = state,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    actions = actions,
                    onDeleteRequest = { review -> pendingDeleteReview = review },
                    onDeleteFoodReviewRequest = { review -> pendingDeleteFoodReview = review },
                    modifier = Modifier.padding(padding),
                )
        }
    }
}

@Composable
@Suppress("LongParameterList", "LongMethod")
private fun ReviewListContent(
    state: ReviewListUiState,
    selectedTab: ReviewListTab,
    onTabSelected: (ReviewListTab) -> Unit,
    actions: ReviewListActionHandlers,
    onDeleteRequest: (Review) -> Unit,
    onDeleteFoodReviewRequest: (SakeFoodReview) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = ScreenHorizontalPadding),
    ) {
        ReviewStatsPanel(
            state = state,
            onOpenImage = { sakeId -> actions.onOpenSakeImage(sakeId) },
        )
        state.deleteError?.let { error ->
            Text(
                text = stringResource(error.messageResId),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        TabRow(selectedTabIndex = selectedTab.ordinal) {
            Tab(
                selected = selectedTab == ReviewListTab.SAKE_REVIEW,
                onClick = { onTabSelected(ReviewListTab.SAKE_REVIEW) },
                text = { Text(text = stringResource(R.string.tab_sake_reviews)) },
            )
            Tab(
                selected = selectedTab == ReviewListTab.FOOD_REVIEW,
                onClick = { onTabSelected(ReviewListTab.FOOD_REVIEW) },
                text = { Text(text = stringResource(R.string.tab_food_reviews)) },
            )
        }
        when (selectedTab) {
            ReviewListTab.SAKE_REVIEW -> {
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

            ReviewListTab.FOOD_REVIEW -> {
                if (state.foodReviews.isEmpty()) {
                    MessageContent(text = stringResource(R.string.message_no_food_reviews))
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 6.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                    ) {
                        items(items = state.foodReviews, key = { review -> review.id }) { review ->
                            FoodReviewTimelineItem(
                                review = review,
                                state = state,
                                actions = actions,
                                onDeleteRequest = { onDeleteFoodReviewRequest(review) },
                            )
                        }
                    }
                }
            }
        }
    }
}
