package io.github.pyth0n14n.tastinggenie.feature.review.edit

import io.github.pyth0n14n.tastinggenie.ui.common.FieldValidationError

private const val REVIEW_PRICE_MIN = 1
private const val REVIEW_PRICE_MAX = 1_000_000
private const val REVIEW_VOLUME_MIN = 1
private const val REVIEW_VOLUME_MAX = 25_000

fun ReviewEditUiState.validationErrorsForSave(): Map<ReviewValidationField, FieldValidationError> {
    val errors = mutableMapOf<ReviewValidationField, FieldValidationError>()
    if (date.toLocalDateOrNull() == null) {
        errors[ReviewValidationField.DATE] = FieldValidationError.INVALID_DATE
    }
    when (val parsedPrice = price.toOptionalInt()) {
        INVALID_NUMBER -> errors[ReviewValidationField.PRICE] = FieldValidationError.INVALID_NUMBER
        null -> Unit
        else ->
            if (parsedPrice !in REVIEW_PRICE_MIN..REVIEW_PRICE_MAX) {
                errors[ReviewValidationField.PRICE] = FieldValidationError.INVALID_INTEGER_RANGE
            }
    }
    when (val parsedVolume = volume.toOptionalInt()) {
        INVALID_NUMBER -> errors[ReviewValidationField.VOLUME] = FieldValidationError.INVALID_NUMBER
        null -> Unit
        else ->
            if (parsedVolume !in REVIEW_VOLUME_MIN..REVIEW_VOLUME_MAX) {
                errors[ReviewValidationField.VOLUME] = FieldValidationError.INVALID_INTEGER_RANGE
            }
    }
    return errors
}

fun ReviewEditUiState.clearValidationError(field: ReviewValidationField): ReviewEditUiState =
    copy(validationErrors = validationErrors - field)

fun reviewValidationRange(field: ReviewValidationField): IntRange? =
    when (field) {
        ReviewValidationField.PRICE -> REVIEW_PRICE_MIN..REVIEW_PRICE_MAX
        ReviewValidationField.VOLUME -> REVIEW_VOLUME_MIN..REVIEW_VOLUME_MAX
        ReviewValidationField.DATE -> null
    }
