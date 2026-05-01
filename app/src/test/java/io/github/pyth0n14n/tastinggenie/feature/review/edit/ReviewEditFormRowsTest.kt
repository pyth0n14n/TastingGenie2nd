package io.github.pyth0n14n.tastinggenie.feature.review.edit

import io.github.pyth0n14n.tastinggenie.ui.common.FieldValidationError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReviewEditFormRowsTest {
    companion object {
        private const val DATE_INDEX = 1
        private const val PRICE_AND_VOLUME_INDEX = 2
    }

    @Test
    fun firstInvalidFieldIndex_pointsToDateRow() {
        val state =
            ReviewEditUiState(
                validationErrors = mapOf(ReviewValidationField.DATE to FieldValidationError.INVALID_DATE),
            )

        assertEquals(DATE_INDEX, state.firstInvalidFieldIndex())
    }

    @Test
    fun firstInvalidFieldIndex_pointsToPriceRow() {
        val state =
            ReviewEditUiState(
                validationErrors = mapOf(ReviewValidationField.PRICE to FieldValidationError.INVALID_NUMBER),
            )

        assertEquals(PRICE_AND_VOLUME_INDEX, state.firstInvalidFieldIndex())
    }

    @Test
    fun firstInvalidFieldIndex_pointsToVolumeRow() {
        val state =
            ReviewEditUiState(
                validationErrors = mapOf(ReviewValidationField.VOLUME to FieldValidationError.INVALID_NUMBER),
            )

        assertEquals(PRICE_AND_VOLUME_INDEX, state.firstInvalidFieldIndex())
    }

    @Test
    fun firstInvalidFieldIndex_withoutSupportedErrors_returnsNull() {
        assertNull(ReviewEditUiState().firstInvalidFieldIndex())
    }
}
