package io.github.pyth0n14n.tastinggenie.feature.review.detail

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.feature.review.ReviewSection
import io.github.pyth0n14n.tastinggenie.feature.review.testReview
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ReviewDetailScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun editAction_opensEditor() {
        var openedReview: Triple<Long, Long, ReviewSection>? = null
        composeRule.setContent {
            ReviewDetailScreen(
                onBack = {},
                content =
                    ReviewDetailScreenContent(
                        state =
                            ReviewDetailUiState(
                                isLoading = false,
                                review = testReview(),
                            ),
                        onEditReview = { sakeId, reviewId, section ->
                            openedReview = Triple(sakeId, reviewId, section)
                        },
                        selectedSection = ReviewSection.TASTE,
                        onSectionSelected = {},
                    ),
            )
        }

        composeRule.onNodeWithText("編集").performClick()
        composeRule.runOnIdle {
            assertEquals(
                Triple(testReview().sakeId, testReview().id, ReviewSection.TASTE),
                openedReview,
            )
        }
    }

    @Test
    fun editAction_usesTappedSectionBeforePagerSettles() {
        val tasteLabel =
            composeRule.activity.getString(
                R.string.label_review_section_taste,
            )
        var selectedSection by mutableStateOf(ReviewSection.BASIC)
        var openedReview: Triple<Long, Long, ReviewSection>? = null
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            ReviewDetailScreen(
                onBack = {},
                content =
                    ReviewDetailScreenContent(
                        state =
                            ReviewDetailUiState(
                                isLoading = false,
                                review = testReview(),
                            ),
                        onEditReview = { sakeId, reviewId, section ->
                            openedReview = Triple(sakeId, reviewId, section)
                        },
                        selectedSection = selectedSection,
                        onSectionSelected = { next -> selectedSection = next },
                    ),
            )
        }

        composeRule.onNodeWithContentDescription(tasteLabel).performClick()
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.onNodeWithText("編集").performClick()

        composeRule.runOnIdle {
            assertEquals(
                Triple(testReview().sakeId, testReview().id, ReviewSection.TASTE),
                openedReview,
            )
        }
        composeRule.mainClock.autoAdvance = true
    }

    @Test
    fun imageAction_isNotShownOnDetailScreen() {
        composeRule.setContent {
            ReviewDetailScreen(
                onBack = {},
                content =
                    ReviewDetailScreenContent(
                        state =
                            ReviewDetailUiState(
                                isLoading = false,
                                review = testReview(),
                            ),
                        onEditReview = { _, _, _ -> },
                        selectedSection = ReviewSection.BASIC,
                        onSectionSelected = {},
                    ),
            )
        }

        assertEquals(0, composeRule.onAllNodesWithText("画像").fetchSemanticsNodes().size)
    }

    @Test
    fun swipeChangesVisibleDetailSection() {
        composeRule.setContent {
            var selectedSection by mutableStateOf(ReviewSection.BASIC)
            ReviewDetailScreen(
                onBack = {},
                content =
                    ReviewDetailScreenContent(
                        state =
                            ReviewDetailUiState(
                                isLoading = false,
                                review = testReview(),
                            ),
                        onEditReview = { _, _, _ -> },
                        selectedSection = selectedSection,
                        onSectionSelected = { next -> selectedSection = next },
                    ),
            )
        }

        composeRule.onNodeWithText("日付: 2026-03-14").assertIsDisplayed()
        composeRule.onNodeWithTag("review_detail_pager").performTouchInput { swipeLeft() }
        composeRule.onNodeWithText("色: CLEAR").assertIsDisplayed()
    }
}
