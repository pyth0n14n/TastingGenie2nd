package io.github.pyth0n14n.tastinggenie.feature.review.edit

import io.github.pyth0n14n.tastinggenie.ui.common.FieldValidationError

fun ReviewEditUiState.validationErrorsForSave(): Map<ReviewValidationField, FieldValidationError> {
    val errors = mutableMapOf<ReviewValidationField, FieldValidationError>()
    if (date.toLocalDateOrNull() == null) {
        errors[ReviewValidationField.DATE] = FieldValidationError.INVALID_DATE
    }
    if (price.toOptionalInt() == INVALID_NUMBER) {
        errors[ReviewValidationField.PRICE] = FieldValidationError.INVALID_NUMBER
    }
    if (volume.toOptionalInt() == INVALID_NUMBER) {
        errors[ReviewValidationField.VOLUME] = FieldValidationError.INVALID_NUMBER
    }
    return errors
}

fun ReviewEditUiState.clearValidationError(field: ReviewValidationField): ReviewEditUiState =
    copy(validationErrors = validationErrors - field)
