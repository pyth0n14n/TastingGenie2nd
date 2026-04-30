package io.github.pyth0n14n.tastinggenie.feature.review.edit

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.MasterOption
import io.github.pyth0n14n.tastinggenie.domain.model.UiError
import io.github.pyth0n14n.tastinggenie.domain.model.enums.TasteLevel
import io.github.pyth0n14n.tastinggenie.feature.review.ReviewSection
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ReviewEditScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun loadFailureWithEmptyRatingOptions_showsErrorWithoutCrashing() {
        composeRule.setContent {
            ReviewEditScreen(
                onBack = {},
                content =
                    ReviewEditScreenContent(
                        state =
                            ReviewEditUiState(
                                isLoading = false,
                                isEditTargetMissing = true,
                                error = UiError(messageResId = R.string.error_load_review),
                            ),
                        onAction = {},
                        onSave = {},
                        viscosityOptions = emptyList(),
                        volumeShortcutOptions = emptyList(),
                        selectedSection = ReviewSection.BASIC,
                        onSectionSelected = {},
                    ),
            )
        }

        composeRule.onNodeWithText("レビューの登録").assertIsDisplayed()
        composeRule.onNodeWithText("* は必須項目です").assertIsDisplayed()
        composeRule.onNodeWithText("レビュー情報の読み込みに失敗しました").assertIsDisplayed()
        composeRule.onNodeWithText("保存").assertIsNotEnabled()
    }

    @Test
    fun saveButton_staysVisibleAfterSwipeToAnotherSection() {
        composeRule.setContent {
            var selectedSection by mutableStateOf(ReviewSection.BASIC)
            ReviewEditScreen(
                onBack = {},
                content =
                    ReviewEditScreenContent(
                        state = ReviewEditUiState(isLoading = false),
                        onAction = {},
                        onSave = {},
                        viscosityOptions = emptyList(),
                        volumeShortcutOptions = emptyList(),
                        selectedSection = selectedSection,
                        onSectionSelected = { next -> selectedSection = next },
                    ),
            )
        }

        composeRule.onNodeWithText("保存").assertIsDisplayed()
        composeRule.onNodeWithTag("review_edit_pager").performTouchInput { swipeLeft() }
        composeRule.onNodeWithText("色").assertIsDisplayed()
        composeRule.onNodeWithText("保存").assertIsDisplayed()
    }

    @Test
    fun tabClick_jumpsDirectlyToTappedSection() {
        composeRule.setContent {
            var selectedSection by mutableStateOf(ReviewSection.BASIC)
            ReviewEditScreen(
                onBack = {},
                content =
                    ReviewEditScreenContent(
                        state = ReviewEditUiState(isLoading = false),
                        onAction = {},
                        onSave = {},
                        viscosityOptions = emptyList(),
                        volumeShortcutOptions = emptyList(),
                        selectedSection = selectedSection,
                        onSectionSelected = { next -> selectedSection = next },
                    ),
            )
        }

        composeRule.onNodeWithContentDescription("特記").performClick()
        composeRule.onNodeWithText("個性").assertIsDisplayed()
    }

    @Test
    fun aftertaste_usesLengthLabels() {
        composeRule.setContent {
            ReviewEditScreen(
                onBack = {},
                content =
                    ReviewEditScreenContent(
                        state =
                            ReviewEditUiState(
                                isLoading = false,
                                tasteOptions =
                                    TasteLevel.entries.map { level ->
                                        MasterOption(value = level.name, label = level.name)
                                    },
                            ),
                        onAction = {},
                        onSave = {},
                        viscosityOptions = emptyList(),
                        volumeShortcutOptions = emptyList(),
                        selectedSection = ReviewSection.TASTE,
                        onSectionSelected = {},
                    ),
            )
        }

        composeRule.onNodeWithText("短い").assertIsDisplayed()
        composeRule.onNodeWithText("長い").assertIsDisplayed()
    }

    @Test
    fun basicInfo_priceAndVolumeAreSideBySideWithUnits() {
        composeRule.setContent {
            ReviewEditScreen(
                onBack = {},
                content =
                    ReviewEditScreenContent(
                        state =
                            ReviewEditUiState(
                                isLoading = false,
                                price = "800",
                                volume = "180",
                            ),
                        onAction = {},
                        onSave = {},
                        viscosityOptions = emptyList(),
                        volumeShortcutOptions =
                            listOf(
                                MasterOption(value = "120", label = "グラス (120ml)").toDropdownOption(),
                                MasterOption(value = "180", label = "1合 (180ml)").toDropdownOption(),
                                MasterOption(value = "720", label = "四合瓶 (720ml)").toDropdownOption(),
                            ),
                        selectedSection = ReviewSection.BASIC,
                        onSectionSelected = {},
                    ),
            )
        }

        composeRule.onNodeWithText("円").assertIsDisplayed()
        composeRule.onNodeWithText("ml").assertIsDisplayed()
        composeRule.onNodeWithText("グラス (120ml)").assertIsDisplayed()
        val priceTop =
            composeRule
                .onNodeWithText("価格")
                .fetchSemanticsNode()
                .boundsInRoot.top
        val volumeTop =
            composeRule
                .onNodeWithText("量")
                .fetchSemanticsNode()
                .boundsInRoot.top
        assertEquals(priceTop, volumeTop)
    }
}

private fun MasterOption.toDropdownOption() =
    io.github.pyth0n14n.tastinggenie.ui.common.DropdownOption(
        value = value,
        label = label,
    )
