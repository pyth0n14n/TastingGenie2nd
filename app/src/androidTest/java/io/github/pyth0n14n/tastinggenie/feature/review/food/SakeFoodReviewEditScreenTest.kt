package io.github.pyth0n14n.tastinggenie.feature.review.food

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.UiError
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SakeFoodReviewEditScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun requiredFields_areDisplayed() {
        composeRule.setContent {
            SakeFoodReviewEditScreen(
                state = SakeFoodReviewEditUiState(isLoading = false, sakeName = "テスト銘柄"),
                onBack = {},
                onDateSelected = {},
                onBarChanged = {},
                onDishChanged = {},
                onCompatibilityChanged = {},
                onTemperatureChanged = {},
                onCommentChanged = {},
                onSave = {},
            )
        }

        composeRule.onNodeWithText("* は必須項目です").assertIsDisplayed()
        composeRule.onNodeWithText("日付").assertIsDisplayed()
        composeRule.onNodeWithText("料理").assertIsDisplayed()
        composeRule.onNodeWithText("料理との相性 *").assertIsDisplayed()
    }

    @Test
    fun saveButton_callsOnSave() {
        var saveCalled = false
        composeRule.setContent {
            SakeFoodReviewEditScreen(
                state = SakeFoodReviewEditUiState(isLoading = false),
                onBack = {},
                onDateSelected = {},
                onBarChanged = {},
                onDishChanged = {},
                onCompatibilityChanged = {},
                onTemperatureChanged = {},
                onCommentChanged = {},
                onSave = { saveCalled = true },
            )
        }

        composeRule.onNodeWithText("保存").performClick()
        composeRule.runOnIdle { assertTrue(saveCalled) }
    }

    @Test
    fun validationError_isDisplayed() {
        composeRule.setContent {
            SakeFoodReviewEditScreen(
                state =
                    SakeFoodReviewEditUiState(
                        isLoading = false,
                        error = UiError(R.string.error_invalid_food_review_input),
                    ),
                onBack = {},
                onDateSelected = {},
                onBarChanged = {},
                onDishChanged = {},
                onCompatibilityChanged = {},
                onTemperatureChanged = {},
                onCommentChanged = {},
                onSave = {},
            )
        }

        composeRule.onNodeWithText("日付・料理・料理との相性を確認してください").assertIsDisplayed()
    }
}
