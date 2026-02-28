package io.github.pyth0n14n.tastinggenie.data.mapper

import io.github.pyth0n14n.tastinggenie.data.local.entity.SakeEntity
import io.github.pyth0n14n.tastinggenie.domain.model.Sake
import io.github.pyth0n14n.tastinggenie.domain.model.SakeInput
import io.github.pyth0n14n.tastinggenie.domain.model.SerializableSake
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Prefecture
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeClassification
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeGrade

fun SakeEntity.toDomain(): Sake {
    return Sake(
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
}

fun SakeInput.toEntity(): SakeEntity {
    return SakeEntity(
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
}

fun SakeEntity.toSerializable(): SerializableSake {
    return SerializableSake(
        id = id,
        name = name,
        grade = grade.name,
        type = type.map { it.name },
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
}

fun SerializableSake.toEntity(): SakeEntity {
    return SakeEntity(
        id = id,
        name = name,
        grade = enumValueOf(grade),
        type = type.map { enumValueOf<SakeClassification>(it) },
        typeOther = typeOther,
        maker = maker,
        prefecture = prefecture?.let { enumValueOf<Prefecture>(it) },
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
}
