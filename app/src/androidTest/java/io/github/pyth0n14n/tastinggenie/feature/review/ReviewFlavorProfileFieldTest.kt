package io.github.pyth0n14n.tastinggenie.feature.review

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import io.github.pyth0n14n.tastinggenie.domain.model.enums.ComplexityLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.IntensityLevel
import org.junit.Rule
import org.junit.Test

class ReviewFlavorProfileFieldTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun selectedCell_exposesReadableContentDescription() {
        composeRule.setContent {
            ReviewFlavorProfileField(
                intensity = IntensityLevel.STRONG,
                complexity = ComplexityLevel.SLIGHTLY_COMPLEX,
                onSelectionChanged = {},
            )
        }

        composeRule
            .onNodeWithContentDescription("香り 強い、味わい やや複雑")
            .assertIsDisplayed()
            .assertIsSelected()
    }
}
