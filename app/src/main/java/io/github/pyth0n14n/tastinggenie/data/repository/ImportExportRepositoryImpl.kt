package io.github.pyth0n14n.tastinggenie.data.repository

import androidx.room.withTransaction
import io.github.pyth0n14n.tastinggenie.data.local.AppDatabase
import io.github.pyth0n14n.tastinggenie.data.mapper.toImportedEntity
import io.github.pyth0n14n.tastinggenie.data.mapper.toSerializable
import io.github.pyth0n14n.tastinggenie.di.IoDispatcher
import io.github.pyth0n14n.tastinggenie.domain.model.BackupPayload
import io.github.pyth0n14n.tastinggenie.domain.model.CURRENT_SCHEMA_VERSION
import io.github.pyth0n14n.tastinggenie.domain.model.InvalidBackupReferenceException
import io.github.pyth0n14n.tastinggenie.domain.model.UnsupportedSchemaVersionException
import io.github.pyth0n14n.tastinggenie.domain.repository.ImportExportRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
                        payload,
                    )
                }
            }

        override suspend fun importJson(rawJson: String): Result<Unit> =
            runSuspendCatching {
                withContext(ioDispatcher) {
                    val payload = json.decodeFromString<BackupPayload>(rawJson)
                    validateSchemaVersion(payload.schemaVersion)
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

        private fun validateSchemaVersion(version: Int) {
            if (version != CURRENT_SCHEMA_VERSION) {
                throw UnsupportedSchemaVersionException(version = version)
            }
        }

        private suspend fun <T> runSuspendCatching(block: suspend () -> T): Result<T> =
            runCatching { block() }.onFailure { throwable ->
                if (throwable is CancellationException) {
                    throw throwable
                }
            }
    }
