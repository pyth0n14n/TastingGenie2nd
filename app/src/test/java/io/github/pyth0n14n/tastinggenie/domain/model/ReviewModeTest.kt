package io.github.pyth0n14n.tastinggenie.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewModeTest {
    @Test
    fun normalMode_includesSoundnessItems() {
        val normalMode = builtInReviewModeDefinitions.single { it.id == ReviewMode.NORMAL.id }

        assertTrue(ReviewItemId.APPEARANCE_SOUNDNESS in normalMode.enabledItemIds)
        assertTrue(ReviewItemId.AROMA_SOUNDNESS in normalMode.enabledItemIds)
        assertTrue(ReviewItemId.TASTE_SOUNDNESS in normalMode.enabledItemIds)
    }

    @Test
    fun debugMode_enablesAllReviewItems() {
        val debugMode = builtInReviewModeDefinitions.single { it.id == ReviewMode.DEBUG.id }

        assertEquals("デバッグ", debugMode.label)
        assertEquals(ReviewItemId.entries.toSet(), debugMode.enabledItemIds)
        assertTrue(debugMode.isBuiltIn)
    }
}
