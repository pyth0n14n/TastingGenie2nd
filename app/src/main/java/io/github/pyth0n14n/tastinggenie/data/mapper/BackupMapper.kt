@file:Suppress("TooManyFunctions")

package io.github.pyth0n14n.tastinggenie.data.mapper

import io.github.pyth0n14n.tastinggenie.data.local.entity.ReviewEntity
import io.github.pyth0n14n.tastinggenie.data.local.entity.SakeEntity
import io.github.pyth0n14n.tastinggenie.data.local.entity.SakeFoodReviewEntity
import io.github.pyth0n14n.tastinggenie.domain.model.SerializableReview
import io.github.pyth0n14n.tastinggenie.domain.model.SerializableSake
import io.github.pyth0n14n.tastinggenie.domain.model.SerializableSakeFoodReview
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Aroma
import io.github.pyth0n14n.tastinggenie.domain.model.enums.AttackLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.ComplexityLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.FlavorProfileType
import io.github.pyth0n14n.tastinggenie.domain.model.enums.FoodCompatibility
import io.github.pyth0n14n.tastinggenie.domain.model.enums.IntensityLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.OverallReview
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Prefecture
import io.github.pyth0n14n.tastinggenie.domain.model.enums.ReviewSoundness
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeClassification
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeColor
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeGrade
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SweetDryness
import io.github.pyth0n14n.tastinggenie.domain.model.enums.TasteLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Temperature
import io.github.pyth0n14n.tastinggenie.domain.model.enums.TextureRoundness
import io.github.pyth0n14n.tastinggenie.domain.model.enums.TextureSmoothness
import java.time.LocalDate

private const val MIN_IMPORTED_VISCOSITY = 1
private const val MAX_IMPORTED_VISCOSITY = 5

fun SakeEntity.toSerializable(): SerializableSake =
    SerializableSake(
        id = id,
        name = name,
        grade = grade.name,
        isPinned = isPinned,
        imageUris = imageUris,
        gradeOther = gradeOther,
        type = type.map { classification -> classification.name },
        typeOther = typeOther,
        maker = maker,
        prefecture = prefecture?.name,
        city = city,
        alcohol = alcohol,
        kojiMai = kojiMai,
        kojiPolish = kojiPolish,
        kakeMai = kakeMai,
        kakePolish = kakePolish,
        sakeDegree = sakeDegree,
        acidity = acidity,
        amino = amino,
        yeast = yeast,
        water = water,
    )

fun SerializableSake.toImportedEntity(): SakeEntity = toRestoredEntity(id = 0L, imageUris = emptyList())

fun SerializableSake.toRestoredEntity(
    id: Long = this.id,
    imageUris: List<String> = this.imageUris,
): SakeEntity =
    SakeEntity(
        id = id,
        name =
            name
                .trim()
                .also { trimmedName -> require(trimmedName.isNotEmpty()) { "Backup sake name must not be blank" } },
        grade = enumValueOf<SakeGrade>(grade),
        isPinned = isPinned,
        imageUris = imageUris,
        gradeOther = gradeOther,
        type = type.map { classification -> enumValueOf<SakeClassification>(classification) },
        typeOther = typeOther,
        maker = maker,
        prefecture = prefecture?.let { value -> enumValueOf<Prefecture>(value) },
        city = city,
        alcohol = alcohol,
        kojiMai = kojiMai,
        kojiPolish = kojiPolish,
        kakeMai = kakeMai,
        kakePolish = kakePolish,
        sakeDegree = sakeDegree,
        acidity = acidity,
        amino = amino,
        yeast = yeast,
        water = water,
    )

fun ReviewEntity.toSerializable(): SerializableReview =
    SerializableReview(
        id = id,
        sakeId = sakeId,
        date = LocalDate.ofEpochDay(dateEpochDay).toString(),
        bar = bar,
        price = price,
        volume = volume,
        temperature = temperature?.name,
        appearanceSoundness = appearanceSoundness?.name,
        appearanceColor = appearanceColor?.name,
        appearanceColorOther = appearanceColorOther,
        appearanceViscosity = appearanceViscosity,
        aromaSoundness = aromaSoundness?.name,
        aromaIntensity = aromaIntensity?.name,
        aromaExamples = aromaExamples.map { aroma -> aroma.name },
        aromaMainNote = aromaMainNote,
        aromaComplexity = aromaComplexity?.name,
        tasteSoundness = tasteSoundness?.name,
        tasteAttack = tasteAttack?.name,
        tasteTextureRoundness = tasteTextureRoundness?.name,
        tasteTextureSmoothness = tasteTextureSmoothness?.name,
        tasteTextureNote = tasteTextureNote,
        tasteSweetness = tasteSweetness?.name,
        tasteSourness = tasteSourness?.name,
        tasteBitterness = tasteBitterness?.name,
        tasteUmami = tasteUmami?.name,
        tasteDescription = tasteDescription,
        tasteSweetDryness = tasteSweetDryness?.name,
        tasteInPalateAromaIntensity = tasteInPalateAromaIntensity?.name,
        tasteInPalateAroma = tasteInPalateAroma.map { aroma -> aroma.name },
        tasteAftertaste = tasteAftertaste?.name,
        tasteAftertasteNote = tasteAftertasteNote,
        tasteComplexity = tasteComplexity?.name,
        otherIndividuality = otherIndividuality,
        otherCautions = otherCautions,
        otherSakeTypes = otherSakeTypes.map { type -> type.name },
        otherFreeComment = otherFreeComment,
        otherOverallReview = otherOverallReview?.name,
    )

