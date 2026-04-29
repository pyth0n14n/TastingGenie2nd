package io.github.pyth0n14n.tastinggenie.feature.sake.edit

import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeGrade
import io.github.pyth0n14n.tastinggenie.ui.common.FieldValidationError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SakeEditAminoFieldTest {
    private companion object {
        const val AMINO_VALUE = 1.2F
    }

    @Test
    fun toValidatedInput_withAmino_persistsAmino() {
        val input =
            SakeEditUiState(
                name = "保存テスト",
                grade = SakeGrade.JUNMAI,
                amino = AMINO_VALUE.toString(),
            ).toValidatedInput()

        assertEquals(AMINO_VALUE, input?.amino)
    }

    @Test
    fun validationErrorsForSave_withInvalidAmino_returnsInvalidNumber() {
        val state =
            SakeEditUiState(
                name = "保存テスト",
                grade = SakeGrade.JUNMAI,
                amino = "abc",
            )

        assertEquals(
            FieldValidationError.INVALID_NUMBER,
            state.validationErrorsForSave()[SakeValidationField.AMINO],
        )
        assertNull(state.toValidatedInput())
    }
}
