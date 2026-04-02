package io.github.pyth0n14n.tastinggenie.feature.sake.list

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.Sake
import io.github.pyth0n14n.tastinggenie.domain.model.UiError
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
                        onDeleteSake = {},
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
                        onDeleteSake = {},
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
                        onDeleteSake = {},
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
                        onDeleteSake = {},
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
                        onDeleteSake = {},
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

    @Test
    fun deleteAction_opensConfirmationDialogAndConfirms() {
        var deleteRequested = false
        var deleteConfirmed = false
        composeRule.setContent {
            SakeListScreen(
                state =
                    SakeListUiState(
                        isLoading = false,
                        pendingDeleteSake =
                            PendingDeleteSake(
                                sakeId = 7L,
                                sakeName = "夏酒",
                                reviewCount = 2,
                                hasImage = true,
                            ),
                        sakes =
                            listOf(
                                Sake(
                                    id = 7L,
                                    name = "夏酒",
                                    grade = SakeGrade.JUNMAI,
                                ),
                            ),
                        gradeLabels = mapOf(SakeGrade.JUNMAI.name to "純米"),
                    ),
                onCreateSake = {},
                itemActions =
                    SakeListItemActions(
                        onOpenSake = {},
                        onEditSake = {},
                        onDeleteSake = { deleteRequested = true },
                    ),
                topBarActions =
                    SakeListTopBarActions(
                        onOpenHelp = {},
                        onOpenSettings = {},
                    ),
                deleteDialogActions =
                    SakeListDeleteDialogActions(
                        onDismiss = {},
                        onConfirm = { deleteConfirmed = true },
                    ),
            )
        }

        composeRule.onNodeWithContentDescription("酒を削除").performClick()
        composeRule.runOnIdle { assertTrue(deleteRequested) }
        composeRule.onNodeWithText("この酒を削除しますか？関連するレビュー 2 件と画像も削除します。").assertExists()
        composeRule.onNodeWithText("確定").performClick()
        composeRule.runOnIdle { assertTrue(deleteConfirmed) }
    }

    @Test
    fun deleteError_staysVisibleWhenListBecomesEmpty() {
        composeRule.setContent {
            SakeListScreen(
                state =
                    SakeListUiState(
                        isLoading = false,
                        deleteError =
                            UiError(messageResId = R.string.error_delete_sake_image_cleanup),
                        sakes = emptyList(),
                    ),
                onCreateSake = {},
                itemActions =
                    SakeListItemActions(
                        onOpenSake = {},
                        onEditSake = {},
                        onDeleteSake = {},
                    ),
                topBarActions =
                    SakeListTopBarActions(
                        onOpenHelp = {},
                        onOpenSettings = {},
                    ),
            )
        }

        composeRule.onNodeWithText("酒は削除しましたが画像の削除に失敗しました").assertExists()
        composeRule.onNodeWithText("登録された酒がありません").assertExists()
    }
}
