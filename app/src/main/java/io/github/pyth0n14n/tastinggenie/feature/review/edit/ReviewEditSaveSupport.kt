package io.github.pyth0n14n.tastinggenie.feature.review.edit

import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.ReviewInput
import io.github.pyth0n14n.tastinggenie.domain.model.UiError

fun ReviewEditUiState.toValidatedInput(): ReviewInput? {
    val currentSakeId = sakeId
    val parsedDate = date.toLocalDateOrNull()
    val parsedPrice = price.toOptionalInt()
    val parsedVolume = volume.toOptionalInt()
    val hasInvalidNumber = parsedPrice == INVALID_NUMBER || parsedVolume == INVALID_NUMBER
    val hasOutOfRangeNumber =
        parsedPrice.isOutOfReviewRange(ReviewValidationField.PRICE) ||
            parsedVolume.isOutOfReviewRange(ReviewValidationField.VOLUME)
    val canSave =
        currentSakeId != null &&
            parsedDate != null &&
            !hasInvalidNumber &&
            !hasOutOfRangeNumber

    return if (canSave) {
        ReviewInput(
            id = reviewId,
            sakeId = currentSakeId,
            date = parsedDate,
            bar = bar.trimmedOrNull(),
            price = parsedPrice,
            volume = parsedVolume,
            temperature = temperature,
            color = color,
            viscosity = viscosity,
            intensity = intensity,
            scentTop = scentTop,
            scentBase = scentBase,
            scentMouth = scentMouth,
            sweet = sweet,
            sour = sour,
            bitter = bitter,
            umami = umami,
            sharp = sharp,
            scene = scene.trimmedOrNull(),
            dish = dish.trimmedOrNull(),
            comment = comment.trimmedOrNull(),
            review = review,
        )
    } else {
        null
    }
}

fun ReviewEditUiState.withValidationFailure(snapshot: ReviewEditUiState): ReviewEditUiState {
    val validationErrors = snapshot.validationErrorsForSave()
    return copy(
        error =
            if (snapshot.sakeId == null && validationErrors.isEmpty()) {
                UiError(messageResId = R.string.error_invalid_review_input)
            } else {
                null
            },
        validationErrors = validationErrors,
        validationFailureCount = validationFailureCount + 1,
    )
}

private fun String.trimmedOrNull(): String? = trim().takeIf { it.isNotEmpty() }

private fun Int?.isOutOfReviewRange(field: ReviewValidationField): Boolean =
    this != null &&
        this != INVALID_NUMBER &&
        this !in requireNotNull(reviewValidationRange(field))
