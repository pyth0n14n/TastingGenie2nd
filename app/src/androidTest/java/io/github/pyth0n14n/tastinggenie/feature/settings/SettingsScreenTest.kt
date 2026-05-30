package io.github.pyth0n14n.tastinggenie.feature.settings

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.pyth0n14n.tastinggenie.ui.theme.TastingGenie2ndAndroidTheme
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
        composeRule.onNodeWithText("通常は選択式が多く、利酒師は記述式が多くなります").assertIsDisplayed()
        composeRule.onNodeWithText("デバッグ").assertIsDisplayed()
        composeRule.onNodeWithText("健全度を表示しない").assertIsDisplayed()
        composeRule.onNodeWithText("見た目・香り・味の健全度はデフォルトで健全とする").assertIsDisplayed()
        composeRule.onNodeWithText("データ").assertIsDisplayed()
        composeRule.onNodeWithText("バックアップを書き出す").assertIsDisplayed()
        composeRule.onNodeWithText("バックアップから復元").assertIsDisplayed()
        composeRule.onNodeWithText("その他").assertIsDisplayed()
        composeRule.onNodeWithText("アプリの使い方").assertIsDisplayed()
        composeRule.onNodeWithText("このアプリについて").assertIsDisplayed()
    }

    @Test
    fun settingsContent_rendersInLightAndDarkTheme() {
        var darkTheme by mutableStateOf(false)
        composeRule.setContent {
            TastingGenie2ndAndroidTheme(darkTheme = darkTheme) {
                SettingsScreen(
                    state = SettingsUiState(isLoading = false),
                    onBack = {},
                    actions = emptySettingsActions(),
                )
            }
        }
        composeRule.onNodeWithText("設定").assertIsDisplayed()
        composeRule.onNodeWithText("バックアップを書き出す").assertIsDisplayed()

        composeRule.runOnIdle { darkTheme = true }
        composeRule.onNodeWithText("設定").assertIsDisplayed()
        composeRule.onNodeWithText("バックアップを書き出す").assertIsDisplayed()
    }

    @Test
    fun appGuideRow_callsCallback() {
        var appGuideClicked = false
        composeRule.setContent {
            SettingsScreen(
                state = SettingsUiState(isLoading = false),
                onBack = {},
                actions = emptySettingsActions(onOpenAppGuide = { appGuideClicked = true }),
            )
        }

        composeRule.onNodeWithText("アプリの使い方").performClick()

        composeRule.runOnIdle {
            assertTrue(appGuideClicked)
        }
    }

    @Test
    fun backupExport_callsCallback() {
        var exportClicked = false
        composeRule.setContent {
            SettingsScreen(
                state = SettingsUiState(isLoading = false),
                onBack = {},
                actions = emptySettingsActions(onExportBackup = { exportClicked = true }),
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
                actions = emptySettingsActions(onRestoreBackup = { importClicked = true }),
            )
        }

        composeRule.onNodeWithText("バックアップから復元").performClick()
        composeRule.onNodeWithText("バックアップから復元しますか？").assertIsDisplayed()
        composeRule.onNodeWithText("復元すると現在のデータはバックアップ内容で上書きされます。").assertIsDisplayed()
        composeRule.onNodeWithText("復元する").performClick()

        composeRule.runOnIdle { assertTrue(importClicked) }
    }

    @Test
    fun aboutApp_showsUnderageDrinkingNotice() {
        composeRule.setContent {
            SettingsScreen(
                state = SettingsUiState(isLoading = false),
                onBack = {},
                actions = emptySettingsActions(),
            )
        }

        composeRule.onNodeWithText("このアプリについて").performClick()

        composeRule
            .onNodeWithText("ききさけ帖は、20歳以上の方が日本酒の記録を残すためのアプリです。20歳未満の飲酒は法律で禁止されています。")
            .assertIsDisplayed()
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
}

private fun emptySettingsActions(
    onExportBackup: () -> Unit = {},
    onRestoreBackup: () -> Unit = {},
    onOpenAppGuide: () -> Unit = {},
    onDismissMessage: () -> Unit = {},
) = SettingsScreenActions(
    onToggleHelpHints = {},
    onToggleReviewSoundness = {},
    onSelectReviewMode = {},
    onExportBackup = onExportBackup,
    onRestoreBackup = onRestoreBackup,
    onOpenAppGuide = onOpenAppGuide,
    onDismissMessage = onDismissMessage,
)
