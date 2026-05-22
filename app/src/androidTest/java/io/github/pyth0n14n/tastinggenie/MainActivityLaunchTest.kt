package io.github.pyth0n14n.tastinggenie

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityLaunchTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    @Before
    fun clearDatabase() {
        targetContext().deleteDatabase("tasting_genie.db")
    }

    @Test
    fun firstLaunch_showsEmptySakeList() {
        ActivityScenario.launch(MainActivity::class.java).use {
            composeRule.onNodeWithText("ききさけ帖").assertIsDisplayed()
            composeRule.waitUntil(timeoutMillis = 5_000) {
                composeRule.onAllNodesWithText("登録された酒がありません").fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onNodeWithText("登録された酒がありません").assertIsDisplayed()
        }
    }

    private fun targetContext(): Context = InstrumentationRegistry.getInstrumentation().targetContext
}
