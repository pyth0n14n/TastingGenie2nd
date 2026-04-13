package io.github.pyth0n14n.tastinggenie.feature.review.image

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.util.Base64

class ReviewImageScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun dummyImageUri_rendersImageViewerContent() {
        val imageUri = createDummyImageFile().toURI().toString()

        composeRule.setContent {
            ReviewImageScreen(
                state =
                    ReviewImageUiState(
                        isLoading = false,
                        imageUri = imageUri,
                    ),
                onBack = {},
            )
        }

        org.junit.Assert.assertEquals(0, composeRule.onAllNodesWithText("画像が登録されていません").fetchSemanticsNodes().size)
        composeRule.onNodeWithContentDescription("レビュー画像").assertIsDisplayed()
    }

    /**
     * Creates a tiny real PNG file so the viewer test uses the same file-URI path as production.
     */
    private fun createDummyImageFile(): File {
        val imageBytes =
            Base64.getDecoder().decode(
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAusB9Y9l9WQAAAAASUVORK5CYII=",
            )
        return File(composeRule.activity.cacheDir, "review-image-test.png").apply {
            writeBytes(imageBytes)
        }
    }
}
