package io.github.pyth0n14n.tastinggenie.ui.common

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.click
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNode
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.SemanticsMatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SelectionComponentsTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun confirmationDialog_callsConfirmAction() {
        var confirmCalled = false
        composeRule.setContent {
            ConfirmationDialog(
                title = "削除確認",
                message = "本当に削除しますか？",
                onConfirm = { confirmCalled = true },
                onDismiss = {},
            )
        }

        composeRule.onNodeWithText("確定").performClick()
        composeRule.runOnIdle { assertTrue(confirmCalled) }
    }

    @Test
    fun segmentedSingleChoiceField_callsOnSelected() {
        var selectedValue: String? = null
        composeRule.setContent {
            SegmentedSingleChoiceField(
                label = "強度",
                options =
                    listOf(
                        DropdownOption(value = "LOW", label = "弱"),
                        DropdownOption(value = "HIGH", label = "強"),
                    ),
                selectedValue = null,
                onSelected = { selectedValue = it },
            )
        }

        composeRule.onNodeWithText("強").performClick()
        composeRule.runOnIdle { assertEquals("HIGH", selectedValue) }
    }

    @Test
    fun groupedMultiSelectDropdown_expandsCategoryBeforeTogglingOption() {
        var toggledValue: String? = null
        composeRule.setContent {
            GroupedMultiSelectDropdown(
                label = "香り",
                groups =
                    listOf(
                        DropdownOptionGroup(
                            label = "果実",
                            options = listOf(DropdownOption(value = "APPLE", label = "りんご")),
                        ),
                    ),
                selectedValues = emptyList(),
                onToggle = { toggledValue = it },
            )
        }

        composeRule.onNodeWithText("なし").performClick()
        composeRule.runOnIdle {
            assertEquals(0, composeRule.onAllNodesWithText("  [ ] りんご").fetchSemanticsNodes().size)
        }
        composeRule.onNodeWithText("[+] 果実").performClick()
        composeRule.onNodeWithText("  [ ] りんご").performClick()
        composeRule.runOnIdle { assertEquals("APPLE", toggledValue) }
    }

    @Test
    fun groupedSingleSelectDropdown_expandsCategoryBeforeSelectingOption() {
        var selectedValue: String? = null
        composeRule.setContent {
            GroupedSingleSelectDropdown(
                label = "都道府県",
                groups =
                    listOf(
                        DropdownOptionGroup(
                            label = "北海道",
                            options = listOf(DropdownOption(value = "HOKKAIDO", label = "北海道")),
                        ),
                    ),
                selectedValue = null,
                onSelected = { selectedValue = it },
            )
        }

        composeRule.onNodeWithText("未選択").performClick()
        composeRule.onNodeWithText("[+] 北海道").performClick()
        composeRule.onNodeWithText("  ( ) 北海道").performClick()
        composeRule.runOnIdle { assertEquals("HOKKAIDO", selectedValue) }
    }

    @Test
    fun datePickerField_opensDialog() {
        composeRule.setContent {
            DatePickerField(
                label = "日付",
                value = "",
                onDateSelected = {},
            )
        }

        composeRule.onNodeWithText("未選択", useUnmergedTree = true).performTouchInput { click() }
        composeRule.onNodeWithText("選択").assertIsDisplayed()
        composeRule.onNodeWithText("キャンセル").assertIsDisplayed()
    }

    @Test
    fun discreteSliderField_setsSelectedValue() {
        var selectedValue: String? = null
        composeRule.setContent {
            var currentSelection by remember { mutableStateOf<String?>(null) }
            DiscreteSliderField(
                label = "香味強度",
                options =
                    listOf(
                        DropdownOption(value = "LOW", label = "弱い"),
                        DropdownOption(value = "MEDIUM", label = "中程度"),
                        DropdownOption(value = "HIGH", label = "強い"),
                    ),
                selectedValue = currentSelection,
                onValueChanged = {
                    currentSelection = it
                    selectedValue = it
                },
            )
        }

        composeRule.onNodeWithText("香味強度").assertIsDisplayed()
        composeRule.onNodeWithText("クリア").assertIsDisplayed().assertIsNotEnabled()
        composeRule.onNode(selectableOption("強い")).assertIsNotSelected()
        composeRule.onNode(selectableOption("強い")).performClick()
        composeRule.runOnIdle { assertEquals("HIGH", selectedValue) }
        composeRule.onNode(selectableOption("強い")).assertIsSelected()
        composeRule.onNode(selectableOption("弱い")).assertIsNotSelected()
        composeRule.onNodeWithText("クリア").assertIsEnabled()
    }

    @Test
    fun discreteSliderField_selectsMiddleOptionWhenUnselected() {
        var selectedValue: String? = null
        composeRule.setContent {
            var currentSelection by remember { mutableStateOf<String?>(null) }
            DiscreteSliderField(
                label = "香味強度",
                options =
                    listOf(
                        DropdownOption(value = "LOW", label = "弱い"),
                        DropdownOption(value = "MEDIUM", label = "中程度"),
                        DropdownOption(value = "HIGH", label = "強い"),
                    ),
                selectedValue = currentSelection,
                onValueChanged = {
                    currentSelection = it
                    selectedValue = it
                },
            )
        }

        composeRule.onNode(selectableOption("中程度")).performClick()
        composeRule.runOnIdle { assertEquals("MEDIUM", selectedValue) }
        composeRule.onNode(selectableOption("中程度")).assertIsSelected()
        composeRule.onNodeWithText("中程度").assertIsDisplayed()
    }

    @Test
    fun discreteSliderField_supportsLongerScales() {
        var selectedValue: String? = null
        composeRule.setContent {
            var currentSelection by remember { mutableStateOf<String?>(null) }
            DiscreteSliderField(
                label = "温度",
                options =
                    (1..10).map { index ->
                        DropdownOption(value = "LEVEL_$index", label = "温度$index")
                    },
                selectedValue = currentSelection,
                onValueChanged = {
                    currentSelection = it
                    selectedValue = it
                },
            )
        }

        composeRule.onNode(selectableOption("温度10")).assertIsDisplayed().performClick()
        composeRule.runOnIdle { assertEquals("LEVEL_10", selectedValue) }
        composeRule.onNode(selectableOption("温度10")).assertIsSelected()
    }

    @Test
    fun discreteSliderField_clearRestoresUnselectedState() {
        var selectedValue: String? = "HIGH"
        composeRule.setContent {
            var currentSelection by remember { mutableStateOf<String?>(selectedValue) }
            DiscreteSliderField(
                label = "香味強度",
                options =
                    listOf(
                        DropdownOption(value = "LOW", label = "弱い"),
                        DropdownOption(value = "MEDIUM", label = "中程度"),
                        DropdownOption(value = "HIGH", label = "強い"),
                    ),
                selectedValue = currentSelection,
                onValueChanged = {
                    currentSelection = it
                    selectedValue = it
                },
            )
        }

        composeRule.onNodeWithText("クリア").performClick()
        composeRule.runOnIdle { assertEquals(null, selectedValue) }
        composeRule.onNodeWithText("未選択").assertIsDisplayed()
        composeRule.onNodeWithText("クリア").assertIsNotEnabled()
    }

    @Test
    fun discreteSliderField_withNoOptions_doesNotCrash() {
        composeRule.setContent {
            DiscreteSliderField(
                label = "香味強度",
                options = emptyList(),
                selectedValue = null,
                onValueChanged = {},
            )
        }

        composeRule.onNodeWithText("香味強度").assertIsDisplayed()
        composeRule.onNodeWithText("未選択").assertIsDisplayed()
        composeRule.onNodeWithText("クリア").assertIsNotEnabled()
    }

    @Test
    fun starRatingField_setsAndClearsSelection() {
        var selectedValue: String? = null
        composeRule.setContent {
            var currentSelection by remember { mutableStateOf<String?>(null) }
            StarRatingField(
                label = "総合評価",
                options =
                    listOf(
                        DropdownOption(value = "VERY_BAD", label = "嫌い"),
                        DropdownOption(value = "BAD", label = "そうでもない"),
                        DropdownOption(value = "NEUTRAL", label = "普通"),
                        DropdownOption(value = "GOOD", label = "好き"),
                        DropdownOption(value = "VERY_GOOD", label = "大好き"),
                    ),
                selectedValue = currentSelection,
                onValueChanged = {
                    currentSelection = it
                    selectedValue = it
                },
            )
        }

        composeRule.onNodeWithContentDescription("総合評価 5").performClick()
        composeRule.runOnIdle { assertEquals("VERY_GOOD", selectedValue) }
        composeRule.onNodeWithText("クリア").performClick()
        composeRule.runOnIdle { assertEquals(null, selectedValue) }
    }
}

private fun selectableOption(label: String): SemanticsMatcher =
    hasText(label) and SemanticsMatcher.keyIsDefined(SemanticsProperties.Selected)