fun SerializableReview.toImportedEntity(sakeId: Long): ReviewEntity = toRestoredEntity(id = 0L, sakeId = sakeId)

fun SerializableReview.toRestoredEntity(
    id: Long = this.id,
    sakeId: Long = this.sakeId,
): ReviewEntity =
    ReviewEntity(
        id = id,
        sakeId = sakeId,
        dateEpochDay = LocalDate.parse(date).toEpochDay(),
        bar = bar,
        price = price,
        volume = volume,
        temperature = temperature?.let { value -> enumValueOf<Temperature>(value) },
        appearanceSoundness = appearanceSoundness.toNullableEnum<ReviewSoundness>(),
        appearanceColor = appearanceColor.toNullableEnum<SakeColor>(),
        appearanceColorOther = appearanceColorOther,
        appearanceViscosity = validateImportedViscosity(appearanceViscosity),
        aromaSoundness = aromaSoundness.toNullableEnum<ReviewSoundness>(),
        aromaIntensity = aromaIntensity.toNullableEnum<IntensityLevel>(),
        aromaExamples = aromaExamples.toAromaList(),
        aromaMainNote = aromaMainNote,
        aromaComplexity = aromaComplexity.toNullableEnum<ComplexityLevel>(),
        tasteSoundness = tasteSoundness.toNullableEnum<ReviewSoundness>(),
        tasteAttack = tasteAttack.toNullableEnum<AttackLevel>(),
        tasteTextureRoundness = tasteTextureRoundness.toNullableEnum<TextureRoundness>(),
        tasteTextureSmoothness = tasteTextureSmoothness.toNullableEnum<TextureSmoothness>(),
        tasteTextureNote = tasteTextureNote,
        tasteSweetness = tasteSweetness.toNullableEnum<TasteLevel>(),
        tasteSourness = tasteSourness.toNullableEnum<TasteLevel>(),
        tasteBitterness = tasteBitterness.toNullableEnum<TasteLevel>(),
        tasteUmami = tasteUmami.toNullableEnum<TasteLevel>(),
        tasteDescription = tasteDescription,
        tasteSweetDryness = tasteSweetDryness.toNullableEnum<SweetDryness>(),
        tasteInPalateAromaIntensity = tasteInPalateAromaIntensity.toNullableEnum<IntensityLevel>(),
        tasteInPalateAroma = tasteInPalateAroma.toAromaList(),
        tasteAftertaste = tasteAftertaste.toNullableEnum<TasteLevel>(),
        tasteAftertasteNote = tasteAftertasteNote,
        tasteComplexity = tasteComplexity.toNullableEnum<ComplexityLevel>(),
        otherIndividuality = otherIndividuality,
        otherCautions = otherCautions,
        otherSakeTypes = otherSakeTypes.map { type -> enumValueOf<FlavorProfileType>(type) },
        otherFreeComment = otherFreeComment,
        otherOverallReview = otherOverallReview.toNullableEnum<OverallReview>(),
    )

fun SakeFoodReviewEntity.toSerializable(): SerializableSakeFoodReview =
    SerializableSakeFoodReview(
        id = id,
        sakeId = sakeId,
        date = LocalDate.ofEpochDay(dateEpochDay).toString(),
        bar = bar,
        dish = dish,
        foodCompatibility = foodCompatibility?.name,
        temperature = temperature?.name,
        freeComment = freeComment,
    )

fun SerializableSakeFoodReview.toRestoredEntity(
    id: Long = this.id,
    sakeId: Long = this.sakeId,
): SakeFoodReviewEntity =
    SakeFoodReviewEntity(
        id = id,
        sakeId = sakeId,
        dateEpochDay = LocalDate.parse(date).toEpochDay(),
        bar = bar,
        dish = dish,
        foodCompatibility = foodCompatibility.toNullableEnum<FoodCompatibility>(),
        temperature = temperature.toNullableEnum<Temperature>(),
        freeComment = freeComment,
    )

private inline fun <reified T : Enum<T>> String?.toNullableEnum(): T? = this?.let { value -> enumValueOf<T>(value) }

private fun List<String>.toAromaList(): List<Aroma> = map { aroma -> enumValueOf<Aroma>(aroma) }

private fun validateImportedViscosity(viscosity: Int?): Int? =
    viscosity?.also { importedViscosity ->
        require(importedViscosity in MIN_IMPORTED_VISCOSITY..MAX_IMPORTED_VISCOSITY) {
            "Backup viscosity must be between $MIN_IMPORTED_VISCOSITY and $MAX_IMPORTED_VISCOSITY"
        }
    }
