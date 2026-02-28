package io.github.pyth0n14n.tastinggenie.data.mapper

import io.github.pyth0n14n.tastinggenie.data.local.entity.SakeEntity
import io.github.pyth0n14n.tastinggenie.domain.model.Sake
import io.github.pyth0n14n.tastinggenie.domain.model.SakeInput

fun SakeEntity.toDomain(): Sake =
    Sake(
        id = id,
        name = name,
        grade = grade,
        type = type,
        typeOther = typeOther,
        maker = maker,
        prefecture = prefecture,
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

fun SakeInput.toEntity(): SakeEntity =
    SakeEntity(
        id = id ?: 0L,
        name = name,
        grade = grade,
        type = type,
        typeOther = typeOther,
        maker = maker,
        prefecture = prefecture,
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
