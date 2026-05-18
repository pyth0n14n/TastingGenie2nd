package io.github.pyth0n14n.tastinggenie.domain.model

import io.github.pyth0n14n.tastinggenie.domain.model.enums.FoodCompatibility
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Temperature
import java.time.LocalDate

data class SakeFoodReview(
    val id: SakeFoodReviewId,
    val sakeId: SakeId,
    val date: LocalDate,
    val bar: String? = null,
    val dish: String? = null,
    val foodCompatibility: FoodCompatibility? = null,
    val temperature: Temperature? = null,
    val freeComment: String? = null,
)

data class SakeFoodReviewInput(
    val id: SakeFoodReviewId? = null,
    val sakeId: SakeId,
    val date: LocalDate,
    val bar: String? = null,
    val dish: String? = null,
    val foodCompatibility: FoodCompatibility? = null,
    val temperature: Temperature? = null,
    val freeComment: String? = null,
)
