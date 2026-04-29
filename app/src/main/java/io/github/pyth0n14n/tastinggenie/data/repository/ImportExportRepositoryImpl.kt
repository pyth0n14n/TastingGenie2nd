package io.github.pyth0n14n.tastinggenie.data.repository

import androidx.room.withTransaction
import io.github.pyth0n14n.tastinggenie.data.local.AppDatabase
import io.github.pyth0n14n.tastinggenie.data.mapper.toImportedEntity
import io.github.pyth0n14n.tastinggenie.data.mapper.toSerializable
import io.github.pyth0n14n.tastinggenie.data.mapper.toSerializableV4
import io.github.pyth0n14n.tastinggenie.di.IoDispatcher
import io.github.pyth0n14n.tastinggenie.domain.model.BackupPayload
import io.github.pyth0n14n.tastinggenie.domain.model.CURRENT_SCHEMA_VERSION
import io.github.pyth0n14n.tastinggenie.domain.model.InvalidBackupReferenceException
import io.github.pyth0n14n.tastinggenie.domain.model.LEGACY_SCHEMA_VERSION_3
import io.github.pyth0n14n.tastinggenie.domain.model.LEGACY_SCHEMA_VERSION_4
import io.github.pyth0n14n.tastinggenie.domain.model.LEGACY_SCHEMA_VERSION_5
import io.github.pyth0n14n.tastinggenie.domain.model.LegacyBackupPayloadV3
import io.github.pyth0n14n.tastinggenie.domain.model.UnsupportedSchemaVersionException
import io.github.pyth0n14n.tastinggenie.domain.repository.ImportExportRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

class ImportExportRepositoryImpl
    @Inject
    constructor(
        private val database: AppDatabase,
        private val json: Json,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ImportExportRepository {
        override suspend fun exportJson(): Result<String> =
            runSuspendCatching {
                withContext(ioDispatcher) {
                    val payload =
                        database.withTransaction {
                            val sakes = database.sakeDao().getAllOnce().map { sake -> sake.toSerializable() }
                            val reviews = database.reviewDao().getAllOnce().map { review -> review.toSerializable() }
                            BackupPayload(
                                schemaVersion = CURRENT_SCHEMA_VERSION,
                                sakes = sakes,
                                reviews = reviews,
                            )
                        }
                    json.encodeToString(
                        BackupPayload.serializer(),
                        payload,
                    )
                }
            }

        override suspend fun importJson(rawJson: String): Result<Unit> =
            runSuspendCatching {
                withContext(ioDispatcher) {
                    val payload = decodePayload(rawJson)
                    database.withTransaction {
                        validateReviewReferences(payload)
                        val importedSakeIds =
                            payload.sakes.associate { sake ->
                                sake.id to database.sakeDao().insert(sake.toImportedEntity())
                            }
                        payload.reviews.forEach { review ->
                            val importedSakeId =
                                checkNotNull(importedSakeIds[review.sakeId]) {
                                    "Review references unknown backup sakeId: ${review.sakeId}"
                                }
                            database.reviewDao().insert(review.toImportedEntity(sakeId = importedSakeId))
                        }
                    }
                }
            }

        private fun decodePayload(rawJson: String): BackupPayload {
            val root = json.parseToJsonElement(rawJson).jsonObject
            val version = root.getValue("schemaVersion").jsonPrimitive.int
            return when (version) {
                CURRENT_SCHEMA_VERSION -> json.decodeFromJsonElement(BackupPayload.serializer(), root)
                LEGACY_SCHEMA_VERSION_5 ->
                    json.decodeFromJsonElement(BackupPayload.serializer(), root).copy(
                        schemaVersion = CURRENT_SCHEMA_VERSION,
                    )
                LEGACY_SCHEMA_VERSION_4 ->
                    json.decodeFromJsonElement(BackupPayload.serializer(), root).copy(
                        schemaVersion = CURRENT_SCHEMA_VERSION,
                    )
                LEGACY_SCHEMA_VERSION_3 ->
                    json.decodeFromJsonElement(LegacyBackupPayloadV3.serializer(), root).let { legacy ->
                        BackupPayload(
                            schemaVersion = CURRENT_SCHEMA_VERSION,
                            sakes = legacy.sakes,
                            reviews = legacy.reviews.map { review -> review.toSerializableV4() },
                        )
                    }

                else -> throw UnsupportedSchemaVersionException(version = version)
            }
        }

        private suspend fun validateReviewReferences(payload: BackupPayload) {
            val payloadSakeIds = payload.sakes.map { sake -> sake.id }
            require(payloadSakeIds.size == payloadSakeIds.toSet().size) {
                "Backup contains duplicate sake ids"
            }
            val allowedSakeIds = payloadSakeIds.toSet()
            payload.reviews.firstOrNull { review -> review.sakeId !in allowedSakeIds }?.let { invalidReview ->
                throw InvalidBackupReferenceException(sakeId = invalidReview.sakeId)
            }
        }

        private suspend fun <T> runSuspendCatching(block: suspend () -> T): Result<T> =
            runCatching { block() }.onFailure { throwable ->
                if (throwable is CancellationException) {
                    throw throwable
                }
            }
    }
