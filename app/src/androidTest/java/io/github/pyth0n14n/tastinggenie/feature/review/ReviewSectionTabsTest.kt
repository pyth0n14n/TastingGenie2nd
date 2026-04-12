package io.github.pyth0n14n.tastinggenie.feature.review

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertExists
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

        composeRule.onNodeWithContentDescription("基本情報").assertExists()
        composeRule.onNodeWithContentDescription("外観").assertExists()
        composeRule.onNodeWithContentDescription("香り").assertExists()
        composeRule.onNodeWithContentDescription("味わい").assertExists()
        composeRule.onNodeWithContentDescription("その他").assertExists()

        composeRule.onNodeWithContentDescription("香り").performClick()
        composeRule.runOnIdle {
            assertEquals(ReviewSection.AROMA, selected)
        }
    }
}
