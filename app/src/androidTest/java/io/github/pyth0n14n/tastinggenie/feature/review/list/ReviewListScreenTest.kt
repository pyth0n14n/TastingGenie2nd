package io.github.pyth0n14n.tastinggenie.feature.review.list

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.pyth0n14n.tastinggenie.domain.model.SakeFoodReview
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Aroma
import io.github.pyth0n14n.tastinggenie.domain.model.enums.FoodCompatibility
import io.github.pyth0n14n.tastinggenie.domain.model.enums.OverallReview
import io.github.pyth0n14n.tastinggenie.feature.review.TEST_REVIEW_ID
import io.github.pyth0n14n.tastinggenie.feature.review.testReview
import io.github.pyth0n14n.tastinggenie.navigation.AppDestination
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class ReviewListScreenTest {
    private companion object {
        const val TEST_FOOD_REVIEW_ID = 21L
    }

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
    fun foodReviewDeleteAction_confirmsBeforeDeleting() {
        var deletedReviewId: Long? = null
        composeRule.setContent {
            ReviewListScreen(
                state =
                    ReviewListUiState(
                        isLoading = false,
                        foodReviews = listOf(testFoodReview()),
                    ),
                initialTabName = AppDestination.REVIEW_LIST_TAB_FOOD,
                onBack = {},
                onAddReview = {},
                actions =
                    ReviewListActionHandlers(
                        onOpenReview = {},
                        onOpenSakeImage = {},
                        onDeleteReview = {},
                        onDeleteFoodReview = { reviewId -> deletedReviewId = reviewId },
                    ),
            )
        }

        composeRule.onNodeWithContentDescription("削除").performClick()
        composeRule.onNodeWithText("この料理相性レビューを削除しますか？")
        composeRule.onNodeWithText("確定").performClick()
        composeRule.runOnIdle { assertEquals(TEST_FOOD_REVIEW_ID, deletedReviewId) }
    }

    @Test
    fun foodReviewTab_addButtonCallsFoodReviewAction() {
        var addedFoodReviewSakeId: Long? = null
        composeRule.setContent {
            ReviewListScreen(
                state =
                    ReviewListUiState(
                        isLoading = false,
                        sakeId = testReview().sakeId,
                    ),
                initialTabName = AppDestination.REVIEW_LIST_TAB_FOOD,
                onBack = {},
                onAddReview = {},
                onAddFoodReview = { sakeId -> addedFoodReviewSakeId = sakeId },
                actions =
                    ReviewListActionHandlers(
                        onOpenReview = {},
                        onOpenSakeImage = {},
                        onDeleteReview = {},
                    ),
            )
        }

        composeRule.onNodeWithContentDescription("追加").performClick()
        composeRule.runOnIdle { assertEquals(testReview().sakeId, addedFoodReviewSakeId) }
    }

    @Test
    fun switchingTabs_changesAddButtonTarget() {
        var addedReviewSakeId: Long? = null
        var addedFoodReviewSakeId: Long? = null
        composeRule.setContent {
            ReviewListScreen(
                state =
                    ReviewListUiState(
                        isLoading = false,
                        sakeId = testReview().sakeId,
                    ),
                onBack = {},
                onAddReview = { sakeId -> addedReviewSakeId = sakeId },
                onAddFoodReview = { sakeId -> addedFoodReviewSakeId = sakeId },
                actions =
                    ReviewListActionHandlers(
                        onOpenReview = {},
                        onOpenSakeImage = {},
                        onDeleteReview = {},
                    ),
            )
        }

        composeRule.onNode(hasText("料理相性") and hasClickAction()).performClick()
        composeRule.onNodeWithText("料理相性レビューはまだありません\n料理と合わせた印象を記録できます")
        composeRule.onNodeWithContentDescription("追加").performClick()
        composeRule.runOnIdle {
            assertEquals(null, addedReviewSakeId)
            assertEquals(testReview().sakeId, addedFoodReviewSakeId)
        }

        composeRule.onNode(hasText("酒レビュー") and hasClickAction()).performClick()
        composeRule.onNodeWithText("まだレビューがありません")
        composeRule.onNodeWithText("この酒を飲んだら、右下の＋からレビューを記録できます。")
        composeRule.onNodeWithContentDescription("追加").performClick()
        composeRule.runOnIdle { assertEquals(testReview().sakeId, addedReviewSakeId) }
    }

    @Test
    fun emptyFabCoachmark_displaysOnlyOnSakeReviewTabAndDismisses() {
        var dismissed = false
        composeRule.setContent {
            ReviewListScreen(
                state =
                    ReviewListUiState(
                        isLoading = false,
                        sakeId = testReview().sakeId,
                        onboardingCompleted = true,
                        reviewEmptyFabCoachmarkSeen = false,
                    ),
                onBack = {},
                onAddReview = {},
                onAddFoodReview = {},
                actions =
                    ReviewListActionHandlers(
                        onOpenReview = {},
                        onOpenSakeImage = {},
                        onDeleteReview = {},
                        onDismissReviewEmptyFabCoachmark = { dismissed = true },
                    ),
            )
        }

        composeRule.onNodeWithText("ここからレビューを追加").assertExists()
        composeRule.onNodeWithText("色・香り・味を選んで記録できます。").assertExists()
        composeRule.onNode(hasText("料理相性") and hasClickAction()).performClick()
        composeRule.onNodeWithText("ここからレビューを追加").assertDoesNotExist()
        composeRule.onNode(hasText("酒レビュー") and hasClickAction()).performClick()
        composeRule.onNodeWithContentDescription("閉じる").performClick()
        composeRule.runOnIdle { assertEquals(true, dismissed) }
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
        composeRule.onNodeWithText("酒レビュー").assertExists()
        composeRule.onNodeWithText("2").assertExists()
        composeRule.onNodeWithText("料理相性").assertExists()
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

    private fun testFoodReview(): SakeFoodReview =
        SakeFoodReview(
            id = TEST_FOOD_REVIEW_ID,
            sakeId = testReview().sakeId,
            date = LocalDate.of(2026, 5, 17),
            dish = "焼き鳥",
            foodCompatibility = FoodCompatibility.GOOD,
        )
}
