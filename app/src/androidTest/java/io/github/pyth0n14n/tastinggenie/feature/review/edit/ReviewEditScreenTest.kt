package io.github.pyth0n14n.tastinggenie.feature.review.edit

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.UiError
import org.junit.Rule
import org.junit.Test

class ReviewEditScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun loadFailureWithEmptyRatingOptions_showsErrorWithoutCrashing() {
        composeRule.setContent {
            ReviewEditScreen(
                state =
                    ReviewEditUiState(
                        isLoading = false,
                        isEditTargetMissing = true,
                        error = UiError(messageResId = R.string.error_load_review),
                    ),
                onAction = {},
                onSave = {},
                onBack = {},
            )
        }

        composeRule.onNodeWithText("レビュー登録/編集").assertIsDisplayed()
        composeRule.onNodeWithText("レビュー情報の読み込みに失敗しました").assertIsDisplayed()
        composeRule.onNodeWithText("保存").assertIsNotEnabled()
    }
}
