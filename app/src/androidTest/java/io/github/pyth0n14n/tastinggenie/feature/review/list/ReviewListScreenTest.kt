package io.github.pyth0n14n.tastinggenie.feature.review.list

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Aroma
import io.github.pyth0n14n.tastinggenie.domain.model.enums.OverallReview
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
        var openedSakeId: Long? = null
        composeRule.setContent {
            ReviewListScreen(
                state =
                    ReviewListUiState(
                        isLoading = false,
                        sakeId = testReview().sakeId,
                        hasSakeImage = true,
                        reviews = listOf(testReview()),
                    ),
                onBack = {},
                onAddReview = {},
                actions =
                    ReviewListActionHandlers(
                        onOpenReview = {},
                        onOpenSakeImage = { sakeId -> openedSakeId = sakeId },
                        onDeleteReview = {},
                    ),
            )
        }

        composeRule.onNodeWithContentDescription("画像").performClick()
        composeRule.runOnIdle { assertEquals(testReview().sakeId, openedSakeId) }
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
                        onOpenSakeImage = {},
                        onDeleteReview = {},
                    ),
            )
        }

        composeRule.onNodeWithContentDescription("画像").assertDoesNotExist()
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
                        onOpenSakeImage = {},
                        onDeleteReview = { reviewId -> deletedReviewId = reviewId },
                    ),
            )
        }

        composeRule.onNodeWithContentDescription("削除").performClick()
        composeRule.onNodeWithText("このレビューを削除しますか？")
        composeRule.onNodeWithText("確定").performClick()
        composeRule.runOnIdle { assertEquals(TEST_REVIEW_ID, deletedReviewId) }
    }

    @Test
    fun reviewStats_showCountAndAverageRating() {
        composeRule.setContent {
            ReviewListScreen(
                state =
                    ReviewListUiState(
                        isLoading = false,
                        reviews =
                            listOf(
                                testReview(otherOverallReview = OverallReview.GOOD),
                                testReview(id = 12L, otherOverallReview = OverallReview.VERY_GOOD),
                            ),
                        foodReviews =
                            listOf(
                                io.github.pyth0n14n.tastinggenie.domain.model.SakeFoodReview(
                                    id = 21L,
                                    sakeId = testReview().sakeId,
                                    date = java.time.LocalDate.of(2026, 5, 17),
                                    bar = null,
                                    dish = "焼き鳥",
                                    foodCompatibility = null,
                                    temperature = null,
                                    freeComment = null,
                                ),
                            ),
                    ),
                onBack = {},
                onAddReview = {},
                actions =
                    ReviewListActionHandlers(
                        onOpenReview = {},
                        onOpenSakeImage = {},
                        onDeleteReview = {},
                    ),
            )
        }

        composeRule.onNodeWithText("平均評価").assertExists()
        composeRule.onNodeWithText("4.50").assertExists()
        composeRule.onNodeWithText("レビュー数").assertExists()
        composeRule.onNodeWithText("2").assertExists()
        composeRule.onNodeWithText("料理レビュー数").assertExists()
        composeRule.onNodeWithText("1").assertExists()
    }

    @Test
    fun reviewItem_showsTemperatureGuideAndAromaChips() {
        composeRule.setContent {
            ReviewListScreen(
                state =
                    ReviewListUiState(
                        isLoading = false,
                        reviews = listOf(testReview(tasteInPalateAroma = listOf(Aroma.PEAR))),
                        temperatureLabels = mapOf("JOON" to "常温"),
                        aromaLabels = mapOf("MELON" to "メロン", "PEAR" to "洋梨"),
                    ),
                onBack = {},
                onAddReview = {},
                actions =
                    ReviewListActionHandlers(
                        onOpenReview = {},
                        onOpenSakeImage = {},
                        onDeleteReview = {},
                    ),
            )
        }

        composeRule.onNodeWithText("常温（20℃）").assertExists()
        composeRule.onNodeWithText("メロン").assertExists()
        composeRule.onNodeWithText("洋梨").assertExists()
    }

    @Test
    fun reviewItem_showsIndividualityOnly() {
        composeRule.setContent {
            ReviewListScreen(
                state =
                    ReviewListUiState(
                        isLoading = false,
                        reviews =
                            listOf(
                                testReview(
                                    otherCautions = "留意点",
                                    otherIndividuality = "個性コメント",
                                    otherFreeComment = "自由コメント",
                                ),
                            ),
                    ),
                onBack = {},
                onAddReview = {},
                actions =
                    ReviewListActionHandlers(
                        onOpenReview = {},
                        onOpenSakeImage = {},
                        onDeleteReview = {},
                    ),
            )
        }

        composeRule.onNodeWithText("個性コメント").assertExists()
        composeRule.onNodeWithText("自由コメント").assertDoesNotExist()
        composeRule.onNodeWithText("留意点").assertDoesNotExist()
    }
}
