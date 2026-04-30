package io.github.pyth0n14n.tastinggenie.data.mapper

import io.github.pyth0n14n.tastinggenie.data.local.entity.ReviewEntity
import io.github.pyth0n14n.tastinggenie.data.local.entity.SakeEntity
import io.github.pyth0n14n.tastinggenie.domain.model.LegacySerializableReviewV3
import io.github.pyth0n14n.tastinggenie.domain.model.SerializableReview
import io.github.pyth0n14n.tastinggenie.domain.model.SerializableSake
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Aroma
import io.github.pyth0n14n.tastinggenie.domain.model.enums.AttackLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.ComplexityLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.IntensityLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.OverallReview
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Prefecture
import io.github.pyth0n14n.tastinggenie.domain.model.enums.ReviewSoundness
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeClassification
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeColor
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeGrade
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

fun SerializableSake.toImportedEntity(): SakeEntity =
    SakeEntity(
        name =
            name
                .trim()
                .also { trimmedName -> require(trimmedName.isNotEmpty()) { "Backup sake name must not be blank" } },
        grade = enumValueOf<SakeGrade>(grade),
        isPinned = isPinned,
        imageUris = emptyList(),
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
        scene = scene,
        dish = dish,
        appearanceSoundness = appearanceSoundness.name,
        appearanceColor = appearanceColor?.name,
        appearanceViscosity = appearanceViscosity,
        aromaSoundness = aromaSoundness.name,
        aromaIntensity = aromaIntensity?.name,
        aromaExamples = aromaExamples.map { aroma -> aroma.name },
        aromaMainNote = aromaMainNote,
        aromaComplexity = aromaComplexity?.name,
        tasteSoundness = tasteSoundness.name,
        tasteAttack = tasteAttack?.name,
        tasteTextureRoundness = tasteTextureRoundness?.name,
        tasteTextureSmoothness = tasteTextureSmoothness?.name,
        tasteMainNote = tasteMainNote,
        tasteSweetness = tasteSweetness?.name,
        tasteSourness = tasteSourness?.name,
        tasteBitterness = tasteBitterness?.name,
        tasteUmami = tasteUmami?.name,
        tasteInPalateAroma = tasteInPalateAroma.map { aroma -> aroma.name },
        tasteAftertaste = tasteAftertaste?.name,
        tasteComplexity = tasteComplexity?.name,
        otherIndividuality = otherIndividuality,
        otherCautions = otherCautions,
        otherFreeComment = otherFreeComment,
        otherOverallReview = otherOverallReview?.name,
    )

fun SerializableReview.toImportedEntity(sakeId: Long): ReviewEntity =
    ReviewEntity(
        sakeId = sakeId,
        dateEpochDay = LocalDate.parse(date).toEpochDay(),
        bar = bar,
        price = price,
        volume = volume,
        temperature = temperature?.let { value -> enumValueOf<Temperature>(value) },
        scene = scene,
        dish = dish,
        appearanceSoundness = appearanceSoundness.toEnum(),
        appearanceColor = appearanceColor.toNullableEnum<SakeColor>(),
        appearanceViscosity = validateImportedViscosity(appearanceViscosity),
        aromaSoundness = aromaSoundness.toEnum(),
        aromaIntensity = aromaIntensity.toNullableEnum<IntensityLevel>(),
        aromaExamples = aromaExamples.toAromaList(),
        aromaMainNote = aromaMainNote,
        aromaComplexity = aromaComplexity.toNullableEnum<ComplexityLevel>(),
        tasteSoundness = tasteSoundness.toEnum(),
        tasteAttack = tasteAttack.toNullableEnum<AttackLevel>(),
        tasteTextureRoundness = tasteTextureRoundness.toNullableEnum<TextureRoundness>(),
        tasteTextureSmoothness = tasteTextureSmoothness.toNullableEnum<TextureSmoothness>(),
        tasteMainNote = tasteMainNote,
        tasteSweetness = tasteSweetness.toNullableEnum<TasteLevel>(),
        tasteSourness = tasteSourness.toNullableEnum<TasteLevel>(),
        tasteBitterness = tasteBitterness.toNullableEnum<TasteLevel>(),
        tasteUmami = tasteUmami.toNullableEnum<TasteLevel>(),
        tasteInPalateAroma = tasteInPalateAroma.toAromaList(),
        tasteAftertaste = tasteAftertaste.toNullableEnum<TasteLevel>(),
        tasteComplexity = tasteComplexity.toNullableEnum<ComplexityLevel>(),
        otherIndividuality = otherIndividuality,
        otherCautions = otherCautions,
        otherFreeComment = otherFreeComment,
        otherOverallReview = otherOverallReview.toNullableEnum<OverallReview>(),
    )

fun LegacySerializableReviewV3.toSerializableV4(): SerializableReview =
    SerializableReview(
        id = id,
        sakeId = sakeId,
        date = date,
        bar = bar,
        price = price,
        volume = volume,
        temperature = temperature,
        scene = scene,
        dish = dish,
        appearanceSoundness = ReviewSoundness.SOUND.name,
        appearanceColor = color,
        appearanceViscosity = viscosity,
        aromaSoundness = ReviewSoundness.SOUND.name,
        aromaIntensity = intensity,
        aromaExamples = scentTop,
        tasteSoundness = ReviewSoundness.SOUND.name,
        tasteSweetness = sweet,
        tasteSourness = sour,
        tasteBitterness = bitter,
        tasteUmami = umami,
        tasteInPalateAroma = scentMouth,
        tasteAftertaste = sharp,
        otherCautions = comment,
        otherOverallReview = review,
    )

private inline fun <reified T : Enum<T>> String.toEnum(): T = enumValueOf<T>(this)

private inline fun <reified T : Enum<T>> String?.toNullableEnum(): T? = this?.let { value -> enumValueOf<T>(value) }

private fun List<String>.toAromaList(): List<Aroma> = map { aroma -> enumValueOf<Aroma>(aroma) }

private fun validateImportedViscosity(viscosity: Int?): Int? =
    viscosity?.also { importedViscosity ->
        require(importedViscosity in MIN_IMPORTED_VISCOSITY..MAX_IMPORTED_VISCOSITY) {
            "Backup viscosity must be between $MIN_IMPORTED_VISCOSITY and $MAX_IMPORTED_VISCOSITY"
        }
    }
