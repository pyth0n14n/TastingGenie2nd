package io.github.pyth0n14n.tastinggenie.feature.sake.edit

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.pyth0n14n.tastinggenie.domain.model.MasterOption
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
                onNameChanged = {},
                onGradeSelected = { selectedValue = it },
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
                onNameChanged = {},
                onGradeSelected = {},
                onSave = { saveCalled = true },
                onBack = {},
            )
        }

        composeRule.onNodeWithText("保存").performClick()
        composeRule.runOnIdle { assertTrue(saveCalled) }
    }
}
