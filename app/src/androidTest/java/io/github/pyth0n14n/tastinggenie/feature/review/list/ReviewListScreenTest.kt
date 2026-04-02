package io.github.pyth0n14n.tastinggenie.feature.review.list

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.pyth0n14n.tastinggenie.feature.review.TEST_REVIEW_ID
import io.github.pyth0n14n.tastinggenie.feature.review.testReview
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ReviewListScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun imageAction_opensViewerFromReviewList() {
        var openedReviewId: Long? = null
        composeRule.setContent {
            ReviewListScreen(
                state =
                    ReviewListUiState(
                        isLoading = false,
                        hasSakeImage = true,
                        reviews = listOf(testReview()),
                    ),
                onBack = {},
                onAddReview = {},
                actions =
                    ReviewListActionHandlers(
                        onOpenReview = {},
                        onOpenImage = { reviewId -> openedReviewId = reviewId },
                        onDeleteReview = {},
                    ),
            )
        }

        composeRule.onNodeWithText("画像").performClick()
        composeRule.runOnIdle { assertEquals(TEST_REVIEW_ID, openedReviewId) }
    }

    @Test
    fun imageAction_isHiddenWhenReviewHasNoImage() {
        composeRule.setContent {
            ReviewListScreen(
                state =
                    ReviewListUiState(
                        isLoading = false,
                        hasSakeImage = false,
                        reviews = listOf(testReview()),
                    ),
                onBack = {},
                onAddReview = {},
                actions =
                    ReviewListActionHandlers(
                        onOpenReview = {},
                        onOpenImage = {},
                        onDeleteReview = {},
                    ),
            )
        }

        composeRule.onNodeWithText("画像").assertDoesNotExist()
    }

    @Test
    fun deleteAction_confirmsBeforeDeleting() {
        var deletedReviewId: Long? = null
        composeRule.setContent {
            ReviewListScreen(
                state =
                    ReviewListUiState(
                        isLoading = false,
                        reviews = listOf(testReview()),
                    ),
                onBack = {},
                onAddReview = {},
                actions =
                    ReviewListActionHandlers(
                        onOpenReview = {},
                        onOpenImage = {},
                        onDeleteReview = { reviewId -> deletedReviewId = reviewId },
                    ),
            )
        }

        composeRule.onNodeWithContentDescription("レビューを削除").performClick()
        composeRule.onNodeWithText("このレビューを削除しますか？")
        composeRule.onNodeWithText("確定").performClick()
        composeRule.runOnIdle { assertEquals(TEST_REVIEW_ID, deletedReviewId) }
    }
}
