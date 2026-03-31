package io.github.pyth0n14n.tastinggenie.data.repository

import androidx.room.withTransaction
import io.github.pyth0n14n.tastinggenie.data.local.AppDatabase
import io.github.pyth0n14n.tastinggenie.data.local.entity.ReviewEntity
import io.github.pyth0n14n.tastinggenie.data.local.entity.SakeEntity
import io.github.pyth0n14n.tastinggenie.data.mapper.toSerializable
import io.github.pyth0n14n.tastinggenie.di.IoDispatcher
import io.github.pyth0n14n.tastinggenie.domain.model.BackupPayload
import io.github.pyth0n14n.tastinggenie.domain.model.CURRENT_SCHEMA_VERSION
import io.github.pyth0n14n.tastinggenie.domain.model.InvalidBackupArchiveException
import io.github.pyth0n14n.tastinggenie.domain.model.InvalidBackupReferenceException
import io.github.pyth0n14n.tastinggenie.domain.model.UnsupportedSchemaVersionException
import io.github.pyth0n14n.tastinggenie.domain.repository.ImportExportRepository
import io.github.pyth0n14n.tastinggenie.domain.repository.SakeImageRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.zip.ZipException
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject

private const val BACKUP_MANIFEST_ENTRY = "backup.json"
private const val BACKUP_IMAGE_DIRECTORY = "images/sakes"
private const val ROLLBACK_CLEANUP_FAILURE_MESSAGE = "Rollback image cleanup failed"

