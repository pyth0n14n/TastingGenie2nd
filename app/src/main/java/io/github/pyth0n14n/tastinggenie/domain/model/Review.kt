package io.github.pyth0n14n.tastinggenie.domain.model

import io.github.pyth0n14n.tastinggenie.domain.model.enums.Aroma
import io.github.pyth0n14n.tastinggenie.domain.model.enums.AttackLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.ComplexityLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.FlavorProfileType
import io.github.pyth0n14n.tastinggenie.domain.model.enums.FoodCompatibility
import io.github.pyth0n14n.tastinggenie.domain.model.enums.IntensityLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.OverallReview
import io.github.pyth0n14n.tastinggenie.domain.model.enums.ReviewSoundness
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeColor
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SweetDryness
import io.github.pyth0n14n.tastinggenie.domain.model.enums.TasteLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Temperature
import io.github.pyth0n14n.tastinggenie.domain.model.enums.TextureRoundness
import io.github.pyth0n14n.tastinggenie.domain.model.enums.TextureSmoothness
import java.time.LocalDate

/**
 * レビューの動的情報。
 */
data class Review(
    val id: ReviewId,
    val sakeId: SakeId,
    val date: LocalDate,
    val bar: String? = null,
    val price: Int? = null,
    val volume: Int? = null,
    val temperature: Temperature? = null,
    val dish: String? = null,
    val foodCompatibility: FoodCompatibility? = null,
    val appearanceSoundness: ReviewSoundness? = null,
    val appearanceColor: SakeColor? = null,
    val appearanceColorOther: String? = null,
    val appearanceViscosity: Int? = null,
    val aromaSoundness: ReviewSoundness? = null,
    val aromaIntensity: IntensityLevel? = null,
    val aromaExamples: List<Aroma> = emptyList(),
    val aromaMainNote: String? = null,
    val aromaComplexity: ComplexityLevel? = null,
    val tasteSoundness: ReviewSoundness? = null,
    val tasteAttack: AttackLevel? = null,
    val tasteTextureRoundness: TextureRoundness? = null,
    val tasteTextureSmoothness: TextureSmoothness? = null,
    val tasteTextureNote: String? = null,
    val tasteSweetness: TasteLevel? = null,
    val tasteSourness: TasteLevel? = null,
    val tasteBitterness: TasteLevel? = null,
    val tasteUmami: TasteLevel? = null,
    val tasteDescription: String? = null,
    val tasteSweetDryness: SweetDryness? = null,
    val tasteInPalateAromaIntensity: IntensityLevel? = null,
    val tasteInPalateAroma: List<Aroma> = emptyList(),
    val tasteAftertaste: TasteLevel? = null,
    val tasteAftertasteNote: String? = null,
    val tasteComplexity: ComplexityLevel? = null,
    val otherIndividuality: String? = null,
    val otherCautions: String? = null,
    val otherSakeTypes: List<FlavorProfileType> = emptyList(),
    val otherFreeComment: String? = null,
    val otherOverallReview: OverallReview? = null,
)

/**
 * レビュー登録/編集の入力値。
 */
data class ReviewInput(
    val id: ReviewId? = null,
    val sakeId: SakeId,
    val date: LocalDate,
    val bar: String? = null,
    val price: Int? = null,
    val volume: Int? = null,
    val temperature: Temperature? = null,
    val dish: String? = null,
    val foodCompatibility: FoodCompatibility? = null,
    val appearanceSoundness: ReviewSoundness? = null,
    val appearanceColor: SakeColor? = null,
    val appearanceColorOther: String? = null,
    val appearanceViscosity: Int? = null,
    val aromaSoundness: ReviewSoundness? = null,
    val aromaIntensity: IntensityLevel? = null,
    val aromaExamples: List<Aroma> = emptyList(),
    val aromaMainNote: String? = null,
    val aromaComplexity: ComplexityLevel? = null,
    val tasteSoundness: ReviewSoundness? = null,
    val tasteAttack: AttackLevel? = null,
    val tasteTextureRoundness: TextureRoundness? = null,
    val tasteTextureSmoothness: TextureSmoothness? = null,
    val tasteTextureNote: String? = null,
    val tasteSweetness: TasteLevel? = null,
    val tasteSourness: TasteLevel? = null,
    val tasteBitterness: TasteLevel? = null,
    val tasteUmami: TasteLevel? = null,
    val tasteDescription: String? = null,
    val tasteSweetDryness: SweetDryness? = null,
    val tasteInPalateAromaIntensity: IntensityLevel? = null,
    val tasteInPalateAroma: List<Aroma> = emptyList(),
    val tasteAftertaste: TasteLevel? = null,
    val tasteAftertasteNote: String? = null,
    val tasteComplexity: ComplexityLevel? = null,
    val otherIndividuality: String? = null,
    val otherCautions: String? = null,
    val otherSakeTypes: List<FlavorProfileType> = emptyList(),
    val otherFreeComment: String? = null,
    val otherOverallReview: OverallReview? = null,
)
