package io.github.pyth0n14n.tastinggenie.feature.sake.edit

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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

        composeRule.onNodeWithText("画像を選択").performClick()
        composeRule.runOnIdle { assertTrue(pickCalled) }
    }

    @Test
    fun deletingImage_requiresConfirmation() {
        var deleteCalled = false
        composeRule.setContent {
            SakeEditScreen(
                state =
                    SakeEditUiState(
                        isLoading = false,
                        imagePreviewUri = "file:///images/sakes/preview.jpg",
                        gradeOptions = listOf(MasterOption(value = SakeGrade.JUNMAI.name, label = "純米")),
                    ),
                callbacks = defaultCallbacks(onDeleteImage = { deleteCalled = true }),
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
        composeRule.onNodeWithText("* は必須項目です").assertIsDisplayed()
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

        composeRule.onNodeWithText("都道府県: 未選択").assertExists()
        composeRule.onNodeWithText("都道府県: 未選択").performClick()
        composeRule.onNodeWithText("[+] 北関東").performClick()
        composeRule.onNodeWithText("  ( ) 長野県").performClick()
        composeRule.runOnIdle { assertEquals(Prefecture.NAGANO.name, selectedValue) }
    }
}

private fun defaultCallbacks(
    onTextChanged: (SakeTextField, String) -> Unit = { _, _ -> },
    onGradeSelected: (String) -> Unit = {},
    onClassificationToggled: (String) -> Unit = {},
    onPrefectureSelected: (String?) -> Unit = {},
    onPickImageRequest: () -> Unit = {},
    onDeleteImage: () -> Unit = {},
): SakeEditCallbacks =
    SakeEditCallbacks(
        onTextChanged = onTextChanged,
        onGradeSelected = onGradeSelected,
        onClassificationToggled = onClassificationToggled,
        onPrefectureSelected = onPrefectureSelected,
        onPickImageRequest = onPickImageRequest,
        onDeleteImage = onDeleteImage,
    )
