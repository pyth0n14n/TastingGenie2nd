package io.github.pyth0n14n.tastinggenie.domain.model

import io.github.pyth0n14n.tastinggenie.domain.model.enums.Aroma
import io.github.pyth0n14n.tastinggenie.domain.model.enums.IntensityLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.OverallReview
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeColor
import io.github.pyth0n14n.tastinggenie.domain.model.enums.TasteLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Temperature
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
    val color: SakeColor? = null,
    val viscosity: Int? = null,
    val intensity: IntensityLevel? = null,
    val scentTop: List<Aroma> = emptyList(),
    val scentBase: List<Aroma> = emptyList(),
    val scentMouth: List<Aroma> = emptyList(),
    val sweet: TasteLevel? = null,
    val sour: TasteLevel? = null,
    val bitter: TasteLevel? = null,
    val umami: TasteLevel? = null,
    val sharp: TasteLevel? = null,
    val scene: String? = null,
    val dish: String? = null,
    val comment: String? = null,
    val review: OverallReview? = null,
    val imageUri: String? = null,
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
    val color: SakeColor? = null,
    val viscosity: Int? = null,
    val intensity: IntensityLevel? = null,
    val scentTop: List<Aroma> = emptyList(),
    val scentBase: List<Aroma> = emptyList(),
    val scentMouth: List<Aroma> = emptyList(),
    val sweet: TasteLevel? = null,
    val sour: TasteLevel? = null,
    val bitter: TasteLevel? = null,
    val umami: TasteLevel? = null,
    val sharp: TasteLevel? = null,
    val scene: String? = null,
    val dish: String? = null,
    val comment: String? = null,
    val review: OverallReview? = null,
    val imageUri: String? = null,
)
