package io.github.pyth0n14n.tastinggenie.data.mapper

import io.github.pyth0n14n.tastinggenie.data.local.entity.ReviewEntity
import io.github.pyth0n14n.tastinggenie.domain.model.Review
import io.github.pyth0n14n.tastinggenie.domain.model.ReviewInput
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

fun ReviewInput.toEntity(): ReviewEntity =
    ReviewEntity(
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
