package io.github.pyth0n14n.tastinggenie.feature.settings

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun exportAndImportButtons_callCallbacks() {
        var exportClicked = false
        var importClicked = false
        composeRule.setContent {
            SettingsScreen(
                state = SettingsUiState(isLoading = false),
                onBack = {},
                actions =
                    SettingsScreenActions(
                        onToggleHelpHints = {},
                        onToggleImagePreview = {},
                        onToggleReviewSoundness = {},
                        onExportJson = { exportClicked = true },
                        onImportJson = { importClicked = true },
                        onDismissMessage = {},
                    ),
            )
        }

        composeRule.onNodeWithText("JSONを書き出す").performClick()
        composeRule.onNodeWithText("JSONを読み込む").performClick()

        composeRule.runOnIdle {
            assertTrue(exportClicked)
            assertTrue(importClicked)
        }
    }

    @Test
    fun successMessage_canBeDismissed() {
        var dismissCalled = false
        composeRule.setContent {
            SettingsScreen(
                state =
                    SettingsUiState(
                        isLoading = false,
                        messageResId = io.github.pyth0n14n.tastinggenie.R.string.message_import_success,
                    ),
                onBack = {},
                actions =
                    SettingsScreenActions(
                        onToggleHelpHints = {},
                        onToggleImagePreview = {},
                        onToggleReviewSoundness = {},
                        onExportJson = {},
                        onImportJson = {},
                        onDismissMessage = { dismissCalled = true },
                    ),
            )
        }

        composeRule.onNodeWithText("JSONを読み込みました").assertIsDisplayed()
        composeRule.onNodeWithText("閉じる").performClick()
        composeRule.runOnIdle { assertTrue(dismissCalled) }
    }
}
