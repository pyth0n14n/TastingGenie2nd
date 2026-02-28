package io.github.pyth0n14n.tastinggenie.data.mapper

import io.github.pyth0n14n.tastinggenie.data.local.entity.ReviewEntity
import io.github.pyth0n14n.tastinggenie.domain.model.Review
import io.github.pyth0n14n.tastinggenie.domain.model.ReviewInput
import io.github.pyth0n14n.tastinggenie.domain.model.SerializableReview
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Aroma
import io.github.pyth0n14n.tastinggenie.domain.model.enums.IntensityLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.OverallReview
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeColor
import io.github.pyth0n14n.tastinggenie.domain.model.enums.TasteLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Temperature
import java.time.LocalDate

fun ReviewEntity.toDomain(): Review {
    return Review(
        id = id,
        sakeId = sakeId,
        date = LocalDate.ofEpochDay(dateEpochDay),
        bar = bar,
        price = price,
        volume = volume,
        temperature = temperature,
        color = color,
        viscosity = viscosity,
        intensity = intensity,
        scentTop = scentTop,
        scentBase = scentBase,
        scentMouth = scentMouth,
        sweet = sweet,
        sour = sour,
        bitter = bitter,
        umami = umami,
        sharp = sharp,
        scene = scene,
        dish = dish,
        comment = comment,
        review = review,
        imageUri = imageUri,
    )
}

fun ReviewInput.toEntity(): ReviewEntity {
    return ReviewEntity(
        id = id ?: 0L,
        sakeId = sakeId,
        dateEpochDay = date.toEpochDay(),
        bar = bar,
        price = price,
        volume = volume,
        temperature = temperature,
        color = color,
        viscosity = viscosity,
        intensity = intensity,
        scentTop = scentTop,
        scentBase = scentBase,
        scentMouth = scentMouth,
        sweet = sweet,
        sour = sour,
        bitter = bitter,
        umami = umami,
        sharp = sharp,
        scene = scene,
        dish = dish,
        comment = comment,
        review = review,
        imageUri = imageUri,
    )
}

fun ReviewEntity.toSerializable(): SerializableReview {
    return SerializableReview(
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
        scentTop = scentTop.map { it.name },
        scentBase = scentBase.map { it.name },
        scentMouth = scentMouth.map { it.name },
        sweet = sweet?.name,
        sour = sour?.name,
        bitter = bitter?.name,
        umami = umami?.name,
        sharp = sharp?.name,
        scene = scene,
        dish = dish,
        comment = comment,
        review = review?.name,
        imageUri = imageUri,
    )
}

fun SerializableReview.toEntity(): ReviewEntity {
    return ReviewEntity(
        id = id,
        sakeId = sakeId,
        dateEpochDay = LocalDate.parse(date).toEpochDay(),
        bar = bar,
        price = price,
        volume = volume,
        temperature = temperature?.let { enumValueOf<Temperature>(it) },
        color = color?.let { enumValueOf<SakeColor>(it) },
        viscosity = viscosity,
        intensity = intensity?.let { enumValueOf<IntensityLevel>(it) },
        scentTop = scentTop.map { enumValueOf<Aroma>(it) },
        scentBase = scentBase.map { enumValueOf<Aroma>(it) },
        scentMouth = scentMouth.map { enumValueOf<Aroma>(it) },
        sweet = sweet?.let { enumValueOf<TasteLevel>(it) },
        sour = sour?.let { enumValueOf<TasteLevel>(it) },
        bitter = bitter?.let { enumValueOf<TasteLevel>(it) },
        umami = umami?.let { enumValueOf<TasteLevel>(it) },
        sharp = sharp?.let { enumValueOf<TasteLevel>(it) },
        scene = scene,
        dish = dish,
        comment = comment,
        review = review?.let { enumValueOf<OverallReview>(it) },
        imageUri = imageUri,
    )
}
