package io.github.pyth0n14n.tastinggenie.feature.sake.list

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.pyth0n14n.tastinggenie.domain.model.Sake
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeGrade
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SakeListScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun onCreateButtonClick_callsOnCreateSake() {
        var called = false
        composeRule.setContent {
            SakeListScreen(
                state =
                    SakeListUiState(
                        isLoading = false,
                        sakes = emptyList(),
                    ),
                onCreateSake = { called = true },
                itemActions =
                    SakeListItemActions(
                        onOpenSake = {},
                        onEditSake = {},
                    ),
                topBarActions =
                    SakeListTopBarActions(
                        onOpenHelp = {},
                        onOpenSettings = {},
                    ),
            )
        }

        composeRule.onNodeWithText("追加").performClick()
        composeRule.runOnIdle { assertTrue(called) }
    }

    @Test
    fun onSakeItemClick_callsOnOpenSake() {
        var openedId: Long? = null
        composeRule.setContent {
            SakeListScreen(
                state =
                    SakeListUiState(
                        isLoading = false,
                        sakes =
                            listOf(
                                Sake(
                                    id = 42L,
                                    name = "吟醸酒",
                                    grade = SakeGrade.GINJO,
                                ),
                            ),
                        gradeLabels = mapOf(SakeGrade.GINJO.name to "吟醸"),
                    ),
                onCreateSake = {},
                itemActions =
                    SakeListItemActions(
                        onOpenSake = { openedId = it },
                        onEditSake = {},
                    ),
                topBarActions =
                    SakeListTopBarActions(
                        onOpenHelp = {},
                        onOpenSettings = {},
                    ),
            )
        }

        composeRule.onNodeWithText("吟醸").assertExists()
        composeRule.onNodeWithText("吟醸酒").performClick()
        composeRule.runOnIdle { assertEquals(42L, openedId) }
    }

    @Test
    fun topBarActions_openHelpAndSettings() {
        var helpOpened = false
        var settingsOpened = false
        composeRule.setContent {
            SakeListScreen(
                state = SakeListUiState(isLoading = false),
                onCreateSake = {},
                itemActions =
                    SakeListItemActions(
                        onOpenSake = {},
                        onEditSake = {},
                    ),
                topBarActions =
                    SakeListTopBarActions(
                        onOpenHelp = { helpOpened = true },
                        onOpenSettings = { settingsOpened = true },
                    ),
            )
        }

        composeRule.onNodeWithText("ヘルプ").performClick()
        composeRule.onNodeWithText("設定").performClick()
        composeRule.runOnIdle {
            assertTrue(helpOpened)
            assertTrue(settingsOpened)
        }
    }

    @Test
    fun showImagePreview_displaysPlaceholderWhenImageMissing() {
        composeRule.setContent {
            SakeListScreen(
                state =
                    SakeListUiState(
                        isLoading = false,
                        sakes =
                            listOf(
                                Sake(
                                    id = 7L,
                                    name = "夏酒",
                                    grade = SakeGrade.JUNMAI,
                                    imageUri = null,
                                ),
                            ),
                        gradeLabels = mapOf(SakeGrade.JUNMAI.name to "純米"),
                        showImagePreview = true,
                    ),
                onCreateSake = {},
                itemActions =
                    SakeListItemActions(
                        onOpenSake = {},
                        onEditSake = {},
                    ),
                topBarActions =
                    SakeListTopBarActions(
                        onOpenHelp = {},
                        onOpenSettings = {},
                    ),
            )
        }

        composeRule.onNodeWithText("画像が登録されていません").assertExists()
    }

    @Test
    fun showImagePreview_falseHidesCardImageArea() {
        composeRule.setContent {
            SakeListScreen(
                state =
                    SakeListUiState(
                        isLoading = false,
                        sakes =
                            listOf(
                                Sake(
                                    id = 8L,
                                    name = "秋酒",
                                    grade = SakeGrade.GINJO,
                                    imageUri = "file:///images/sakes/autumn.jpg",
                                ),
                            ),
                        gradeLabels = mapOf(SakeGrade.GINJO.name to "吟醸"),
                        showImagePreview = false,
                    ),
                onCreateSake = {},
                itemActions =
                    SakeListItemActions(
                        onOpenSake = {},
                        onEditSake = {},
                    ),
                topBarActions =
                    SakeListTopBarActions(
                        onOpenHelp = {},
                        onOpenSettings = {},
                    ),
            )
        }

        composeRule.onNodeWithContentDescription("酒画像").assertDoesNotExist()
        composeRule.onNodeWithText("画像が登録されていません").assertDoesNotExist()
    }
}
