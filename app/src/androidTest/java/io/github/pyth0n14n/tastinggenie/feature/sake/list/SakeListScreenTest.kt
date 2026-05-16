package io.github.pyth0n14n.tastinggenie.feature.sake.list

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.Sake
import io.github.pyth0n14n.tastinggenie.domain.model.SakeListSummary
import io.github.pyth0n14n.tastinggenie.domain.model.UiError
import io.github.pyth0n14n.tastinggenie.domain.model.enums.OverallReview
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Prefecture
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
                actions = screenActions(onCreateSake = { called = true }),
            )
        }

        composeRule.onNodeWithContentDescription("追加").performClick()
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
                                SakeListSummary(
                                    sake =
                                        Sake(
                                            id = 42L,
                                            name = "吟醸酒",
                                            grade = SakeGrade.GINJO,
                                        ),
                                ),
                            ),
                        gradeLabels = mapOf(SakeGrade.GINJO.name to "吟醸"),
                    ),
                actions = screenActions(onOpenSake = { openedId = it }),
            )
        }

        composeRule.onNodeWithText("吟醸").assertIsDisplayed()
        composeRule.onNodeWithText("吟醸酒").performClick()
        composeRule.runOnIdle { assertEquals(42L, openedId) }
    }

    @Test
    fun sakeItem_withCity_displaysPrefectureAndCity() {
        composeRule.setContent {
            SakeListScreen(
                state =
                    SakeListUiState(
                        isLoading = false,
                        sakes =
                            listOf(
                                SakeListSummary(
                                    sake =
                                        Sake(
                                            id = 42L,
                                            name = "吟醸酒",
                                            grade = SakeGrade.GINJO,
                                            prefecture = Prefecture.NAGANO,
                                            city = "諏訪市",
                                        ),
                                ),
                            ),
                        gradeLabels = mapOf(SakeGrade.GINJO.name to "吟醸"),
                        prefectureLabels = mapOf(Prefecture.NAGANO.name to "長野県"),
                    ),
                actions = screenActions(),
            )
        }

        composeRule.onNodeWithText("長野県 諏訪市").assertIsDisplayed()
    }

    @Test
    fun topBarActions_openSettings() {
        var settingsOpened = false
        composeRule.setContent {
            SakeListScreen(
                state = SakeListUiState(isLoading = false),
                actions =
                    screenActions(
                        onOpenSettings = { settingsOpened = true },
                    ),
            )
        }

        composeRule.onNodeWithContentDescription("設定").performClick()
        composeRule.runOnIdle {
            assertTrue(settingsOpened)
        }
    }

    @Test
    fun helpHintsDisabled_keepsSettingsAction() {
        composeRule.setContent {
            SakeListScreen(
                state =
                    SakeListUiState(
                        isLoading = false,
                        showHelpHints = false,
                    ),
                actions = screenActions(),
            )
        }

        assertEquals(0, composeRule.onAllNodesWithContentDescription("ヘルプ").fetchSemanticsNodes().size)
        composeRule.onNodeWithContentDescription("設定").assertIsDisplayed()
    }

    @Test
    fun itemOverflowActions_useEditAndPinActions() {
        var editedId: Long? = null
        var favoriteToggle: Pair<Long, Boolean>? = null
        composeRule.setContent {
            SakeListScreen(
                state =
                    SakeListUiState(
                        isLoading = false,
                        sakes =
                            listOf(
                                SakeListSummary(
                                    sake =
                                        Sake(
                                            id = 9L,
                                            name = "冬酒",
                                            grade = SakeGrade.JUNMAI,
                                            isPinned = false,
                                        ),
                                ),
                            ),
                        gradeLabels = mapOf(SakeGrade.JUNMAI.name to "純米"),
                    ),
                actions =
                    screenActions(
                        onEditSake = { editedId = it },
                        onTogglePinned = { id, isPinned -> favoriteToggle = id to isPinned },
                    ),
            )
        }

        composeRule.onAllNodesWithContentDescription("その他の操作")[0].performClick()
        composeRule.onNodeWithText("編集").performClick()
        composeRule.onAllNodesWithContentDescription("その他の操作")[0].performClick()
        composeRule.onNodeWithText("固定").performClick()
        composeRule.runOnIdle {
            assertEquals(9L, editedId)
            assertEquals(9L to true, favoriteToggle)
        }
    }

    @Test
    fun sakeItems_areSeparatedByPinnedSectionsAndCanCollapse() {
        composeRule.setContent {
            SakeListScreen(
                state =
                    SakeListUiState(
                        isLoading = false,
                        sakes =
                            listOf(
                                SakeListSummary(
                                    sake =
                                        Sake(
                                            id = 1L,
                                            name = "固定酒",
                                            grade = SakeGrade.JUNMAI,
                                            isPinned = true,
                                        ),
                                ),
                                SakeListSummary(
                                    sake =
                                        Sake(
                                            id = 2L,
                                            name = "通常酒",
                                            grade = SakeGrade.GINJO,
                                            isPinned = false,
                                        ),
                                ),
                            ),
                        gradeLabels =
                            mapOf(
                                SakeGrade.JUNMAI.name to "純米",
                                SakeGrade.GINJO.name to "吟醸",
                            ),
                    ),
                actions = screenActions(),
            )
        }

        composeRule.onNodeWithText("ピン止め").assertIsDisplayed()
        composeRule.onNodeWithText("酒一覧").assertIsDisplayed()
        composeRule.onNodeWithText("固定酒").assertIsDisplayed()
        composeRule.onNodeWithText("通常酒").assertIsDisplayed()

        composeRule.onNodeWithText("ピン止め").performClick()

        composeRule.onNodeWithText("固定酒").assertDoesNotExist()
        composeRule.onNodeWithText("通常酒").assertIsDisplayed()
    }

    @Test
    fun searchField_filtersVisibleSakes() {
        composeRule.setContent {
            SakeListScreen(
                state =
                    SakeListUiState(
                        isLoading = false,
                        searchQuery = "高木",
                        sakes =
                            listOf(
                                SakeListSummary(
                                    sake =
                                        Sake(
                                            id = 1L,
                                            name = "十四代",
                                            grade = SakeGrade.JUNMAI,
                                            maker = "高木酒造",
                                        ),
                                ),
                                SakeListSummary(
                                    sake =
                                        Sake(
                                            id = 2L,
                                            name = "而今",
                                            grade = SakeGrade.GINJO,
                                            maker = "木屋正酒造",
                                        ),
                                ),
                            ),
                        gradeLabels =
                            mapOf(
                                SakeGrade.JUNMAI.name to "純米",
                                SakeGrade.GINJO.name to "吟醸",
                            ),
                    ),
                actions = screenActions(),
            )
        }

        composeRule.onNodeWithText("十四代").assertIsDisplayed()
        assertEquals(0, composeRule.onAllNodesWithText("而今").fetchSemanticsNodes().size)
    }

    @Test
    fun titleHeight_staysStableForShortAndLongNames() {
        composeRule.setContent {
            SakeListScreen(
                state =
                    SakeListUiState(
                        isLoading = false,
                        sakes =
                            listOf(
                                SakeListSummary(
                                    sake =
                                        Sake(
                                            id = 12L,
                                            name = "短い名前",
                                            grade = SakeGrade.JUNMAI,
                                        ),
                                ),
                                SakeListSummary(
                                    sake =
                                        Sake(
                                            id = 13L,
                                            name = "とても長い銘柄名で二行に折り返されることを想定したテスト用の名前",
                                            grade = SakeGrade.JUNMAI,
                                        ),
                                ),
                            ),
                        gradeLabels = mapOf(SakeGrade.JUNMAI.name to "純米"),
                    ),
                actions = screenActions(),
            )
        }

        val shortBounds = composeRule.onNodeWithText("短い名前").fetchSemanticsNode().boundsInRoot
        val longBounds =
            composeRule
                .onNodeWithText("とても長い銘柄名で二行に折り返されることを想定したテスト用の名前")
                .fetchSemanticsNode()
                .boundsInRoot
        assertEquals(shortBounds.height, longBounds.height, 0.01f)
    }

    @Test
    fun averageReview_usesStableStarRowWithAndWithoutValue() {
        composeRule.setContent {
            SakeListScreen(
                state =
                    SakeListUiState(
                        isLoading = false,
                        sakes =
                            listOf(
                                SakeListSummary(
                                    sake = Sake(id = 10L, name = "評価あり", grade = SakeGrade.JUNMAI),
                                    averageOverallReview = 4.5,
                                ),
                                SakeListSummary(
                                    sake = Sake(id = 11L, name = "評価なし", grade = SakeGrade.GINJO),
                                    averageOverallReview = null,
                                ),
                            ),
                        gradeLabels =
                            mapOf(
                                SakeGrade.JUNMAI.name to "純米",
                                SakeGrade.GINJO.name to "吟醸",
                            ),
                        overallReviewLabels = mapOf(OverallReview.GOOD.name to "好き"),
                    ),
                actions = screenActions(),
            )
        }

        composeRule.onNodeWithContentDescription("平均評価 4.50").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("平均評価なし").assertIsDisplayed()
    }

    @Test
    fun sakeImagePlaceholder_displaysWhenImageMissing() {
        composeRule.setContent {
            SakeListScreen(
                state =
                    SakeListUiState(
                        isLoading = false,
                        sakes =
                            listOf(
                                SakeListSummary(
                                    sake =
                                        Sake(
                                            id = 7L,
                                            name = "夏酒",
                                            grade = SakeGrade.JUNMAI,
                                            imageUris = emptyList(),
                                        ),
                                ),
                            ),
                        gradeLabels = mapOf(SakeGrade.JUNMAI.name to "純米"),
                    ),
                actions = screenActions(),
            )
        }

        composeRule.onNodeWithText("画像").assertIsDisplayed()
    }

    @Test
    fun sakeImage_displaysWhenPresent() {
        var openedImageSakeId: Long? = null
        composeRule.setContent {
            SakeListScreen(
                state =
                    SakeListUiState(
                        isLoading = false,
                        sakes =
                            listOf(
                                SakeListSummary(
                                    sake =
                                        Sake(
                                            id = 8L,
                                            name = "秋酒",
                                            grade = SakeGrade.GINJO,
                                            imageUris = listOf("file:///images/sakes/autumn.jpg"),
                                        ),
                                ),
                            ),
                        gradeLabels = mapOf(SakeGrade.GINJO.name to "吟醸"),
                    ),
                actions = screenActions(onOpenSakeImage = { openedImageSakeId = it }),
            )
        }

        composeRule.onNodeWithContentDescription("酒画像").performClick()
        composeRule.runOnIdle { assertEquals(8L, openedImageSakeId) }
    }

    @Test
    fun itemOverflowActions_includeImageAction() {
        var openedImageSakeId: Long? = null
        composeRule.setContent {
            SakeListScreen(
                state =
                    SakeListUiState(
                        isLoading = false,
                        sakes =
                            listOf(
                                SakeListSummary(
                                    sake =
                                        Sake(
                                            id = 8L,
                                            name = "秋酒",
                                            grade = SakeGrade.GINJO,
                                            imageUris = listOf("file:///images/sakes/autumn.jpg"),
                                        ),
                                ),
                            ),
                        gradeLabels = mapOf(SakeGrade.GINJO.name to "吟醸"),
                    ),
                actions = screenActions(onOpenSakeImage = { openedImageSakeId = it }),
            )
        }

        composeRule.onAllNodesWithContentDescription("その他の操作")[0].performClick()
        composeRule.onNodeWithText("画像").performClick()
        composeRule.runOnIdle { assertEquals(8L, openedImageSakeId) }
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
                                SakeListSummary(
                                    sake =
                                        Sake(
                                            id = 7L,
                                            name = "夏酒",
                                            grade = SakeGrade.JUNMAI,
                                        ),
                                ),
                            ),
                        gradeLabels = mapOf(SakeGrade.JUNMAI.name to "純米"),
                    ),
                actions = screenActions(onDeleteSake = { deleteRequested = true }),
                deleteDialogActions =
                    SakeListDeleteDialogActions(
                        onDismiss = {},
                        onConfirm = { deleteConfirmed = true },
                    ),
            )
        }

        composeRule.onAllNodesWithContentDescription("その他の操作")[0].performClick()
        composeRule.onNodeWithText("削除").performClick()
        composeRule.runOnIdle { assertTrue(deleteRequested) }
        composeRule.onNodeWithText("この酒を削除しますか？関連するレビュー 2 件と画像参照も削除します。").assertIsDisplayed()
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
                actions = screenActions(),
            )
        }

        composeRule.onNodeWithText("酒は削除しましたが画像の削除に失敗しました").assertIsDisplayed()
        composeRule.onNodeWithText("登録された酒がありません").assertIsDisplayed()
    }
}

private fun screenActions(
    onCreateSake: () -> Unit = {},
    onOpenSake: (Long) -> Unit = {},
    onEditSake: (Long) -> Unit = {},
    onOpenSakeImage: (Long) -> Unit = {},
    onDeleteSake: (Long) -> Unit = {},
    onTogglePinned: (Long, Boolean) -> Unit = { _, _ -> },
    onOpenSettings: () -> Unit = {},
): SakeListScreenActions =
    SakeListScreenActions(
        onCreateSake = onCreateSake,
        itemActions =
            SakeListItemActions(
                onOpenSake = onOpenSake,
                onEditSake = onEditSake,
                onOpenSakeImage = onOpenSakeImage,
                onDeleteSake = onDeleteSake,
                onTogglePinned = onTogglePinned,
            ),
        topBarActions =
            SakeListTopBarActions(
                onOpenSettings = onOpenSettings,
            ),
    )
