package io.github.pyth0n14n.tastinggenie.data.repository

import io.github.pyth0n14n.tastinggenie.data.local.AppDatabase
import io.github.pyth0n14n.tastinggenie.data.local.entity.ReviewEntity
import io.github.pyth0n14n.tastinggenie.data.local.entity.SakeEntity
import io.github.pyth0n14n.tastinggenie.data.mapper.toImportedEntity
import io.github.pyth0n14n.tastinggenie.domain.model.SerializableReview
import io.github.pyth0n14n.tastinggenie.domain.model.SerializableSake
import io.github.pyth0n14n.tastinggenie.domain.repository.SakeImageRepository
import java.time.LocalDate

internal suspend fun resolveOrInsertSake(
    sake: SerializableSake,
    knownSakes: MutableList<SakeEntity>,
    context: ImportDedupContext,
): Long {
    val targetKey = sake.toImportKey()
    knownSakes.firstOrNull { existing -> existing.toImportKey() == targetKey }?.let { existing ->
        return existing.id
    }
    val imageUri =
        importSakeImage(
            imagePath = sake.imagePath,
            context = context,
        )
    val importedEntity = sake.toImportedEntity(imageUri = imageUri)
    val localId = context.database.sakeDao().insert(importedEntity)
    knownSakes += importedEntity.copy(id = localId)
    return localId
}

internal suspend fun insertReviewIfNeeded(
    review: SerializableReview,
    sakeId: Long,
    knownReviews: MutableList<ReviewEntity>,
    database: AppDatabase,
) {
    val targetKey = review.toImportKey(sakeId = sakeId)
    if (knownReviews.any { existing -> existing.toImportKey() == targetKey }) {
        return
    }
    val importedEntity = review.toImportedEntity(sakeId = sakeId)
    val localId = database.reviewDao().insert(importedEntity)
    knownReviews += importedEntity.copy(id = localId)
}

private suspend fun importSakeImage(
    imagePath: String?,
    context: ImportDedupContext,
): String? {
    if (imagePath.isNullOrBlank()) {
        return null
    }
    val imageBytes =
        requireNotNull(context.imageEntries[imagePath]) {
            "Backup archive is missing image entry: $imagePath"
        }
    val importedImageUri =
        context.sakeImageRepository.importImageBytes(
            filenameHint = imagePath.substringAfterLast('/'),
            bytes = imageBytes,
        )
    context.importedImageUris += importedImageUri
    return importedImageUri
}

internal data class ImportDedupContext(
    val database: AppDatabase,
    val sakeImageRepository: SakeImageRepository,
    val imageEntries: Map<String, ByteArray>,
    val importedImageUris: MutableList<String>,
)

private data class ComparableSakeRecord(
    val name: String,
    val grade: String,
    val gradeOther: String?,
    val type: List<String>,
    val typeOther: String?,
    val maker: String?,
    val prefecture: String?,
    val alcohol: Int?,
    val kojiMai: String?,
    val kojiPolish: Int?,
    val kakeMai: String?,
    val kakePolish: Int?,
    val sakeDegree: Float?,
    val acidity: Float?,
    val amino: Float?,
    val yeast: String?,
    val water: String?,
    val hasImage: Boolean,
)

private data class ComparableReviewRecord(
    val sakeId: Long,
    val dateEpochDay: Long,
    val bar: String?,
    val price: Int?,
    val volume: Int?,
    val temperature: String?,
    val color: String?,
    val viscosity: Int?,
    val intensity: String?,
    val scentTop: List<String>,
    val scentBase: List<String>,
    val scentMouth: List<String>,
    val sweet: String?,
    val sour: String?,
    val bitter: String?,
    val umami: String?,
    val sharp: String?,
    val scene: String?,
    val dish: String?,
    val comment: String?,
    val review: String?,
)

private fun SakeEntity.toImportKey(): ComparableSakeRecord =
    ComparableSakeRecord(
        name = name.trim(),
        grade = grade.name,
        gradeOther = gradeOther,
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
        hasImage = !imageUri.isNullOrBlank(),
    )

private fun SerializableSake.toImportKey(): ComparableSakeRecord =
    ComparableSakeRecord(
        name = name.trim(),
        grade = grade,
        gradeOther = gradeOther,
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
        hasImage = !imagePath.isNullOrBlank(),
    )

private fun ReviewEntity.toImportKey(): ComparableReviewRecord =
    ComparableReviewRecord(
        sakeId = sakeId,
        dateEpochDay = dateEpochDay,
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
    )

private fun SerializableReview.toImportKey(sakeId: Long): ComparableReviewRecord =
    ComparableReviewRecord(
        sakeId = sakeId,
        dateEpochDay = LocalDate.parse(date).toEpochDay(),
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
    )
