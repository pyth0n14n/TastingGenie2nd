package io.github.pyth0n14n.tastinggenie.feature.review.edit

import io.github.pyth0n14n.tastinggenie.domain.model.AromaCategoryMaster
import io.github.pyth0n14n.tastinggenie.domain.model.MasterOption
import io.github.pyth0n14n.tastinggenie.domain.model.ReviewId
import io.github.pyth0n14n.tastinggenie.domain.model.SakeId
import io.github.pyth0n14n.tastinggenie.domain.model.UiError
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Aroma
import io.github.pyth0n14n.tastinggenie.domain.model.enums.AttackLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.ComplexityLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.IntensityLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.OverallReview
import io.github.pyth0n14n.tastinggenie.domain.model.enums.ReviewSoundness
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeColor
import io.github.pyth0n14n.tastinggenie.domain.model.enums.TasteLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Temperature
import io.github.pyth0n14n.tastinggenie.domain.model.enums.TextureRoundness
import io.github.pyth0n14n.tastinggenie.domain.model.enums.TextureSmoothness
import io.github.pyth0n14n.tastinggenie.ui.common.FieldValidationError

data class ReviewEditUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val isSakeMissing: Boolean = false,
    val isEditTargetMissing: Boolean = false,
    val error: UiError? = null,
    val validationErrors: Map<ReviewValidationField, FieldValidationError> = emptyMap(),
    val validationFailureCount: Int = 0,
    val sakeId: SakeId? = null,
    val reviewId: ReviewId? = null,
    val sakeName: String = "",
    val date: String = defaultReviewDateText(),
    val bar: String = "",
    val price: String = "",
    val volume: String = "",
    val aromaMainNote: String = "",
    val tasteMainNote: String = "",
    val otherIndividuality: String = "",
    val scene: String = "",
    val dish: String = "",
    val comment: String = "",
    val appearanceSoundness: ReviewSoundness = ReviewSoundness.SOUND,
    val temperature: Temperature? = null,
    val color: SakeColor? = null,
    val viscosity: Int? = null,
    val aromaSoundness: ReviewSoundness = ReviewSoundness.SOUND,
    val intensity: IntensityLevel? = null,
    val aromaComplexity: ComplexityLevel? = null,
    val tasteSoundness: ReviewSoundness = ReviewSoundness.SOUND,
    val tasteAttack: AttackLevel? = null,
    val tasteTextureRoundness: TextureRoundness? = null,
    val tasteTextureSmoothness: TextureSmoothness? = null,
    val sweet: TasteLevel? = null,
    val sour: TasteLevel? = null,
    val bitter: TasteLevel? = null,
    val umami: TasteLevel? = null,
    val sharp: TasteLevel? = null,
    val tasteComplexity: ComplexityLevel? = null,
    val review: OverallReview? = null,
    val scentTop: List<Aroma> = emptyList(),
    val scentMouth: List<Aroma> = emptyList(),
    val temperatureOptions: List<MasterOption> = emptyList(),
    val colorOptions: List<MasterOption> = emptyList(),
    val intensityOptions: List<MasterOption> = emptyList(),
    val tasteOptions: List<MasterOption> = emptyList(),
    val overallReviewOptions: List<MasterOption> = emptyList(),
    val aromaCategories: List<AromaCategoryMaster> = emptyList(),
) {
    val isInputLocked: Boolean
        get() = isSakeMissing || isEditTargetMissing
}