class ImportExportRepositoryImpl
    @Inject
    constructor(
        private val database: AppDatabase,
        private val json: Json,
        private val sakeImageRepository: SakeImageRepository,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ImportExportRepository {
        override suspend fun exportBackup(): Result<ByteArray> =
            runSuspendCatching {
                withContext(ioDispatcher) {
                    val snapshot = loadBackupSnapshot()
                    val archive = buildBackupArchive(snapshot.sakes, snapshot.reviews)
                    writeBackupArchive(archive)
                }
            }

        override suspend fun importBackup(rawZip: ByteArray): Result<Unit> =
            runSuspendCatching {
                withContext(ioDispatcher) {
                    val archive = readBackupArchive(rawZip)
                    val payload = json.decodeFromString<BackupPayload>(archive.manifestJson)
                    validateSchemaVersion(payload.schemaVersion)
                    validateReviewReferences(payload)
                    importArchive(payload, archive.imageEntries)
                }
            }

        private suspend fun loadBackupSnapshot(): BackupSnapshot =
            database.withTransaction {
                BackupSnapshot(
                    sakes = database.sakeDao().getAllOnce(),
                    reviews = database.reviewDao().getAllOnce(),
                )
            }

        private suspend fun buildBackupArchive(
            sakes: List<SakeEntity>,
            reviews: List<ReviewEntity>,
        ): BackupArchive {
            val imageEntries = linkedMapOf<String, ByteArray>()
            val serializableSakes =
                sakes.map { sake ->
                    val imagePath =
                        sakeImageRepository.exportImage(sake.imageUri)?.let { image ->
                            val entryPath = buildImageEntryPath(sake.id, image.fileName)
                            imageEntries[entryPath] = image.bytes
                            entryPath
                        }
                    sake.toSerializable(imagePath = imagePath)
                }
            val payload =
                BackupPayload(
                    schemaVersion = CURRENT_SCHEMA_VERSION,
                    sakes = serializableSakes,
                    reviews = reviews.map { review -> review.toSerializable() },
                )
            return BackupArchive(
                manifestJson = json.encodeToString(payload),
                imageEntries = imageEntries,
            )
        }

        @Suppress("TooGenericExceptionCaught")
        private suspend fun importArchive(
            payload: BackupPayload,
            imageEntries: Map<String, ByteArray>,
        ) {
            val importedImageUris = mutableListOf<String>()
            try {
                database.withTransaction {
                    val knownSakes = database.sakeDao().getAllOnce().toMutableList()
                    val knownReviews = database.reviewDao().getAllOnce().toMutableList()
                    val dedupContext =
                        ImportDedupContext(
                            database = database,
                            sakeImageRepository = sakeImageRepository,
                            imageEntries = imageEntries,
                            importedImageUris = importedImageUris,
                        )
                    val importedSakeIds =
                        payload.sakes.associate { sake ->
                            sake.id to
                                resolveOrInsertSake(
                                    sake = sake,
                                    knownSakes = knownSakes,
                                    context = dedupContext,
                                )
                        }
                    payload.reviews.forEach { review ->
                        val importedSakeId =
                            checkNotNull(importedSakeIds[review.sakeId]) {
                                "Review references unknown backup sakeId: ${review.sakeId}"
                            }
                        insertReviewIfNeeded(
                            review = review,
                            sakeId = importedSakeId,
                            knownReviews = knownReviews,
                            database = database,
                        )
                    }
                }
            } catch (cancellationException: CancellationException) {
                cleanupImportedImages(importedImageUris)?.let(cancellationException::addSuppressed)
                throw cancellationException
            } catch (exception: Exception) {
                throw wrapImportFailureWithCleanup(
                    importFailure = exception,
                    cleanupFailure = cleanupImportedImages(importedImageUris),
                )
            }
        }

        @Suppress("TooGenericExceptionCaught")
        private suspend fun cleanupImportedImages(importedImageUris: List<String>): IllegalStateException? {
            if (importedImageUris.isEmpty()) {
                return null
            }
            var aggregatedFailure: IllegalStateException? = null
            importedImageUris.forEach { imageUri ->
                try {
                    sakeImageRepository.deleteImage(imageUri)
                } catch (cancellationException: CancellationException) {
                    throw cancellationException
                } catch (exception: Exception) {
                    aggregatedFailure = aggregatedFailure.appendCleanupFailure(imageUri, exception)
                }
            }
            return aggregatedFailure
        }

        private fun wrapImportFailureWithCleanup(
            importFailure: Exception,
            cleanupFailure: IllegalStateException?,
        ): Exception {
            if (cleanupFailure == null) {
                return importFailure
            }
            return IllegalStateException(
                "Import failed and rollback image cleanup also failed",
                importFailure,
            ).apply {
                addSuppressed(cleanupFailure)
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

private data class BackupSnapshot(
    val sakes: List<SakeEntity>,
    val reviews: List<ReviewEntity>,
)

private data class BackupArchive(
    val manifestJson: String,
    val imageEntries: Map<String, ByteArray>,
)

private fun buildImageEntryPath(
    sakeId: Long,
    fileName: String,
): String = "$BACKUP_IMAGE_DIRECTORY/$sakeId-$fileName"

private fun writeBackupArchive(archive: BackupArchive): ByteArray {
    val output = ByteArrayOutputStream()
    ZipOutputStream(output).use { zipOutput ->
        zipOutput.writeEntry(
            name = BACKUP_MANIFEST_ENTRY,
            bytes = archive.manifestJson.encodeToByteArray(),
        )
        archive.imageEntries.forEach { (entryPath, bytes) ->
            zipOutput.writeEntry(
                name = entryPath,
                bytes = bytes,
            )
        }
    }
    return output.toByteArray()
}

private fun readBackupArchive(rawZip: ByteArray): BackupArchive {
    val entries = readZipEntries(rawZip)
    val manifestBytes = requireManifestEntry(entries)
    return BackupArchive(
        manifestJson = manifestBytes.decodeToString(),
        imageEntries = entries,
    )
}

private fun readZipEntries(rawZip: ByteArray): LinkedHashMap<String, ByteArray> {
    val entries = linkedMapOf<String, ByteArray>()
    try {
        ZipInputStream(ByteArrayInputStream(rawZip)).use { zipInput ->
            var entry = zipInput.nextEntry
            while (entry != null) {
                val entryName = requireEntryName(entry.name)
                require(entries.put(entryName, zipInput.readBytes()) == null) {
                    "Backup archive contains duplicate entry: $entryName"
                }
                zipInput.closeEntry()
                entry = zipInput.nextEntry
            }
        }
    } catch (zipException: ZipException) {
        throw InvalidBackupArchiveException("Backup archive is not a valid ZIP file", zipException)
    } catch (ioException: IOException) {
        throw InvalidBackupArchiveException("Backup archive could not be read", ioException)
    }
    return entries
}

private fun IllegalStateException?.appendCleanupFailure(
    imageUri: String,
    exception: Exception,
): IllegalStateException =
    (this ?: IllegalStateException(ROLLBACK_CLEANUP_FAILURE_MESSAGE)).apply {
        addSuppressed(
            IllegalStateException("Failed to delete imported rollback image: $imageUri", exception),
        )
    }

private fun requireEntryName(entryName: String): String =
    entryName.takeIf { name -> name.isNotBlank() }
        ?: throw InvalidBackupArchiveException("Backup archive contains a blank entry name")

private fun requireManifestEntry(entries: MutableMap<String, ByteArray>): ByteArray =
    entries.remove(BACKUP_MANIFEST_ENTRY)
        ?: throw InvalidBackupArchiveException("Backup archive is missing $BACKUP_MANIFEST_ENTRY")

private fun ZipOutputStream.writeEntry(
    name: String,
    bytes: ByteArray,
) {
    putNextEntry(java.util.zip.ZipEntry(name))
    write(bytes)
    closeEntry()
}
