package io.github.pyth0n14n.tastinggenie.data.mapper

import io.github.pyth0n14n.tastinggenie.data.local.entity.ReviewEntity
import io.github.pyth0n14n.tastinggenie.data.local.entity.SakeEntity
import io.github.pyth0n14n.tastinggenie.domain.model.SerializableReview
import io.github.pyth0n14n.tastinggenie.domain.model.SerializableSake
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Aroma
import io.github.pyth0n14n.tastinggenie.domain.model.enums.IntensityLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.OverallReview
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Prefecture
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeClassification
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeColor
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeGrade
import io.github.pyth0n14n.tastinggenie.domain.model.enums.TasteLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Temperature
import java.time.LocalDate

private const val MIN_IMPORTED_VISCOSITY = 1
private const val MAX_IMPORTED_VISCOSITY = 3

fun SakeEntity.toSerializable(): SerializableSake =
    SerializableSake(
        id = id,
        name = name,
        grade = grade.name,
        type = type.map { classification -> classification.name },
        typeOther = typeOther,
        maker = maker,
        prefecture = prefecture?.name,
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
        type = type.map { classification -> enumValueOf<SakeClassification>(classification) },
        typeOther = typeOther,
        maker = maker,
        prefecture = prefecture?.let { value -> enumValueOf<Prefecture>(value) },
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
        color = color?.name,
        viscosity = viscosity,
        intensity = intensity?.name,
        scentTop = scentTop.map { aroma -> aroma.name },
        scentBase = scentBase.map { aroma -> aroma.name },
        scentMouth = scentMouth.map { aroma -> aroma.name },
        sweet = sweet?.name,
        sour = sour?.name,
        bitter = bitter?.name,
        umami = umami?.name,
        sharp = sharp?.name,
        scene = scene,
        dish = dish,
        comment = comment,
        review = review?.name,
        // SAF content URIs are not portable across installs or devices, so backups omit them.
        imageUri = null,
    )

fun SerializableReview.toImportedEntity(sakeId: Long): ReviewEntity =
    ReviewEntity(
        sakeId = sakeId,
        dateEpochDay = LocalDate.parse(date).toEpochDay(),
        bar = bar,
        price = price,
        volume = volume,
        temperature = temperature?.let { value -> enumValueOf<Temperature>(value) },
        color = color?.let { value -> enumValueOf<SakeColor>(value) },
        viscosity =
            viscosity?.also { importedViscosity ->
                require(importedViscosity in MIN_IMPORTED_VISCOSITY..MAX_IMPORTED_VISCOSITY) {
                    "Backup viscosity must be between $MIN_IMPORTED_VISCOSITY and $MAX_IMPORTED_VISCOSITY"
                }
            },
        intensity = intensity?.let { value -> enumValueOf<IntensityLevel>(value) },
        scentTop = scentTop.map { aroma -> enumValueOf<Aroma>(aroma) },
        scentBase = scentBase.map { aroma -> enumValueOf<Aroma>(aroma) },
        scentMouth = scentMouth.map { aroma -> enumValueOf<Aroma>(aroma) },
        sweet = sweet?.let { value -> enumValueOf<TasteLevel>(value) },
        sour = sour?.let { value -> enumValueOf<TasteLevel>(value) },
        bitter = bitter?.let { value -> enumValueOf<TasteLevel>(value) },
        umami = umami?.let { value -> enumValueOf<TasteLevel>(value) },
        sharp = sharp?.let { value -> enumValueOf<TasteLevel>(value) },
        scene = scene,
        dish = dish,
        comment = comment,
        review = review?.let { value -> enumValueOf<OverallReview>(value) },
        imageUri = null,
    )
