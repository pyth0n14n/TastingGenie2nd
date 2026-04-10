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

    return if (currentSakeId == null || parsedDate == null || hasInvalidNumber) {
        null
    } else {
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
    )
}

private fun String.trimmedOrNull(): String? = trim().takeIf { it.isNotEmpty() }
