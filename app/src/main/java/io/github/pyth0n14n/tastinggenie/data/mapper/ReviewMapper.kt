package io.github.pyth0n14n.tastinggenie.data.mapper

import io.github.pyth0n14n.tastinggenie.data.local.entity.ReviewEntity
import io.github.pyth0n14n.tastinggenie.data.local.entity.SakeFoodReviewEntity
import io.github.pyth0n14n.tastinggenie.domain.model.Review
import io.github.pyth0n14n.tastinggenie.domain.model.ReviewInput
import io.github.pyth0n14n.tastinggenie.domain.model.SakeFoodReview
import io.github.pyth0n14n.tastinggenie.domain.model.SakeFoodReviewInput
import java.time.LocalDate

fun ReviewEntity.toDomain(): Review =
    Review(
        id = id,
        sakeId = sakeId,
        date = LocalDate.ofEpochDay(dateEpochDay),
        bar = bar,
        price = price,
        volume = volume,
        temperature = temperature,
        appearanceSoundness = appearanceSoundness,
        appearanceColor = appearanceColor,
        appearanceColorOther = appearanceColorOther,
        appearanceViscosity = appearanceViscosity,
        aromaSoundness = aromaSoundness,
        aromaIntensity = aromaIntensity,
        aromaExamples = aromaExamples,
        aromaMainNote = aromaMainNote,
        aromaComplexity = aromaComplexity,
        tasteSoundness = tasteSoundness,
        tasteAttack = tasteAttack,
        tasteTextureRoundness = tasteTextureRoundness,
        tasteTextureSmoothness = tasteTextureSmoothness,
        tasteTextureNote = tasteTextureNote,
        tasteSweetness = tasteSweetness,
        tasteSourness = tasteSourness,
        tasteBitterness = tasteBitterness,
        tasteUmami = tasteUmami,
        tasteDescription = tasteDescription,
        tasteSweetDryness = tasteSweetDryness,
        tasteInPalateAromaIntensity = tasteInPalateAromaIntensity,
        tasteInPalateAroma = tasteInPalateAroma,
        tasteAftertaste = tasteAftertaste,
        tasteAftertasteNote = tasteAftertasteNote,
        tasteComplexity = tasteComplexity,
        otherIndividuality = otherIndividuality,
        otherCautions = otherCautions,
        otherSakeTypes = otherSakeTypes,
        otherFreeComment = otherFreeComment,
        otherOverallReview = otherOverallReview,
    )

fun ReviewInput.toEntity(): ReviewEntity =
    ReviewEntity(
        id = id ?: 0L,
        sakeId = sakeId,
        dateEpochDay = date.toEpochDay(),
        bar = bar,
        price = price,
        volume = volume,
        temperature = temperature,
        appearanceSoundness = appearanceSoundness,
        appearanceColor = appearanceColor,
        appearanceColorOther = appearanceColorOther,
        appearanceViscosity = appearanceViscosity,
        aromaSoundness = aromaSoundness,
        aromaIntensity = aromaIntensity,
        aromaExamples = aromaExamples,
        aromaMainNote = aromaMainNote,
        aromaComplexity = aromaComplexity,
        tasteSoundness = tasteSoundness,
        tasteAttack = tasteAttack,
        tasteTextureRoundness = tasteTextureRoundness,
        tasteTextureSmoothness = tasteTextureSmoothness,
        tasteTextureNote = tasteTextureNote,
        tasteSweetness = tasteSweetness,
        tasteSourness = tasteSourness,
        tasteBitterness = tasteBitterness,
        tasteUmami = tasteUmami,
        tasteDescription = tasteDescription,
        tasteSweetDryness = tasteSweetDryness,
        tasteInPalateAromaIntensity = tasteInPalateAromaIntensity,
        tasteInPalateAroma = tasteInPalateAroma,
        tasteAftertaste = tasteAftertaste,
        tasteAftertasteNote = tasteAftertasteNote,
        tasteComplexity = tasteComplexity,
        otherIndividuality = otherIndividuality,
        otherCautions = otherCautions,
        otherSakeTypes = otherSakeTypes,
        otherFreeComment = otherFreeComment,
        otherOverallReview = otherOverallReview,
    )

fun SakeFoodReviewEntity.toDomain(): SakeFoodReview =
    SakeFoodReview(
        id = id,
        sakeId = sakeId,
        date = LocalDate.ofEpochDay(dateEpochDay),
        bar = bar,
        dish = dish,
        foodCompatibility = foodCompatibility,
        temperature = temperature,
        freeComment = freeComment,
    )

fun SakeFoodReviewInput.toEntity(): SakeFoodReviewEntity =
    SakeFoodReviewEntity(
        id = id ?: 0L,
        sakeId = sakeId,
        dateEpochDay = date.toEpochDay(),
        bar = bar,
        dish = dish,
        foodCompatibility = foodCompatibility,
        temperature = temperature,
        freeComment = freeComment,
    )
