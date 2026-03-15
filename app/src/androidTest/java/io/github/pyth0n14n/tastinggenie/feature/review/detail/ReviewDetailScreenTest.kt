package io.github.pyth0n14n.tastinggenie.feature.review.detail

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.pyth0n14n.tastinggenie.feature.review.TEST_REVIEW_ID
import io.github.pyth0n14n.tastinggenie.feature.review.testReview
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ReviewDetailScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun imageAction_opensViewerWhenImageExists() {
        var openedReviewId: Long? = null
        composeRule.setContent {
            ReviewDetailScreen(
                state =
                    ReviewDetailUiState(
                        isLoading = false,
                        review = testReview().copy(imageUri = "content://review/image/1"),
                    ),
                onBack = {},
                onEditReview = { _, _ -> },
                onOpenImage = { reviewId -> openedReviewId = reviewId },
            )
        }

        composeRule.onNodeWithText("画像").performClick()
        composeRule.runOnIdle { assertEquals(TEST_REVIEW_ID, openedReviewId) }
    }

    @Test
    fun imageAction_isHiddenWhenImageDoesNotExist() {
        composeRule.setContent {
            ReviewDetailScreen(
                state =
                    ReviewDetailUiState(
                        isLoading = false,
                        review = testReview(),
                    ),
                onBack = {},
                onEditReview = { _, _ -> },
                onOpenImage = { error("should not be called") },
            )
        }

        composeRule.onNodeWithText("画像").assertDoesNotExist()
    }
}
