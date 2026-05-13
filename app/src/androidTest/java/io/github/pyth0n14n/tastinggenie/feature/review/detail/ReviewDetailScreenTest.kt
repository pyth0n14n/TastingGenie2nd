package io.github.pyth0n14n.tastinggenie.feature.review.detail

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.pyth0n14n.tastinggenie.domain.model.Review
import io.github.pyth0n14n.tastinggenie.domain.model.enums.FlavorProfileType
import io.github.pyth0n14n.tastinggenie.domain.model.enums.FoodCompatibility
import io.github.pyth0n14n.tastinggenie.feature.review.ReviewSection
import io.github.pyth0n14n.tastinggenie.feature.review.TEST_REVIEW_ID
import io.github.pyth0n14n.tastinggenie.feature.review.TEST_SAKE_ID
import io.github.pyth0n14n.tastinggenie.feature.review.testReview
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ReviewDetailScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun topBar_usesBackIconInsteadOfBackText() {
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
                    ),
            )
        }

        composeRule.onNodeWithContentDescription("戻る").assertIsDisplayed()
        composeRule.onNodeWithText("戻る").assertDoesNotExist()
    }

    @Test
    fun editAction_opensEditorAtBasicSection() {
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
                    ),
            )
        }

        composeRule.onNodeWithText("編集").performClick()
        composeRule.runOnIdle {
            assertEquals(
                Triple(testReview().sakeId, testReview().id, ReviewSection.BASIC),
                openedReview,
            )
        }
    }

    @Test
    fun summary_showsImportantReviewValues() {
        composeRule.setContent {
            ReviewDetailScreen(
                onBack = {},
                content =
                    ReviewDetailScreenContent(
                        state =
                            ReviewDetailUiState(
                                isLoading = false,
                                review =
                                    testReview().copy(
                                        volume = 720,
                                        foodCompatibility = FoodCompatibility.SLIGHTLY_GOOD,
                                        otherFreeComment = "すっきりした立ち上がりだが、後半に旨味が伸びる。",
                                        otherSakeTypes = listOf(FlavorProfileType.SOUSHU),
                                    ),
                                temperatureLabels = mapOf("JOON" to "常温"),
                                overallReviewLabels = mapOf("GOOD" to "やや良い"),
                            ),
                        onEditReview = { _, _, _ -> },
                    ),
            )
        }

        composeRule.onNodeWithText("2026-03-14").assertIsDisplayed()
        composeRule.onNodeWithText("やや良い").assertIsDisplayed()
        composeRule.onNodeWithText("常温").assertIsDisplayed()
        composeRule.onNodeWithText("720ml").assertIsDisplayed()
        composeRule.onNodeWithText("料理との相性: やや良い").assertIsDisplayed()
        composeRule.onNodeWithText("すっきりした立ち上がりだが、後半に旨味が伸びる。").assertIsDisplayed()
        composeRule.onNodeWithText("日本酒4タイプ: 爽酒").assertIsDisplayed()
    }

    @Test
    fun aromaAndTasteSections_areExpandedInitially() {
        composeRule.setContent {
            ReviewDetailScreen(
                onBack = {},
                content =
                    ReviewDetailScreenContent(
                        state =
                            ReviewDetailUiState(
                                isLoading = false,
                                review = testReview(),
                                intensityLabels = mapOf("WEAK" to "やや弱い"),
                                tasteLabels = mapOf("STRONG" to "やや強い"),
                            ),
                        onEditReview = { _, _, _ -> },
                    ),
            )
        }

        composeRule.onNodeWithText("香り").assertIsDisplayed()
        composeRule.onNodeWithText("強さ").assertIsDisplayed()
        composeRule.onNodeWithText("やや弱い").assertIsDisplayed()
        composeRule.onNodeWithText("味").assertIsDisplayed()
        composeRule.onNodeWithText("甘味").assertIsDisplayed()
        composeRule.onNodeWithText("やや強い").assertIsDisplayed()
    }

    @Test
    fun collapsedSections_expandWhenTapped() {
        composeRule.setContent {
            ReviewDetailScreen(
                onBack = {},
                content =
                    ReviewDetailScreenContent(
                        state =
                            ReviewDetailUiState(
                                isLoading = false,
                                review = testReview().copy(appearanceColor = null),
                                temperatureLabels = mapOf("JOON" to "常温"),
                            ),
                        onEditReview = { _, _, _ -> },
                    ),
            )
        }

        composeRule.onNodeWithText("日付").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("基本情報").performClick()
        composeRule.onNodeWithText("日付").assertIsDisplayed()
        composeRule.onNodeWithText("2026-03-14").assertIsDisplayed()
    }

    @Test
    fun emptySections_areHidden() {
        composeRule.setContent {
            ReviewDetailScreen(
                onBack = {},
                content =
                    ReviewDetailScreenContent(
                        state =
                            ReviewDetailUiState(
                                isLoading = false,
                                review =
                                    Review(
                                        id = TEST_REVIEW_ID,
                                        sakeId = TEST_SAKE_ID,
                                        date = java.time.LocalDate.parse("2026-03-14"),
                                    ),
                            ),
                        onEditReview = { _, _, _ -> },
                    ),
            )
        }

        composeRule.onNodeWithText("香り").assertDoesNotExist()
        composeRule.onNodeWithText("味").assertDoesNotExist()
        composeRule.onNodeWithText("見た目").assertDoesNotExist()
        composeRule.onNodeWithText("メモ・評価").assertDoesNotExist()
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
                    ),
            )
        }

        assertEquals(0, composeRule.onAllNodesWithText("画像").fetchSemanticsNodes().size)
    }
}
