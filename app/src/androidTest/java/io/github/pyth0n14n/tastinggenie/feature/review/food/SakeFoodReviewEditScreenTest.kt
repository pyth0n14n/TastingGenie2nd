package io.github.pyth0n14n.tastinggenie.feature.review.food

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.UiError
import org.junit.Assert.assertEquals
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
    fun backWithUnsavedChanges_showsDiscardDialogAndConfirmsBeforeLeaving() {
        var backCalled = false
        composeRule.setContent {
            var state by mutableStateOf(SakeFoodReviewEditUiState(isLoading = false))
            SakeFoodReviewEditScreen(
                state = state,
                onBack = { backCalled = true },
                onDateSelected = {},
                onBarChanged = {},
                onDishChanged = { value -> state = state.copy(dish = value) },
                onCompatibilityChanged = {},
                onTemperatureChanged = {},
                onCommentChanged = {},
                onSave = {},
            )
        }

        composeRule.onNode(hasText("料理", substring = true) and hasSetTextAction()).performTextInput("焼き鳥")
        composeRule.onNodeWithContentDescription("戻る").performClick()
        composeRule.onNodeWithText("下書きを破棄する").assertIsDisplayed()
        composeRule.onNodeWithText("キャンセル").performClick()
        composeRule.onNodeWithText("下書きを破棄する").assertDoesNotExist()
        composeRule.runOnIdle { assertEquals(false, backCalled) }

        composeRule.onNodeWithContentDescription("戻る").performClick()
        composeRule.onNodeWithText("確定").performClick()
        composeRule.runOnIdle { assertTrue(backCalled) }
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
