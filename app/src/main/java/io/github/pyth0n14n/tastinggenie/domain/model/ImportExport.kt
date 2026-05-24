package io.github.pyth0n14n.tastinggenie.domain.model

import kotlinx.serialization.Serializable

const val CURRENT_SCHEMA_VERSION = 12

@Serializable
data class BackupPayload(
    val schemaVersion: Int,
    val sakes: List<SerializableSake>,
    val reviews: List<SerializableReview>,
    val foodReviews: List<SerializableSakeFoodReview> = emptyList(),
    val reviewModes: List<SerializableReviewMode> = emptyList(),
    val reviewModeItems: List<SerializableReviewModeItem> = emptyList(),
    val settings: SerializableAppSettings = SerializableAppSettings(),
)

@Serializable
data class BackupManifest(
    val schemaVersion: Int,
    val app: String = "tastinggenie",
    val format: String = "full-zip",
)

@Serializable
data class SerializableAppSettings(
    val showHelpHints: Boolean = true,
    val showReviewSoundness: Boolean = false,
    val reviewModeId: String = ReviewMode.NORMAL.id,
)

@Serializable
data class SerializableReviewMode(
    val id: String,
    val label: String,
    val isBuiltIn: Boolean,
)

@Serializable
data class SerializableReviewModeItem(
    val modeId: String,
    val itemId: String,
    val isEnabled: Boolean,
)

@Serializable
data class SerializableSake(
    val id: Long,
    val name: String,
    val grade: String,
    val isPinned: Boolean = false,
    val imageUris: List<String> = emptyList(),
    val gradeOther: String? = null,
    val type: List<String>,
    val typeOther: String? = null,
    val maker: String? = null,
    val prefecture: String? = null,
    val city: String? = null,
    val alcohol: Float? = null,
    val kojiMai: String? = null,
    val kojiPolish: Int? = null,
    val kakeMai: String? = null,
    val kakePolish: Int? = null,
    val sakeDegree: Float? = null,
    val acidity: Float? = null,
    val amino: Float? = null,
    val yeast: String? = null,
    val water: String? = null,
)

@Serializable
data class SerializableReview(
    val id: Long,
    val sakeId: Long,
    val date: String,
    val bar: String? = null,
    val price: Int? = null,
    val volume: Int? = null,
    val temperature: String? = null,
    val dish: String? = null,
    val foodCompatibility: String? = null,
    val appearanceSoundness: String? = "SOUND",
    val appearanceColor: String? = null,
    val appearanceColorOther: String? = null,
    val appearanceViscosity: Int? = null,
    val aromaSoundness: String? = "SOUND",
    val aromaIntensity: String? = null,
    val aromaExamples: List<String> = emptyList(),
    val aromaMainNote: String? = null,
    val aromaComplexity: String? = null,
    val tasteSoundness: String? = "SOUND",
    val tasteAttack: String? = null,
    val tasteTextureRoundness: String? = null,
    val tasteTextureSmoothness: String? = null,
    val tasteTextureNote: String? = null,
    val tasteSweetness: String? = null,
    val tasteSourness: String? = null,
    val tasteBitterness: String? = null,
    val tasteUmami: String? = null,
    val tasteDescription: String? = null,
    val tasteSweetDryness: String? = null,
    val tasteInPalateAromaIntensity: String? = null,
    val tasteInPalateAroma: List<String> = emptyList(),
    val tasteAftertaste: String? = null,
    val tasteAftertasteNote: String? = null,
    val tasteComplexity: String? = null,
    val otherIndividuality: String? = null,
    val otherCautions: String? = null,
    val otherSakeTypes: List<String> = emptyList(),
    val otherFreeComment: String? = null,
    val otherOverallReview: String? = null,
)

@Serializable
data class SerializableSakeFoodReview(
    val id: Long,
    val sakeId: Long,
    val date: String,
    val bar: String? = null,
    val dish: String? = null,
    val foodCompatibility: String? = null,
    val temperature: String? = null,
    val freeComment: String? = null,
)

class UnsupportedSchemaVersionException(
    val version: Int,
) : IllegalArgumentException("Unsupported schema version: $version")

class InvalidBackupReferenceException(
    val sakeId: Long,
) : IllegalArgumentException("Review references unknown sakeId: $sakeId")
