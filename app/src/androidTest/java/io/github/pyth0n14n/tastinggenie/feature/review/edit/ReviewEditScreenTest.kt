package io.github.pyth0n14n.tastinggenie.feature.review.edit

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.UiError
import io.github.pyth0n14n.tastinggenie.feature.review.ReviewSection
import org.junit.Rule
import org.junit.Test

class ReviewEditScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun loadFailureWithEmptyRatingOptions_showsErrorWithoutCrashing() {
        composeRule.setContent {
            ReviewEditScreen(
                onBack = {},
                content =
                    ReviewEditScreenContent(
                        state =
                            ReviewEditUiState(
                                isLoading = false,
                                isEditTargetMissing = true,
                                error = UiError(messageResId = R.string.error_load_review),
                            ),
                        onAction = {},
                        onSave = {},
                        viscosityOptions = emptyList(),
                        volumeShortcutOptions = emptyList(),
                        selectedSection = ReviewSection.BASIC,
                        onSectionSelected = {},
                    ),
            )
        }

        composeRule.onNodeWithText("レビュー登録/編集").assertIsDisplayed()
        composeRule.onNodeWithText("* は必須項目です").assertIsDisplayed()
        composeRule.onNodeWithText("レビュー情報の読み込みに失敗しました").assertIsDisplayed()
        composeRule.onNodeWithText("保存").assertIsNotEnabled()
    }

    @Test
    fun saveButton_staysVisibleAfterSwipeToAnotherSection() {
        composeRule.setContent {
            var selectedSection by mutableStateOf(ReviewSection.BASIC)
            ReviewEditScreen(
                onBack = {},
                content =
                    ReviewEditScreenContent(
                        state = ReviewEditUiState(isLoading = false),
                        onAction = {},
                        onSave = {},
                        viscosityOptions = emptyList(),
                        volumeShortcutOptions = emptyList(),
                        selectedSection = selectedSection,
                        onSectionSelected = { next -> selectedSection = next },
                    ),
            )
        }

        composeRule.onNodeWithText("保存").assertIsDisplayed()
        composeRule.onNodeWithTag("review_edit_pager").performTouchInput { swipeLeft() }
        composeRule.onNodeWithText("色").assertIsDisplayed()
        composeRule.onNodeWithText("保存").assertIsDisplayed()
    }
}
