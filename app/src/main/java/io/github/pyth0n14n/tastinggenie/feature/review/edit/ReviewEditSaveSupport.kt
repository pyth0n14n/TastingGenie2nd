package io.github.pyth0n14n.tastinggenie.feature.review.edit

import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.ReviewInput
import io.github.pyth0n14n.tastinggenie.domain.model.ReviewItemId
import io.github.pyth0n14n.tastinggenie.domain.model.UiError
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeColor

@Suppress("CyclomaticComplexMethod", "LongMethod")
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
            bar = bar.trimmedOrNull().takeIf { isItemEnabled(ReviewItemId.BAR) },
            price = parsedPrice.takeIf { isItemEnabled(ReviewItemId.PRICE) },
            volume = parsedVolume.takeIf { isItemEnabled(ReviewItemId.VOLUME) },
            temperature = temperature.takeIf { isItemEnabled(ReviewItemId.TEMPERATURE) },
            appearanceSoundness = appearanceSoundness,
            appearanceColor = color.takeIf { isItemEnabled(ReviewItemId.APPEARANCE_COLOR) },
            appearanceColorOther =
                colorOther.trimmedOrNull().takeIf {
                    isItemEnabled(ReviewItemId.APPEARANCE_COLOR) && color == SakeColor.OTHER
                },
            appearanceViscosity = viscosity.takeIf { isItemEnabled(ReviewItemId.APPEARANCE_VISCOSITY) },
            aromaSoundness = aromaSoundness,
            aromaIntensity = intensity.takeIf { isItemEnabled(ReviewItemId.AROMA_INTENSITY) },
            aromaExamples = scentTop.takeIf { isItemEnabled(ReviewItemId.AROMA_EXAMPLES) } ?: emptyList(),
            aromaMainNote = aromaMainNote.trimmedOrNull().takeIf { isItemEnabled(ReviewItemId.AROMA_MAIN_NOTE) },
            aromaComplexity = aromaComplexity.takeIf { isItemEnabled(ReviewItemId.AROMA_COMPLEXITY) },
            tasteSoundness = tasteSoundness,
            tasteAttack = tasteAttack.takeIf { isItemEnabled(ReviewItemId.TASTE_ATTACK) },
            tasteTextureRoundness =
                tasteTextureRoundness.takeIf { isItemEnabled(ReviewItemId.TASTE_TEXTURE_ROUNDNESS) },
            tasteTextureSmoothness =
                tasteTextureSmoothness.takeIf { isItemEnabled(ReviewItemId.TASTE_TEXTURE_SMOOTHNESS) },
            tasteTextureNote =
                tasteTextureNote.trimmedOrNull().takeIf { isItemEnabled(ReviewItemId.TASTE_TEXTURE_NOTE) },
            tasteDescription = tasteMainNote.trimmedOrNull().takeIf { isItemEnabled(ReviewItemId.TASTE_DESCRIPTION) },
            tasteSweetDryness = tasteSweetDryness.takeIf { isItemEnabled(ReviewItemId.TASTE_SWEET_DRYNESS) },
            tasteInPalateAromaIntensity =
                tasteInPalateAromaIntensity.takeIf { isItemEnabled(ReviewItemId.TASTE_IN_PALATE_AROMA_INTENSITY) },
            tasteSweetness = sweet.takeIf { isItemEnabled(ReviewItemId.TASTE_SWEETNESS) },
            tasteSourness = sour.takeIf { isItemEnabled(ReviewItemId.TASTE_SOURNESS) },
            tasteBitterness = bitter.takeIf { isItemEnabled(ReviewItemId.TASTE_BITTERNESS) },
            tasteUmami = umami.takeIf { isItemEnabled(ReviewItemId.TASTE_UMAMI) },
            tasteInPalateAroma =
                scentMouth.takeIf { isItemEnabled(ReviewItemId.TASTE_IN_PALATE_AROMA_EXAMPLES) } ?: emptyList(),
            tasteAftertaste = sharp.takeIf { isItemEnabled(ReviewItemId.TASTE_AFTERTASTE_LENGTH) },
            tasteAftertasteNote =
                tasteAftertasteNote.trimmedOrNull().takeIf { isItemEnabled(ReviewItemId.TASTE_AFTERTASTE_NOTE) },
            tasteComplexity = tasteComplexity.takeIf { isItemEnabled(ReviewItemId.TASTE_COMPLEXITY) },
            otherIndividuality =
                otherIndividuality.trimmedOrNull().takeIf { isItemEnabled(ReviewItemId.OTHER_INDIVIDUALITY) },
            otherCautions = otherCautions.trimmedOrNull().takeIf { isItemEnabled(ReviewItemId.OTHER_CAUTIONS) },
            otherSakeTypes = otherSakeTypes.takeIf { isItemEnabled(ReviewItemId.OTHER_SAKE_TYPES) } ?: emptyList(),
            otherFreeComment = comment.trimmedOrNull().takeIf { isItemEnabled(ReviewItemId.OTHER_FREE_COMMENT) },
            otherOverallReview = review.takeIf { isItemEnabled(ReviewItemId.OTHER_OVERALL_REVIEW) },
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
