package io.github.pyth0n14n.tastinggenie.ui.common

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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

        composeRule.onNodeWithText("未選択").performClick()
        composeRule.onNodeWithText("選択").assertIsDisplayed()
        composeRule.onNodeWithText("キャンセル").assertIsDisplayed()
    }
}
