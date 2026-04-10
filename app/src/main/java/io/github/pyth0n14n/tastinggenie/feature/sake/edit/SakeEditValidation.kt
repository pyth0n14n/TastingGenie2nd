package io.github.pyth0n14n.tastinggenie.feature.sake.edit

import io.github.pyth0n14n.tastinggenie.domain.model.SakeInput
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeClassification
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeGrade
import io.github.pyth0n14n.tastinggenie.ui.common.FieldValidationError

private const val MIN_POLISH_RATIO = 0
private const val MAX_POLISH_RATIO = 100

private data class ParsedNumber<T>(
    val isValid: Boolean,
    val value: T?,
)

private data class ParsedSakeNumbers(
    val alcohol: Int?,
    val kojiPolish: Int?,
    val kakePolish: Int?,
    val sakeDegree: Float?,
    val acidity: Float?,
)

fun SakeEditUiState.toValidatedInput(): SakeInput? {
    val currentGrade = grade
    val parsedNumbers = parseSakeNumbers()
    if (currentGrade == null || name.isBlank() || parsedNumbers == null) return null

    return SakeInput(
        id = sakeId,
        name = name.trim(),
        grade = currentGrade,
        gradeOther = gradeOther.normalizedOrNull()?.takeIf { currentGrade == SakeGrade.OTHER },
        type = classifications,
        typeOther = typeOther.normalizedOrNull()?.takeIf { classifications.contains(SakeClassification.OTHER) },
        maker = maker.normalizedOrNull(),
        prefecture = prefecture,
        alcohol = parsedNumbers.alcohol,
        kojiMai = kojiMai.normalizedOrNull(),
        kojiPolish = parsedNumbers.kojiPolish,
        kakeMai = kakeMai.normalizedOrNull(),
        kakePolish = parsedNumbers.kakePolish,
        sakeDegree = parsedNumbers.sakeDegree,
        acidity = parsedNumbers.acidity,
        yeast = yeast.normalizedOrNull(),
        water = water.normalizedOrNull(),
    )
}

fun SakeEditUiState.validationErrorsForSave(): Map<SakeValidationField, FieldValidationError> {
    val errors = mutableMapOf<SakeValidationField, FieldValidationError>()
    if (name.isBlank()) {
        errors[SakeValidationField.NAME] = FieldValidationError.REQUIRED
    }
    if (grade == null) {
        errors[SakeValidationField.GRADE] = FieldValidationError.REQUIRED_SELECTION
    }
    if (!alcohol.parseOptionalInt().isValid) {
        errors[SakeValidationField.ALCOHOL] = FieldValidationError.INVALID_NUMBER
    }
    if (!kojiPolish.parseOptionalPercentage().isValid) {
        errors[SakeValidationField.KOJI_POLISH] = FieldValidationError.INVALID_PERCENTAGE
    }
    if (!kakePolish.parseOptionalPercentage().isValid) {
        errors[SakeValidationField.KAKE_POLISH] = FieldValidationError.INVALID_PERCENTAGE
    }
    if (!sakeDegree.parseOptionalFloat().isValid) {
        errors[SakeValidationField.SAKE_DEGREE] = FieldValidationError.INVALID_NUMBER
    }
    if (!acidity.parseOptionalFloat().isValid) {
        errors[SakeValidationField.ACIDITY] = FieldValidationError.INVALID_NUMBER
    }
    return errors
}

fun SakeEditUiState.clearValidationError(field: SakeValidationField?): SakeEditUiState =
    copy(
        error = null,
        validationErrors =
            if (field == null) {
                validationErrors
            } else {
                validationErrors - field
            },
    )

fun SakeTextField.toValidationField(): SakeValidationField? =
    when (this) {
        SakeTextField.NAME -> SakeValidationField.NAME
        SakeTextField.SAKE_DEGREE -> SakeValidationField.SAKE_DEGREE
        SakeTextField.ACIDITY -> SakeValidationField.ACIDITY
        SakeTextField.KOJI_POLISH -> SakeValidationField.KOJI_POLISH
        SakeTextField.KAKE_POLISH -> SakeValidationField.KAKE_POLISH
        SakeTextField.ALCOHOL -> SakeValidationField.ALCOHOL
        SakeTextField.GRADE_OTHER,
        SakeTextField.TYPE_OTHER,
        SakeTextField.MAKER,
        SakeTextField.KOJI_MAI,
        SakeTextField.KAKE_MAI,
        SakeTextField.YEAST,
        SakeTextField.WATER,
        -> null
    }

private fun String.normalizedOrNull(): String? = trim().takeIf { value -> value.isNotEmpty() }

private fun String.parseOptionalInt(): ParsedNumber<Int> {
    val normalized = trim()
    return when {
        normalized.isEmpty() -> ParsedNumber(isValid = true, value = null)
        else -> ParsedNumber(isValid = normalized.toIntOrNull() != null, value = normalized.toIntOrNull())
    }
}

private fun String.parseOptionalPercentage(): ParsedNumber<Int> {
    val parsed = parseOptionalInt()
    val isInRange = parsed.value == null || parsed.value in MIN_POLISH_RATIO..MAX_POLISH_RATIO
    return ParsedNumber(
        isValid = parsed.isValid && isInRange,
        value = parsed.value,
    )
}

private fun String.parseOptionalFloat(): ParsedNumber<Float> {
    val normalized = trim()
    return when {
        normalized.isEmpty() -> ParsedNumber(isValid = true, value = null)
        else -> {
            val parsed = normalized.toFloatOrNull()
            ParsedNumber(
                isValid = parsed?.isFinite() == true,
                value = parsed?.takeIf { it.isFinite() },
            )
        }
    }
}

private fun SakeEditUiState.parseSakeNumbers(): ParsedSakeNumbers? {
    val alcohol = alcohol.parseOptionalInt()
    val kojiPolish = kojiPolish.parseOptionalPercentage()
    val kakePolish = kakePolish.parseOptionalPercentage()
    val sakeDegree = sakeDegree.parseOptionalFloat()
    val acidity = acidity.parseOptionalFloat()
    val allNumbersValid =
        alcohol.isValid &&
            kojiPolish.isValid &&
            kakePolish.isValid &&
            sakeDegree.isValid &&
            acidity.isValid
    return if (allNumbersValid) {
        ParsedSakeNumbers(
            alcohol = alcohol.value,
            kojiPolish = kojiPolish.value,
            kakePolish = kakePolish.value,
            sakeDegree = sakeDegree.value,
            acidity = acidity.value,
        )
    } else {
        null
    }
}
