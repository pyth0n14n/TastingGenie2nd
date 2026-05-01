package io.github.pyth0n14n.tastinggenie.feature.review.edit

import androidx.lifecycle.SavedStateHandle
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.AppSettings
import io.github.pyth0n14n.tastinggenie.domain.model.MasterDataBundle
import io.github.pyth0n14n.tastinggenie.domain.model.Review
import io.github.pyth0n14n.tastinggenie.domain.model.Sake
import io.github.pyth0n14n.tastinggenie.domain.model.UiError
import io.github.pyth0n14n.tastinggenie.domain.model.enums.TextureRoundness
import io.github.pyth0n14n.tastinggenie.navigation.AppDestination

data class ReviewEditArgs(
    val sakeId: Long,
    val reviewId: Long?,
)

data class ReviewSeedData(
    val master: MasterDataBundle,
    val sake: Sake?,
    val review: Review?,
    val settings: AppSettings,
)

fun SavedStateHandle.toReviewArgs(): ReviewEditArgs =
    ReviewEditArgs(
        sakeId = get<Long>(AppDestination.ARG_SAKE_ID) ?: AppDestination.NO_ID,
        reviewId = get<Long>(AppDestination.ARG_REVIEW_ID)?.takeIf { it != AppDestination.NO_ID },
    )

fun ReviewEditUiState.toMissingSakeState(): ReviewEditUiState =
    copy(
        isLoading = false,
        isSakeMissing = true,
        validationErrors = emptyMap(),
        error = UiError(messageResId = R.string.error_load_sake),
    )

fun ReviewEditUiState.toLoadedState(
    args: ReviewEditArgs,
    loaded: ReviewSeedData,
): ReviewEditUiState {
    val sake = loaded.sake
    val reviewId = args.reviewId
    val review = loaded.review
    return when {
        sake == null -> toMissingSakeWithId(args.sakeId)
        reviewId != null && (review == null || review.sakeId != args.sakeId) ->
            toMissingReviewState(
                sakeId = args.sakeId,
                sakeName = sake.name,
                master = loaded.master,
                settings = loaded.settings,
                reviewId = reviewId,
            )
        else ->
            toEditableLoadedState(
                sakeId = args.sakeId,
                sakeName = sake.name,
                review = review,
                master = loaded.master,
                settings = loaded.settings,
            )
    }
}

private fun ReviewEditUiState.toMissingSakeWithId(sakeId: Long): ReviewEditUiState =
    copy(
        isLoading = false,
        sakeId = sakeId,
        isSakeMissing = true,
        error =
            UiError(
                messageResId = R.string.error_load_sake,
                causeKey = sakeId.toString(),
            ),
    )

private fun ReviewEditUiState.toMissingReviewState(
    sakeId: Long,
    sakeName: String,
    master: MasterDataBundle,
    settings: AppSettings,
    reviewId: Long,
): ReviewEditUiState =
    copy(
        isLoading = false,
        sakeId = sakeId,
        sakeName = sakeName,
        showReviewSoundness = settings.showReviewSoundness,
        isEditTargetMissing = true,
        temperatureOptions = master.temperatures,
        colorOptions = master.colors,
        intensityOptions = master.intensityLevels,
        tasteOptions = master.tasteLevels,
        overallReviewOptions = master.overallReviews,
        aromaCategories = master.aromaCategories,
        validationErrors = emptyMap(),
        error =
            UiError(
                messageResId = R.string.error_load_review,
                causeKey = reviewId.toString(),
            ),
    )

private fun ReviewEditUiState.toEditableLoadedState(
    sakeId: Long,
    sakeName: String,
    review: Review?,
    master: MasterDataBundle,
    settings: AppSettings,
): ReviewEditUiState =
    copy(
        isLoading = false,
        sakeId = sakeId,
        reviewId = review?.id,
        sakeName = sakeName,
        showReviewSoundness = settings.showReviewSoundness,
        date = review?.date?.toString() ?: defaultReviewDateText(),
        bar = review?.bar.orEmpty(),
        price = review?.price?.toString().orEmpty(),
        volume = review?.volume?.toString().orEmpty(),
        aromaMainNote = review?.aromaMainNote.orEmpty(),
        tasteMainNote = review?.tasteMainNote.orEmpty(),
        otherIndividuality = review?.otherIndividuality.orEmpty(),
        otherCautions = review?.otherCautions.orEmpty(),
        scene = review?.scene.orEmpty(),
        dish = review?.dish.orEmpty(),
        comment = review?.otherFreeComment.orEmpty(),
        appearanceSoundness = review?.appearanceSoundness ?: appearanceSoundness,
        temperature = review?.temperature,
        color = review?.appearanceColor,
        viscosity = review?.appearanceViscosity,
        aromaSoundness = review?.aromaSoundness ?: aromaSoundness,
        intensity = review?.aromaIntensity,
        aromaComplexity = review?.aromaComplexity,
        tasteSoundness = review?.tasteSoundness ?: tasteSoundness,
        tasteAttack = review?.tasteAttack,
        tasteTextureRoundness = review?.tasteTextureRoundness.normalizeLegacyTextureRoundness(),
        tasteTextureSmoothness = review?.tasteTextureSmoothness,
        sweet = review?.tasteSweetness,
        sour = review?.tasteSourness,
        bitter = review?.tasteBitterness,
        umami = review?.tasteUmami,
        sharp = review?.tasteAftertaste,
        tasteComplexity = review?.tasteComplexity,
        review = review?.otherOverallReview,
        scentTop = review?.aromaExamples.orEmpty(),
        scentMouth = review?.tasteInPalateAroma.orEmpty(),
        temperatureOptions = master.temperatures,
        colorOptions = master.colors,
        intensityOptions = master.intensityLevels,
        tasteOptions = master.tasteLevels,
        overallReviewOptions = master.overallReviews,
        aromaCategories = master.aromaCategories,
        isSakeMissing = false,
        isEditTargetMissing = false,
        validationErrors = emptyMap(),
    )

private fun TextureRoundness?.normalizeLegacyTextureRoundness(): TextureRoundness? =
    when (this) {
        TextureRoundness.MELLOW -> TextureRoundness.SOFT
        else -> this
    }
