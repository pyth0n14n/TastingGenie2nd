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
    fun settingsContent_showsGroupedRows() {
        composeRule.setContent {
            SettingsScreen(
                state = SettingsUiState(isLoading = false),
                onBack = {},
                actions = emptySettingsActions(),
            )
        }

        composeRule.onNodeWithText("表示・操作").assertIsDisplayed()
        composeRule.onNodeWithText("ヘルプ表示").assertIsDisplayed()
        composeRule.onNodeWithText("酒リスト・レビュー").assertIsDisplayed()
        composeRule.onNodeWithText("健全度を表示しない").assertIsDisplayed()
        composeRule.onNodeWithText("見た目・香り・味の健全度はデフォルトで健全とする").assertIsDisplayed()
        composeRule.onNodeWithText("アプリで使用していない酒画像を削除します").assertIsDisplayed()
        composeRule.onNodeWithText("データ").assertIsDisplayed()
        composeRule.onNodeWithText("バックアップを書き出す").assertIsDisplayed()
        composeRule.onNodeWithText("バックアップから復元").assertIsDisplayed()
        composeRule.onNodeWithText("その他").assertIsDisplayed()
        composeRule.onNodeWithText("用語集（日本酒のきほん）").assertIsDisplayed()
        composeRule.onNodeWithText("このアプリについて").assertIsDisplayed()
    }

    @Test
    fun glossaryRow_callsCallback() {
        var glossaryClicked = false
        composeRule.setContent {
            SettingsScreen(
                state = SettingsUiState(isLoading = false),
                onBack = {},
                actions = emptySettingsActions(onOpenGlossary = { glossaryClicked = true }),
            )
        }

        composeRule.onNodeWithText("用語集（日本酒のきほん）").performClick()

        composeRule.runOnIdle {
            assertTrue(glossaryClicked)
        }
    }

    @Test
    fun backupExport_callsCallback() {
        var exportClicked = false
        composeRule.setContent {
            SettingsScreen(
                state = SettingsUiState(isLoading = false),
                onBack = {},
                actions = emptySettingsActions(onExportJson = { exportClicked = true }),
            )
        }

        composeRule.onNodeWithText("バックアップを書き出す").performClick()

        composeRule.runOnIdle { assertTrue(exportClicked) }
    }

    @Test
    fun backupImport_confirmsBeforeCallback() {
        var importClicked = false
        composeRule.setContent {
            SettingsScreen(
                state = SettingsUiState(isLoading = false),
                onBack = {},
                actions = emptySettingsActions(onImportJson = { importClicked = true }),
            )
        }

        composeRule.onNodeWithText("バックアップから復元").performClick()
        composeRule.onNodeWithText("バックアップから復元しますか？").assertIsDisplayed()
        composeRule.onNodeWithText("復元すると現在のデータが失われます。").assertIsDisplayed()
        composeRule.onNodeWithText("復元する").performClick()

        composeRule.runOnIdle { assertTrue(importClicked) }
    }

    @Test
    fun successMessage_canBeDismissed() {
        composeRule.setContent {
            SettingsScreen(
                state =
                    SettingsUiState(
                        isLoading = false,
                        messageResId = io.github.pyth0n14n.tastinggenie.R.string.message_import_success,
                    ),
                onBack = {},
                actions = emptySettingsActions(),
            )
        }

        composeRule.onNodeWithText("バックアップから復元しました").assertIsDisplayed()
    }

    @Test
    fun cleanupSuccessMessage_isShownInSnackbar() {
        composeRule.setContent {
            SettingsScreen(
                state =
                    SettingsUiState(
                        isLoading = false,
                        messageResId = io.github.pyth0n14n.tastinggenie.R.string.message_cleanup_unused_images_success,
                    ),
                onBack = {},
                actions = emptySettingsActions(),
            )
        }

        composeRule.onNodeWithText("未参照アプリ内画像を削除しました").assertIsDisplayed()
    }
}

private fun emptySettingsActions(
    onExportJson: () -> Unit = {},
    onImportJson: () -> Unit = {},
    onOpenGlossary: () -> Unit = {},
    onDismissMessage: () -> Unit = {},
) = SettingsScreenActions(
    onToggleHelpHints = {},
    onToggleReviewSoundness = {},
    onToggleAutoDeleteUnusedImages = {},
    onCleanupUnusedImages = {},
    onExportJson = onExportJson,
    onImportJson = onImportJson,
    onOpenGlossary = onOpenGlossary,
    onDismissMessage = onDismissMessage,
)
