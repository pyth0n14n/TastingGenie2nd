package io.github.pyth0n14n.tastinggenie.feature.sake.edit

import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeClassification
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeGrade
import io.github.pyth0n14n.tastinggenie.ui.common.FieldValidationError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SakeEditFormRowsTest {
    companion object {
        private const val ALCOHOL_INDEX_WITHOUT_OPTIONAL_ROWS = 15
        private const val KAKE_POLISH_INDEX_WITH_OPTIONAL_ROWS = 13
        private const val FIRST_INVALID_INDEX_WITHOUT_OPTIONAL_ROWS = 9
        private const val GRADE_INDEX_WITH_OTHER = 2
        private const val GRADE_OTHER_INDEX = 3
        private const val IMAGE_INDEX_WITH_GRADE_OTHER = 4
        private const val CLASSIFICATION_INDEX_WITH_OTHER = 4
        private const val CLASSIFICATION_OTHER_INDEX = 5
        private const val MAKER_INDEX_WITH_CLASSIFICATION_OTHER = 6
    }

    @Test
    fun firstInvalidFieldIndex_withoutOptionalRows_tracksCurrentMetadataOrder() {
        val state =
            SakeEditUiState(
                validationErrors = mapOf(SakeValidationField.ALCOHOL to FieldValidationError.INVALID_NUMBER),
            )

        assertEquals(ALCOHOL_INDEX_WITHOUT_OPTIONAL_ROWS, state.firstInvalidFieldIndex())
    }

    @Test
    fun firstInvalidFieldIndex_withOptionalRows_accountsForInsertedItems() {
        val state =
            SakeEditUiState(
                grade = SakeGrade.OTHER,
                classifications = listOf(SakeClassification.OTHER),
                validationErrors = mapOf(SakeValidationField.KAKE_POLISH to FieldValidationError.INVALID_PERCENTAGE),
            )

        assertEquals(KAKE_POLISH_INDEX_WITH_OPTIONAL_ROWS, state.firstInvalidFieldIndex())
    }

    @Test
    fun firstInvalidFieldIndex_prefersFirstVisibleInvalidField() {
        val state =
            SakeEditUiState(
                validationErrors =
                    linkedMapOf(
                        SakeValidationField.KOJI_POLISH to FieldValidationError.INVALID_PERCENTAGE,
                        SakeValidationField.ALCOHOL to FieldValidationError.INVALID_NUMBER,
                    ),
            )

        assertEquals(FIRST_INVALID_INDEX_WITHOUT_OPTIONAL_ROWS, state.firstInvalidFieldIndex())
    }

    @Test
    fun firstInvalidFieldIndex_withoutValidationErrors_returnsNull() {
        assertNull(SakeEditUiState().firstInvalidFieldIndex())
    }

    @Test
    fun firstInvalidSectionIndex_mapsBasicValidationErrorsToBasicSection() {
        val state =
            SakeEditUiState(
                validationErrors = mapOf(SakeValidationField.NAME to FieldValidationError.REQUIRED),
            )

        assertEquals(1, state.firstInvalidSectionIndex())
    }

    @Test
    fun firstInvalidSectionIndex_mapsMetadataValidationErrorsToDetailSection() {
        val state =
            SakeEditUiState(
                validationErrors = mapOf(SakeValidationField.ALCOHOL to FieldValidationError.INVALID_NUMBER),
            )

        assertEquals(2, state.firstInvalidSectionIndex())
    }

    @Test
    fun visibleSakeEditRowKeys_placesGradeOtherImmediatelyAfterGrade() {
        val rowKeys =
            SakeEditUiState(
                grade = SakeGrade.OTHER,
            ).visibleSakeEditRowKeys()

        assertEquals(SAKE_ROW_GRADE, rowKeys[GRADE_INDEX_WITH_OTHER])
        assertEquals(SAKE_ROW_GRADE_OTHER, rowKeys[GRADE_OTHER_INDEX])
        assertEquals(SAKE_ROW_IMAGE, rowKeys[IMAGE_INDEX_WITH_GRADE_OTHER])
    }

    @Test
    fun visibleSakeEditRowKeys_placesClassificationOtherImmediatelyAfterClassification() {
        val rowKeys =
            SakeEditUiState(
                classifications = listOf(SakeClassification.OTHER),
            ).visibleSakeEditRowKeys()

        assertEquals(SAKE_ROW_CLASSIFICATION, rowKeys[CLASSIFICATION_INDEX_WITH_OTHER])
        assertEquals(SAKE_ROW_CLASSIFICATION_OTHER, rowKeys[CLASSIFICATION_OTHER_INDEX])
        assertEquals(SAKE_ROW_MAKER, rowKeys[MAKER_INDEX_WITH_CLASSIFICATION_OTHER])
    }
}
