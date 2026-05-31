package io.github.pyth0n14n.tastinggenie.feature.onboarding

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.pressBack
import io.github.pyth0n14n.tastinggenie.R
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class OnboardingScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun nextButton_movesThroughPagesAndFinalCtaCreatesSake() {
        var createCalled = false
        composeRule.setContent {
            OnboardingScreen(
                pages = testPages(),
                onSkip = {},
                onCreateSake = { createCalled = true },
            )
        }

        composeRule.onNodeWithText("まずは酒を登録").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("オンボーディング 1 / 3").assertIsDisplayed()

        composeRule.onNodeWithText("次へ").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("飲んだらレビュー").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("オンボーディング 2 / 3").assertIsDisplayed()

        composeRule.onNodeWithText("次へ").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("感じたことを選んで記録").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("オンボーディング 3 / 3").assertIsDisplayed()
        composeRule.onNodeWithText("スキップ").assertDoesNotExist()

        composeRule.onNodeWithText("酒を登録する").performClick()
        composeRule.runOnIdle { assertTrue(createCalled) }
    }

    @Test
    fun skipButton_callsSkipBeforeLastPage() {
        var skipped = false
        composeRule.setContent {
            OnboardingScreen(
                pages = testPages(),
                onSkip = { skipped = true },
                onCreateSake = {},
            )
        }

        composeRule.onNodeWithText("スキップ").performClick()

        composeRule.runOnIdle { assertTrue(skipped) }
    }

    @Test
    fun backButton_returnsToPreviousPageAndIsDisabledOnFirstPage() {
        composeRule.setContent {
            OnboardingScreen(
                pages = testPages(),
                onSkip = {},
                onCreateSake = {},
            )
        }

        pressBack()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("まずは酒を登録").assertIsDisplayed()

        composeRule.onNodeWithText("次へ").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("飲んだらレビュー").assertIsDisplayed()

        pressBack()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("まずは酒を登録").assertIsDisplayed()
    }

    @Test
    fun firstPageBackDoesNotCallActions() {
        var skipped = false
        var created = false
        composeRule.setContent {
            OnboardingScreen(
                pages = testPages(),
                onSkip = { skipped = true },
                onCreateSake = { created = true },
            )
        }

        pressBack()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("まずは酒を登録").assertIsDisplayed()
        composeRule.runOnIdle {
            assertFalse(skipped)
            assertFalse(created)
        }
    }
}

private fun testPages(): List<OnboardingPage> =
    listOf(
        OnboardingPage(
            titleResId = R.string.onboarding_page_one_title,
            messageResId = R.string.onboarding_page_one_message,
        ),
        OnboardingPage(
            titleResId = R.string.onboarding_page_two_title,
            messageResId = R.string.onboarding_page_two_message,
        ),
        OnboardingPage(
            titleResId = R.string.onboarding_page_three_title,
            messageResId = R.string.onboarding_page_three_message,
        ),
    )
