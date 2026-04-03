package io.github.pyth0n14n.tastinggenie.feature.review.edit

import androidx.lifecycle.SavedStateHandle
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.MasterDataBundle
import io.github.pyth0n14n.tastinggenie.domain.model.Review
import io.github.pyth0n14n.tastinggenie.domain.model.Sake
import io.github.pyth0n14n.tastinggenie.domain.model.UiError
import io.github.pyth0n14n.tastinggenie.navigation.AppDestination

data class ReviewEditArgs(
    val sakeId: Long,
    val reviewId: Long?,
)

data class ReviewSeedData(
    val master: MasterDataBundle,
    val sake: Sake?,
    val review: Review?,
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
                reviewId = reviewId,
            )
        else ->
            toEditableLoadedState(
                sakeId = args.sakeId,
                sakeName = sake.name,
                review = review,
                master = loaded.master,
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
    reviewId: Long,
): ReviewEditUiState =
    copy(
        isLoading = false,
        sakeId = sakeId,
        sakeName = sakeName,
        isEditTargetMissing = true,
        temperatureOptions = master.temperatures,
        colorOptions = master.colors,
        intensityOptions = master.intensityLevels,
        tasteOptions = master.tasteLevels,
        overallReviewOptions = master.overallReviews,
        aromaCategories = master.aromaCategories,
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
): ReviewEditUiState =
    copy(
        isLoading = false,
        sakeId = sakeId,
        reviewId = review?.id,
        sakeName = sakeName,
        date = review?.date?.toString() ?: defaultReviewDateText(),
        bar = review?.bar.orEmpty(),
        price = review?.price?.toString().orEmpty(),
        volume = review?.volume?.toString().orEmpty(),
        scene = review?.scene.orEmpty(),
        dish = review?.dish.orEmpty(),
        comment = review?.comment.orEmpty(),
        temperature = review?.temperature,
        color = review?.color,
        viscosity = review?.viscosity,
        intensity = review?.intensity,
        sweet = review?.sweet,
        sour = review?.sour,
        bitter = review?.bitter,
        umami = review?.umami,
        sharp = review?.sharp,
        review = review?.review,
        scentTop = review?.scentTop.orEmpty(),
        scentBase = review?.scentBase.orEmpty(),
        scentMouth = review?.scentMouth.orEmpty(),
        temperatureOptions = master.temperatures,
        colorOptions = master.colors,
        intensityOptions = master.intensityLevels,
        tasteOptions = master.tasteLevels,
        overallReviewOptions = master.overallReviews,
        aromaCategories = master.aromaCategories,
        isSakeMissing = false,
        isEditTargetMissing = false,
    )
