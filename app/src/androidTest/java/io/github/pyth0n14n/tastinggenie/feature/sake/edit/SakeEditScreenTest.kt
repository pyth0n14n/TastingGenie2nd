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

        composeRule.onNodeWithText("種別: 未選択").performClick()
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
    onNameChanged: (String) -> Unit = {},
    onGradeSelected: (String) -> Unit = {},
    onGradeOtherChanged: (String) -> Unit = {},
    onClassificationToggled: (String) -> Unit = {},
    onTypeOtherChanged: (String) -> Unit = {},
    onMakerChanged: (String) -> Unit = {},
    onPrefectureSelected: (String?) -> Unit = {},
): SakeEditCallbacks =
    SakeEditCallbacks(
        onNameChanged = onNameChanged,
        onGradeSelected = onGradeSelected,
        onGradeOtherChanged = onGradeOtherChanged,
        onClassificationToggled = onClassificationToggled,
        onTypeOtherChanged = onTypeOtherChanged,
        onMakerChanged = onMakerChanged,
        onPrefectureSelected = onPrefectureSelected,
    )
