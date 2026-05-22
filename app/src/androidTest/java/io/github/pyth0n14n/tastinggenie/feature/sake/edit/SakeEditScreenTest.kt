package io.github.pyth0n14n.tastinggenie.feature.sake.edit

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import io.github.pyth0n14n.tastinggenie.domain.model.MasterOption
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Prefecture
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeClassification
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeGrade
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SakeEditScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun selectingGradeFromDropdown_callsOnGradeSelected() {
        var selectedValue: String? = null
        composeRule.setContent {
            SakeEditScreen(
                state =
                    SakeEditUiState(
                        isLoading = false,
                        gradeOptions =
                            listOf(
                                MasterOption(value = SakeGrade.JUNMAI.name, label = "純米"),
                                MasterOption(value = SakeGrade.GINJO.name, label = "吟醸"),
                            ),
                    ),
                callbacks = defaultCallbacks(onGradeSelected = { selectedValue = it }),
                onSave = {},
                onBack = {},
            )
        }

        composeRule.onNodeWithText("種別 *: 未選択").performClick()
        composeRule.onNodeWithText("吟醸").performClick()
        composeRule.runOnIdle { assertEquals(SakeGrade.GINJO.name, selectedValue) }
    }

    @Test
    fun onSaveClick_callsOnSave() {
        var saveCalled = false
        composeRule.setContent {
            SakeEditScreen(
                state =
                    SakeEditUiState(
                        isLoading = false,
                        name = "テスト銘柄",
                        grade = SakeGrade.JUNMAI,
                        gradeOptions = listOf(MasterOption(value = SakeGrade.JUNMAI.name, label = "純米")),
                    ),
                callbacks = defaultCallbacks(),
                onSave = { saveCalled = true },
                onBack = {},
            )
        }

        composeRule.onNodeWithText("保存").performClick()
        composeRule.runOnIdle { assertTrue(saveCalled) }
    }

    @Test
    fun nameField_acceptsJapaneseTextInput() {
        composeRule.setContent {
            var state by mutableStateOf(
                SakeEditUiState(
                    isLoading = false,
                    gradeOptions = listOf(MasterOption(value = SakeGrade.JUNMAI.name, label = "純米")),
                ),
            )
            SakeEditScreen(
                state = state,
                callbacks =
                    defaultCallbacks(
                        onTextChanged = { field, value ->
                            if (field == SakeTextField.NAME) {
                                state = state.copy(name = value)
                            }
                        },
                    ),
                onSave = {},
                onBack = {},
            )
        }

        composeRule.onNode(hasText("銘柄名 *") and hasSetTextAction()).performTextInput("獺祭 純米大吟醸")

        composeRule.onNodeWithText("獺祭 純米大吟醸").assertIsDisplayed()
    }

    @Test
    fun backWithUnsavedChanges_showsDiscardDialogAndConfirmsBeforeLeaving() {
        var backCalled = false
        composeRule.setContent {
            var state by mutableStateOf(
                SakeEditUiState(
                    isLoading = false,
                    gradeOptions = listOf(MasterOption(value = SakeGrade.JUNMAI.name, label = "純米")),
                ),
            )
            SakeEditScreen(
                state = state,
                callbacks =
                    defaultCallbacks(
                        onTextChanged = { field, value ->
                            if (field == SakeTextField.NAME) {
                                state = state.copy(name = value)
                            }
                        },
                    ),
                onSave = {},
                onBack = { backCalled = true },
            )
        }

        composeRule.onNode(hasText("銘柄名 *") and hasSetTextAction()).performTextInput("テスト銘柄")
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
    fun saveButton_staysVisibleAfterScrollingForm() {
        composeRule.setContent {
            SakeEditScreen(
                state =
                    SakeEditUiState(
                        isLoading = false,
                        gradeOptions = listOf(MasterOption(value = SakeGrade.JUNMAI.name, label = "純米")),
                    ),
                callbacks = defaultCallbacks(),
                onSave = {},
                onBack = {},
            )
        }

        composeRule.onNodeWithText("保存").assertIsDisplayed()
        composeRule.onNodeWithTag("sake_edit_form").performTouchInput { swipeUp() }
        composeRule.onNodeWithText("アルコール度数").assertIsDisplayed()
        composeRule.onNodeWithText("保存").assertIsDisplayed()
    }

    @Test
    fun formSections_areDisplayed() {
        composeRule.setContent {
            SakeEditScreen(
                state =
                    SakeEditUiState(
                        isLoading = false,
                        gradeOptions = listOf(MasterOption(value = SakeGrade.JUNMAI.name, label = "純米")),
                    ),
                callbacks = defaultCallbacks(),
                onSave = {},
                onBack = {},
            )
        }

        composeRule.onAllNodesWithText("画像")[0].assertIsDisplayed()
        composeRule.onNodeWithText("基本情報").assertIsDisplayed()
        composeRule.onNodeWithTag("sake_edit_form").performScrollToNode(hasText("詳細情報"))
        composeRule.onNodeWithText("詳細情報").assertIsDisplayed()
    }

    @Test
    fun selectingOtherClassification_showsFreeTextField() {
        composeRule.setContent {
            SakeEditScreen(
                state =
                    SakeEditUiState(
                        isLoading = false,
                        gradeOptions = listOf(MasterOption(value = SakeGrade.JUNMAI.name, label = "純米")),
                        classificationOptions =
                            listOf(
                                MasterOption(value = SakeClassification.KIMOTO.name, label = "生酛"),
                                MasterOption(value = SakeClassification.OTHER.name, label = "その他"),
                            ),
                        classifications = listOf(SakeClassification.OTHER),
                    ),
                callbacks = defaultCallbacks(),
                onSave = {},
                onBack = {},
            )
        }

        composeRule.onNodeWithText("分類（その他）").assertIsDisplayed()
    }

    @Test
    fun selectingOtherGrade_showsFreeTextField() {
        composeRule.setContent {
            SakeEditScreen(
                state =
                    SakeEditUiState(
                        isLoading = false,
                        grade = SakeGrade.OTHER,
                        gradeOptions =
                            listOf(
                                MasterOption(value = SakeGrade.JUNMAI.name, label = "純米"),
                                MasterOption(value = SakeGrade.OTHER.name, label = "その他"),
                            ),
                    ),
                callbacks = defaultCallbacks(),
                onSave = {},
                onBack = {},
            )
        }

        composeRule.onNodeWithText("種別（その他）").assertIsDisplayed()
    }

    @Test
    fun classificationPicker_withHelpHintsOn_showsOptionDescriptions() {
        composeRule.setContent {
            SakeEditScreen(
                state =
                    SakeEditUiState(
                        isLoading = false,
                        showHelpHints = true,
                        gradeOptions = listOf(MasterOption(value = SakeGrade.JUNMAI.name, label = "純米")),
                        classificationOptions =
                            listOf(
                                MasterOption(
                                    value = SakeClassification.KIMOTO.name,
                                    label = "生酛",
                                    description = "自然の乳酸菌を使う、伝統的な酒母づくり",
                                ),
                            ),
                    ),
                callbacks = defaultCallbacks(),
                onSave = {},
                onBack = {},
            )
        }

        composeRule.onNodeWithContentDescription("分類: 未選択").performClick()
        composeRule.onNodeWithText("酛").performClick()
        composeRule.onNodeWithText("自然の乳酸菌を使う、伝統的な酒母づくり").assertIsDisplayed()
    }

    @Test
    fun classificationPicker_withHelpHintsOff_hidesOptionDescriptions() {
        composeRule.setContent {
            SakeEditScreen(
                state =
                    SakeEditUiState(
                        isLoading = false,
                        showHelpHints = false,
                        gradeOptions = listOf(MasterOption(value = SakeGrade.JUNMAI.name, label = "純米")),
                        classificationOptions =
                            listOf(
                                MasterOption(
                                    value = SakeClassification.KIMOTO.name,
                                    label = "生酛",
                                    description = "自然の乳酸菌を使う、伝統的な酒母づくり",
                                ),
                            ),
                    ),
                callbacks = defaultCallbacks(),
                onSave = {},
                onBack = {},
            )
        }

        composeRule.onNodeWithContentDescription("分類: 未選択").performClick()
        composeRule.onNodeWithText("酛").performClick()
        assertEquals(
            0,
            composeRule.onAllNodesWithText("自然の乳酸菌を使う、伝統的な酒母づくり").fetchSemanticsNodes().size,
        )
    }

    @Test
    fun selectingBothOtherValues_showsSeparateFields() {
        composeRule.setContent {
            SakeEditScreen(
                state =
                    SakeEditUiState(
                        isLoading = false,
                        grade = SakeGrade.OTHER,
                        gradeOptions =
                            listOf(
                                MasterOption(value = SakeGrade.JUNMAI.name, label = "純米"),
                                MasterOption(value = SakeGrade.OTHER.name, label = "その他"),
                            ),
                        classificationOptions =
                            listOf(
                                MasterOption(value = SakeClassification.KIMOTO.name, label = "生酛"),
                                MasterOption(value = SakeClassification.OTHER.name, label = "その他"),
                            ),
                        classifications = listOf(SakeClassification.OTHER),
                    ),
                callbacks = defaultCallbacks(),
                onSave = {},
                onBack = {},
            )
        }

        composeRule.onNodeWithText("種別（その他）").assertIsDisplayed()
        composeRule.onNodeWithText("分類（その他）").assertIsDisplayed()
    }

    @Test
    fun selectImageButton_callsOnPickImageRequest() {
        var pickCalled = false
        composeRule.setContent {
            SakeEditScreen(
                state =
                    SakeEditUiState(
                        isLoading = false,
                        gradeOptions = listOf(MasterOption(value = SakeGrade.JUNMAI.name, label = "純米")),
                    ),
                callbacks = defaultCallbacks(onPickImageRequest = { pickCalled = true }),
                onSave = {},
                onBack = {},
            )
        }

        composeRule.onNodeWithText("画像を追加").performClick()
        composeRule.onNodeWithText("フォルダから選択").performClick()
        composeRule.runOnIdle { assertTrue(pickCalled) }
    }

    @Test
    fun captureImageButton_callsOnCaptureImageRequest() {
        var captureCalled = false
        composeRule.setContent {
            SakeEditScreen(
                state =
                    SakeEditUiState(
                        isLoading = false,
                        gradeOptions = listOf(MasterOption(value = SakeGrade.JUNMAI.name, label = "純米")),
                    ),
                callbacks = defaultCallbacks(onCaptureImageRequest = { captureCalled = true }),
                onSave = {},
                onBack = {},
            )
        }

        composeRule.onNodeWithText("画像を追加").performClick()
        composeRule.onNodeWithText("カメラで撮影").performClick()
        composeRule.runOnIdle { assertTrue(captureCalled) }
    }

    @Test
    fun deletingImage_requiresConfirmation() {
        var deleteCalled = false
        composeRule.setContent {
            SakeEditScreen(
                state =
                    SakeEditUiState(
                        isLoading = false,
                        imagePreviewUris = listOf("file:///images/sakes/preview.jpg"),
                        gradeOptions = listOf(MasterOption(value = SakeGrade.JUNMAI.name, label = "純米")),
                    ),
                callbacks = defaultCallbacks(onDeleteImage = { _ -> deleteCalled = true }),
                onSave = {},
                onBack = {},
            )
        }

        composeRule.onNodeWithText("画像を削除").performClick()
        composeRule.onNodeWithText("この酒の画像を削除しますか？").assertIsDisplayed()
        composeRule.onNodeWithText("確定").performClick()
        composeRule.runOnIdle { assertTrue(deleteCalled) }
    }

    @Test
    fun pr4Fields_areDisplayed() {
        composeRule.setContent {
            SakeEditScreen(
                state =
                    SakeEditUiState(
                        isLoading = false,
                        gradeOptions = listOf(MasterOption(value = SakeGrade.JUNMAI.name, label = "純米")),
                    ),
                callbacks = defaultCallbacks(),
                onSave = {},
                onBack = {},
            )
        }

        composeRule.onNodeWithText("日本酒度").assertIsDisplayed()
        composeRule.onNodeWithText("酸度").assertIsDisplayed()
        composeRule.onNodeWithText("麹米").assertIsDisplayed()
        composeRule.onNodeWithText("麹米精米歩合").assertIsDisplayed()
        composeRule.onNodeWithText("酒米").assertIsDisplayed()
        composeRule.onNodeWithText("酒米精米歩合").assertIsDisplayed()
        composeRule.onNodeWithText("アルコール度数").assertIsDisplayed()
        composeRule.onNodeWithText("酵母").assertIsDisplayed()
        composeRule.onNodeWithText("水").assertIsDisplayed()
        composeRule.onNodeWithText("市").assertIsDisplayed()
        composeRule.onNodeWithText("* は必須項目です").assertIsDisplayed()
    }

    @Test
    fun detailHelp_withHelpHintsOn_opensBottomSheet() {
        composeRule.setContent {
            SakeEditScreen(
                state =
                    SakeEditUiState(
                        isLoading = false,
                        showHelpHints = true,
                        gradeOptions = listOf(MasterOption(value = SakeGrade.JUNMAI.name, label = "純米")),
                    ),
                callbacks = defaultCallbacks(),
                onSave = {},
                onBack = {},
            )
        }

        composeRule.onNodeWithTag("sake_edit_form").performScrollToNode(hasText("詳細情報"))
        composeRule.onNodeWithContentDescription("詳細情報のヘルプ").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("詳細情報のヘルプ").performClick()
        composeRule.onAllNodesWithText("詳細情報のヘルプ")[0].assertIsDisplayed()
        composeRule.onAllNodesWithText("麹米")[0].assertIsDisplayed()
        composeRule.onNodeWithText("麹づくりに使用される米。").assertIsDisplayed()
        composeRule.onNodeWithText("精米歩合").assertIsDisplayed()
    }

    @Test
    fun detailHelp_withHelpHintsOff_hidesHelpAction() {
        composeRule.setContent {
            SakeEditScreen(
                state =
                    SakeEditUiState(
                        isLoading = false,
                        showHelpHints = false,
                        gradeOptions = listOf(MasterOption(value = SakeGrade.JUNMAI.name, label = "純米")),
                    ),
                callbacks = defaultCallbacks(),
                onSave = {},
                onBack = {},
            )
        }

        assertEquals(
            0,
            composeRule.onAllNodesWithContentDescription("詳細情報のヘルプ").fetchSemanticsNodes().size,
        )
    }

    @Test
    fun selectingPrefectureFromGroupedDropdown_callsOnPrefectureSelected() {
        var selectedValue: String? = null
        composeRule.setContent {
            SakeEditScreen(
                state =
                    SakeEditUiState(
                        isLoading = false,
                        gradeOptions = listOf(MasterOption(value = SakeGrade.JUNMAI.name, label = "純米")),
                        prefectureOptions =
                            listOf(
                                MasterOption(value = Prefecture.HOKKAIDO.name, label = "北海道"),
                                MasterOption(value = Prefecture.NAGANO.name, label = "長野県"),
                            ),
                    ),
                callbacks = defaultCallbacks(onPrefectureSelected = { selectedValue = it }),
                onSave = {},
                onBack = {},
            )
        }

        composeRule.onNodeWithContentDescription("都道府県: 未選択").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("都道府県: 未選択").performClick()
        composeRule.onNodeWithText("北関東").assertIsDisplayed()
        composeRule.onNodeWithText("長野県").performClick()
        composeRule.runOnIdle { assertEquals(Prefecture.NAGANO.name, selectedValue) }
    }
}

private fun defaultCallbacks(
    onTextChanged: (SakeTextField, String) -> Unit = { _, _ -> },
    onGradeSelected: (String) -> Unit = {},
    onClassificationsChanged: (List<String>) -> Unit = {},
    onPrefectureSelected: (String?) -> Unit = {},
    onPickImageRequest: () -> Unit = {},
    onCaptureImageRequest: () -> Unit = {},
    onDeleteImage: (String) -> Unit = {},
): SakeEditCallbacks =
    SakeEditCallbacks(
        onTextChanged = onTextChanged,
        onGradeSelected = onGradeSelected,
        onClassificationsChanged = onClassificationsChanged,
        onPrefectureSelected = onPrefectureSelected,
        onPickImageRequest = onPickImageRequest,
        onCaptureImageRequest = onCaptureImageRequest,
        onDeleteImage = onDeleteImage,
    )
