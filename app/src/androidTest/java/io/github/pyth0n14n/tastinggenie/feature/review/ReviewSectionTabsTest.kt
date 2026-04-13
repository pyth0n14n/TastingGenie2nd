package io.github.pyth0n14n.tastinggenie.feature.review

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ReviewSectionTabsTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun tabsExposeSectionLabelsAndSelectionCallbacks() {
        var selected = ReviewSection.BASIC
        composeRule.setContent {
            ReviewSectionTabs(
                selectedSection = selected,
                onSectionSelected = { next -> selected = next },
            )
        }

        composeRule.onNodeWithContentDescription("基本情報").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("外観").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("香り").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("味わい").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("その他").assertIsDisplayed()

        composeRule.onNodeWithContentDescription("香り").performClick()
        composeRule.runOnIdle {
            assertEquals(ReviewSection.AROMA, selected)
        }
    }
}
