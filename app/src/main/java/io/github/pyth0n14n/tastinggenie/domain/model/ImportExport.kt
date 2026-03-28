package io.github.pyth0n14n.tastinggenie.domain.model

import kotlinx.serialization.Serializable

const val CURRENT_SCHEMA_VERSION = 2

@Serializable
data class BackupPayload(
    val schemaVersion: Int,
    val sakes: List<SerializableSake>,
    val reviews: List<SerializableReview>,
)

@Serializable
data class SerializableSake(
    val id: Long,
    val name: String,
    val grade: String,
    val gradeOther: String? = null,
    val type: List<String>,
    val typeOther: String? = null,
    val maker: String? = null,
    val prefecture: String? = null,
    val alcohol: Int? = null,
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
    val color: String? = null,
    val viscosity: Int? = null,
    val intensity: String? = null,
    val scentTop: List<String> = emptyList(),
    val scentBase: List<String> = emptyList(),
    val scentMouth: List<String> = emptyList(),
    val sweet: String? = null,
    val sour: String? = null,
    val bitter: String? = null,
    val umami: String? = null,
    val sharp: String? = null,
    val scene: String? = null,
    val dish: String? = null,
    val comment: String? = null,
    val review: String? = null,
    val imageUri: String? = null,
)

class UnsupportedSchemaVersionException(
    val version: Int,
) : IllegalArgumentException("Unsupported schema version: $version")

class InvalidBackupReferenceException(
    val sakeId: Long,
) : IllegalArgumentException("Review references unknown sakeId: $sakeId")
