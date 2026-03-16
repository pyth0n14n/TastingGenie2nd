package io.github.pyth0n14n.tastinggenie.feature.review.detail

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.pyth0n14n.tastinggenie.feature.review.testReview
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ReviewDetailScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun editAction_opensEditor() {
        var openedReview: Pair<Long, Long>? = null
        composeRule.setContent {
            ReviewDetailScreen(
                state =
                    ReviewDetailUiState(
                        isLoading = false,
                        review = testReview(),
                    ),
                onBack = {},
                onEditReview = { sakeId, reviewId -> openedReview = sakeId to reviewId },
            )
        }

        composeRule.onNodeWithText("編集").performClick()
        composeRule.runOnIdle { assertEquals(testReview().sakeId to testReview().id, openedReview) }
    }

    @Test
    fun imageAction_isNotShownOnDetailScreen() {
        composeRule.setContent {
            ReviewDetailScreen(
                state =
                    ReviewDetailUiState(
                        isLoading = false,
                        review = testReview(),
                    ),
                onBack = {},
                onEditReview = { _, _ -> },
            )
        }

        composeRule.onNodeWithText("画像").assertDoesNotExist()
    }
}
