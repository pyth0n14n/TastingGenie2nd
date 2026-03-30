package io.github.pyth0n14n.tastinggenie.data.repository

import androidx.room.withTransaction
import io.github.pyth0n14n.tastinggenie.data.local.AppDatabase
import io.github.pyth0n14n.tastinggenie.data.local.entity.ReviewEntity
import io.github.pyth0n14n.tastinggenie.data.local.entity.SakeEntity
import io.github.pyth0n14n.tastinggenie.data.mapper.toImportedEntity
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
import java.util.zip.ZipException
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject

private const val BACKUP_MANIFEST_ENTRY = "backup.json"
private const val BACKUP_IMAGE_DIRECTORY = "images/sakes"

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

        private suspend fun importArchive(
            payload: BackupPayload,
            imageEntries: Map<String, ByteArray>,
        ) {
            val importedImageUris = mutableListOf<String>()
            var isCommitted = false
            try {
                database.withTransaction {
                    val importedSakeIds =
                        payload.sakes.associate { sake ->
                            val imageUri =
                                importSakeImage(
                                    imagePath = sake.imagePath,
                                    imageEntries = imageEntries,
                                    importedImageUris = importedImageUris,
                                )
                            sake.id to database.sakeDao().insert(sake.toImportedEntity(imageUri = imageUri))
                        }
                    payload.reviews.forEach { review ->
                        val importedSakeId =
                            checkNotNull(importedSakeIds[review.sakeId]) {
                                "Review references unknown backup sakeId: ${review.sakeId}"
                            }
                        database.reviewDao().insert(review.toImportedEntity(sakeId = importedSakeId))
                    }
                }
                isCommitted = true
            } finally {
                if (!isCommitted) {
                    importedImageUris.forEach { imageUri ->
                        runCatching { sakeImageRepository.deleteImage(imageUri) }
                    }
                }
            }
        }

        private suspend fun importSakeImage(
            imagePath: String?,
            imageEntries: Map<String, ByteArray>,
            importedImageUris: MutableList<String>,
        ): String? {
            if (imagePath.isNullOrBlank()) {
                return null
            }
            val imageBytes =
                requireNotNull(imageEntries[imagePath]) {
                    "Backup archive is missing image entry: $imagePath"
                }
            val importedImageUri =
                sakeImageRepository.importImageBytes(
                    filenameHint = imagePath.substringAfterLast('/'),
                    bytes = imageBytes,
                )
            importedImageUris += importedImageUri
            return importedImageUri
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
    }
    return entries
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
