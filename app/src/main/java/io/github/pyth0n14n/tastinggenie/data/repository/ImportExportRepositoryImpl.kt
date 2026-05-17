package io.github.pyth0n14n.tastinggenie.data.repository

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.pyth0n14n.tastinggenie.data.local.AppDatabase
import io.github.pyth0n14n.tastinggenie.data.local.entity.ReviewModeEntity
import io.github.pyth0n14n.tastinggenie.data.local.entity.ReviewModeItemEntity
import io.github.pyth0n14n.tastinggenie.data.mapper.toLegacyFoodReviewEntityOrNull
import io.github.pyth0n14n.tastinggenie.data.mapper.toRestoredEntity
import io.github.pyth0n14n.tastinggenie.data.mapper.toSerializable
import io.github.pyth0n14n.tastinggenie.di.IoDispatcher
import io.github.pyth0n14n.tastinggenie.domain.model.AppSettings
import io.github.pyth0n14n.tastinggenie.domain.model.BackupManifest
import io.github.pyth0n14n.tastinggenie.domain.model.BackupPayload
import io.github.pyth0n14n.tastinggenie.domain.model.CURRENT_SCHEMA_VERSION
import io.github.pyth0n14n.tastinggenie.domain.model.InvalidBackupReferenceException
import io.github.pyth0n14n.tastinggenie.domain.model.SerializableAppSettings
import io.github.pyth0n14n.tastinggenie.domain.model.SerializableReviewMode
import io.github.pyth0n14n.tastinggenie.domain.model.SerializableReviewModeItem
import io.github.pyth0n14n.tastinggenie.domain.model.UnsupportedSchemaVersionException
import io.github.pyth0n14n.tastinggenie.domain.repository.ImportExportRepository
import io.github.pyth0n14n.tastinggenie.domain.repository.SettingsRepository
import io.github.pyth0n14n.tastinggenie.image.SAKE_MANAGED_IMAGE_DIRECTORY
import io.github.pyth0n14n.tastinggenie.image.ownedSakeImageFileOrNull
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject

private const val MANIFEST_ENTRY = "manifest.json"
private const val DATA_ENTRY = "data.json"
private const val IMAGE_ENTRY_PREFIX = "images/sakes/"
private const val MAX_ZIP_ENTRY_COUNT = 1_000
private const val MAX_MANIFEST_BYTES = 64L * 1024L
private const val MAX_DATA_BYTES = 8L * 1024L * 1024L
private const val MAX_IMAGE_BYTES = 10L * 1024L * 1024L
private const val MAX_TOTAL_EXPANDED_BYTES = 128L * 1024L * 1024L
private const val MAX_IMAGE_ENTRY_COUNT = 500
private const val ZIP_BUFFER_BYTES = 8 * 1024
private const val LEGACY_ZIP_SCHEMA_VERSION = 11
private val supportedBackupSchemaVersions = setOf(LEGACY_ZIP_SCHEMA_VERSION, CURRENT_SCHEMA_VERSION)

@Suppress("TooManyFunctions")
class ImportExportRepositoryImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val database: AppDatabase,
        private val settingsRepository: SettingsRepository,
        json: Json,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ImportExportRepository {
        private val backupJson = Json(json) { encodeDefaults = true }

        override suspend fun exportBackup(output: OutputStream): Result<Unit> =
            runSuspendCatching {
                withContext(ioDispatcher) {
                    val snapshot = createSnapshot()
                    ZipOutputStream(output).use { zip ->
                        zip.writeJsonEntry(
                            MANIFEST_ENTRY,
                            backupJson.encodeToString(
                                BackupManifest.serializer(),
                                BackupManifest(schemaVersion = CURRENT_SCHEMA_VERSION),
                            ),
                        )
                        zip.writeJsonEntry(
                            DATA_ENTRY,
                            backupJson.encodeToString(BackupPayload.serializer(), snapshot.payload),
                        )
                        snapshot.images.forEach { image ->
                            zip.putNextEntry(ZipEntry(image.entryName))
                            image.file.inputStream().use { input -> input.copyTo(zip) }
                            zip.closeEntry()
                        }
                    }
                }
            }

        override suspend fun restoreBackup(input: InputStream): Result<Unit> =
            runSuspendCatching {
                withContext(ioDispatcher) {
                    val entries = readZipEntries(input)
                    val payload = decodeAndValidate(entries)
                    val previousSettings = settingsRepository.getCurrentSettings()
                    val restoredImages = restoreImages(payload, entries)
                    val restoreResult =
                        runCatching {
                            val restoredPayload =
                                payload.withRestoredImageUris(
                                    restoredImages.associate { it.entryName to it.uri },
                                )
                            val restoredReviewModes = restoredPayload.reviewModes.map { it.toEntity() }
                            val restoredReviewModeItems = restoredPayload.reviewModeItems.map { it.toEntity() }
                            val restoredSakes = restoredPayload.sakes.map { it.toRestoredEntity() }
                            val restoredReviews = restoredPayload.reviews.map { it.toRestoredEntity() }
                            val restoredFoodReviews = restoredPayload.foodReviews.map { it.toRestoredEntity() }
                            val restoredSettings = restoredPayload.settings.toAppSettings()
                            settingsRepository.replaceSettings(restoredSettings)
                            database.withTransaction {
                                database.sakeFoodReviewDao().deleteAll()
                                database.reviewDao().deleteAll()
                                database.sakeDao().deleteAll()
                                database.reviewModeDao().deleteAllModeItems()
                                database.reviewModeDao().deleteAllModes()
                                database.reviewModeDao().upsertModes(restoredReviewModes)
                                database.reviewModeDao().upsertModeItems(restoredReviewModeItems)
                                database.sakeDao().insertAll(restoredSakes)
                                database.reviewDao().insertAll(restoredReviews)
                                database.sakeFoodReviewDao().insertAll(restoredFoodReviews)
                            }
                        }.onFailure {
                            runCatching { settingsRepository.replaceSettings(previousSettings) }
                            restoredImages.forEach { image -> image.file.delete() }
                        }
                    restoreResult.getOrThrow()
                    cleanupUnusedManagedImagesAfterRestore()
                }
            }

        private suspend fun createSnapshot(): BackupSnapshot =
            database.withTransaction {
                val sakes = database.sakeDao().getAllOnce()
                val reviews = database.reviewDao().getAllOnce()
                val foodReviews = database.sakeFoodReviewDao().getAllOnce()
                val reviewModes = database.reviewModeDao().getAllModesOnce()
                val reviewModeItems = database.reviewModeDao().getAllModeItemsOnce()
                val imageEntries =
                    sakes
                        .flatMap { it.imageUris }
                        .distinct()
                        .associateWith { uri -> imageEntryName(uri) }
                val payload =
                    BackupPayload(
                        schemaVersion = CURRENT_SCHEMA_VERSION,
                        sakes =
                            sakes.map { sake ->
                                sake.toSerializable().copy(
                                    imageUris = sake.imageUris.map { uri -> requireNotNull(imageEntries[uri]) },
                                )
                            },
                        reviews = reviews.map { it.toSerializable() },
                        foodReviews = foodReviews.map { it.toSerializable() },
                        reviewModes = reviewModes.map { it.toSerializable() },
                        reviewModeItems = reviewModeItems.map { it.toSerializable() },
                        settings = settingsRepository.getCurrentSettings().toSerializable(),
                    )
                BackupSnapshot(
                    payload = payload,
                    images =
                        imageEntries.map { (uri, entryName) ->
                            BackupImage(
                                entryName = entryName,
                                file =
                                    requireNotNull(context.ownedSakeImageFileOrNull(uri)) {
                                        "Backup image is not app-managed: $uri"
                                    }.also { file ->
                                        require(file.exists()) { "Backup image does not exist: $uri" }
                                    },
                            )
                        },
                )
            }

        @Suppress("ThrowsCount")
        private fun decodeAndValidate(entries: Map<String, ByteArray>): BackupPayload {
            val manifestBytes = entries[MANIFEST_ENTRY] ?: throw SerializationException("Missing $MANIFEST_ENTRY")
            val dataBytes = entries[DATA_ENTRY] ?: throw SerializationException("Missing $DATA_ENTRY")
            val manifest =
                backupJson.decodeFromString(
                    BackupManifest.serializer(),
                    String(manifestBytes, StandardCharsets.UTF_8),
                )
            val payload =
                backupJson.decodeFromString(
                    BackupPayload.serializer(),
                    String(dataBytes, StandardCharsets.UTF_8),
                )
            if (manifest.schemaVersion != payload.schemaVersion) {
                throw UnsupportedSchemaVersionException(version = manifest.schemaVersion)
            }
            if (payload.schemaVersion !in supportedBackupSchemaVersions) {
                throw UnsupportedSchemaVersionException(version = payload.schemaVersion)
            }
            val normalizedPayload = payload.toCurrentSchemaPayload()
            validatePayload(normalizedPayload, entries)
            return normalizedPayload
        }

        private fun validatePayload(
            payload: BackupPayload,
            entries: Map<String, ByteArray>,
        ) {
            validateSakeReferences(payload)
            validateReviewModes(payload)
            validateReferencedImages(payload, entries)
        }

        private fun validateSakeReferences(payload: BackupPayload) {
            val sakeIds = payload.sakes.map { it.id }
            require(sakeIds.size == sakeIds.toSet().size) { "Backup contains duplicate sake ids" }
            val allowedSakeIds = sakeIds.toSet()
            payload.reviews.firstOrNull { it.sakeId !in allowedSakeIds }?.let { review ->
                throw InvalidBackupReferenceException(sakeId = review.sakeId)
            }
            payload.foodReviews.firstOrNull { it.sakeId !in allowedSakeIds }?.let { review ->
                throw InvalidBackupReferenceException(sakeId = review.sakeId)
            }
            val reviewIds = payload.reviews.map { it.id }
            require(reviewIds.size == reviewIds.toSet().size) { "Backup contains duplicate review ids" }
            val foodReviewIds = payload.foodReviews.map { it.id }
            require(foodReviewIds.size == foodReviewIds.toSet().size) {
                "Backup contains duplicate food review ids"
            }
        }

        private fun validateReviewModes(payload: BackupPayload) {
            val modeIds = payload.reviewModes.map { it.id }
            require(modeIds.size == modeIds.toSet().size) { "Backup contains duplicate review mode ids" }
            val modeItemIds = payload.reviewModeItems.map { it.modeId to it.itemId }
            require(modeItemIds.size == modeItemIds.toSet().size) { "Backup contains duplicate review mode item ids" }
            val allowedModeIds = modeIds.toSet()
            payload.reviewModeItems.firstOrNull { it.modeId !in allowedModeIds }?.let { item ->
                throw IllegalArgumentException("Review mode item references unknown modeId: ${item.modeId}")
            }
        }

        private fun BackupPayload.toCurrentSchemaPayload(): BackupPayload {
            if (schemaVersion != LEGACY_ZIP_SCHEMA_VERSION) return this
            val legacyFoodReviews =
                reviews.mapNotNull { review ->
                    review.toLegacyFoodReviewEntityOrNull()?.toSerializable()
                }
            return copy(
                schemaVersion = CURRENT_SCHEMA_VERSION,
                foodReviews = foodReviews + legacyFoodReviews,
            )
        }

        private fun validateReferencedImages(
            payload: BackupPayload,
            entries: Map<String, ByteArray>,
        ) {
            val referencedImageEntries = payload.sakes.flatMap { it.imageUris }.distinct()
            require(referencedImageEntries.size <= MAX_IMAGE_ENTRY_COUNT) {
                "Backup contains too many images: ${referencedImageEntries.size}"
            }
            referencedImageEntries.forEach { entryName ->
                require(entryName.isSafeImageEntryName()) { "Invalid backup image path: $entryName" }
                val imageBytes =
                    requireNotNull(entries[entryName]) { "Backup image is missing: $entryName" }
                if (imageBytes.size > MAX_IMAGE_BYTES) {
                    throw SerializationException("Backup image is too large: $entryName")
                }
            }
        }

        private fun restoreImages(
            payload: BackupPayload,
            entries: Map<String, ByteArray>,
        ): List<RestoredBackupImage> =
            payload.sakes
                .flatMap { it.imageUris }
                .distinct()
                .map { entryName ->
                    val imageBytes =
                        requireNotNull(entries[entryName]) { "Backup image is missing: $entryName" }
                    if (imageBytes.size > MAX_IMAGE_BYTES) {
                        throw SerializationException("Backup image is too large: $entryName")
                    }
                    val restoredFile = createManagedImageFile(entryName)
                    restoredFile.writeBytes(imageBytes)
                    RestoredBackupImage(
                        entryName = entryName,
                        file = restoredFile,
                        uri = Uri.fromFile(restoredFile).toString(),
                    )
                }

        private fun createManagedImageFile(entryName: String): File {
            val directory = File(context.filesDir, SAKE_MANAGED_IMAGE_DIRECTORY).apply { mkdirs() }
            val extension = entryName.substringAfterLast('.', "").lowercase().ifBlank { null }
            val fileName =
                buildString {
                    append(UUID.randomUUID())
                    extension?.let { append('.').append(it) }
                }
            return File(directory, fileName)
        }

        private suspend fun cleanupUnusedManagedImages() {
            val referencedUris =
                database
                    .sakeDao()
                    .getAllOnce()
                    .flatMap { it.imageUris }
                    .toSet()
            val managedDirectory = File(context.filesDir, SAKE_MANAGED_IMAGE_DIRECTORY)
            managedDirectory.listFiles().orEmpty().forEach { file ->
                val uri = Uri.fromFile(file).toString()
                if (uri !in referencedUris) {
                    file.delete()
                }
            }
        }

        private suspend fun cleanupUnusedManagedImagesAfterRestore() {
            runSuspendCatching { cleanupUnusedManagedImages() }
        }

        private fun imageEntryName(uri: String): String {
            val file =
                requireNotNull(context.ownedSakeImageFileOrNull(uri)) {
                    "Backup image is not app-managed: $uri"
                }
            val extension = file.extension.lowercase().ifBlank { "bin" }
            return "$IMAGE_ENTRY_PREFIX${UUID.randomUUID()}.$extension"
        }

        private fun readZipEntries(input: InputStream): Map<String, ByteArray> {
            val entries = mutableMapOf<String, ByteArray>()
            var entryCount = 0
            var imageEntryCount = 0
            var totalExpandedBytes = 0L
            val buffer = ByteArray(ZIP_BUFFER_BYTES)
            ZipInputStream(input).use { zip ->
                while (true) {
                    val entry = zip.nextEntry ?: break
                    entryCount = checkedZipEntryCount(entryCount + 1)
                    if (!entry.isDirectory) {
                        imageEntryCount = checkedImageEntryCount(entry, imageEntryCount)
                        val bytes =
                            zip.readLimitedEntry(entry, buffer) { bytesRead ->
                                totalExpandedBytes += bytesRead
                                checkTotalExpandedBytes(totalExpandedBytes)
                            }
                        storeZipEntry(entries, entry.name, bytes)
                    }
                    zip.closeEntry()
                }
            }
            return entries
        }

        private fun checkedZipEntryCount(entryCount: Int): Int {
            if (entryCount > MAX_ZIP_ENTRY_COUNT) {
                throw SerializationException("Backup has too many ZIP entries")
            }
            return entryCount
        }

        private fun checkedImageEntryCount(
            entry: ZipEntry,
            imageEntryCount: Int,
        ): Int {
            if (!entry.name.startsWith(IMAGE_ENTRY_PREFIX)) return imageEntryCount
            val updatedCount = imageEntryCount + 1
            if (updatedCount > MAX_IMAGE_ENTRY_COUNT) {
                throw SerializationException("Backup contains too many images")
            }
            return updatedCount
        }

        private fun checkTotalExpandedBytes(totalExpandedBytes: Long) {
            if (totalExpandedBytes > MAX_TOTAL_EXPANDED_BYTES) {
                throw SerializationException("Backup expanded size is too large")
            }
        }

        private fun storeZipEntry(
            entries: MutableMap<String, ByteArray>,
            name: String,
            bytes: ByteArray,
        ) {
            if (entries.put(name, bytes) != null) {
                throw SerializationException("Backup contains duplicate ZIP entry: $name")
            }
        }

        private fun ZipInputStream.readLimitedEntry(
            entry: ZipEntry,
            buffer: ByteArray,
            onBytesRead: (Long) -> Unit,
        ): ByteArray {
            val maxEntryBytes = entry.maxAllowedBytes()
            val shouldStore = entry.name.shouldStoreEntry()
            val output = if (shouldStore) ByteArrayOutputStream() else null
            var entryBytes = 0L
            while (true) {
                val read = read(buffer)
                if (read == -1) break
                entryBytes += read.toLong()
                onBytesRead(read.toLong())
                if (entryBytes > maxEntryBytes) {
                    throw SerializationException("Backup entry is too large: ${entry.name}")
                }
                output?.write(buffer, 0, read)
            }
            return output?.toByteArray() ?: ByteArray(0)
        }

        private fun ZipEntry.maxAllowedBytes(): Long =
            when {
                name == MANIFEST_ENTRY -> MAX_MANIFEST_BYTES
                name == DATA_ENTRY -> MAX_DATA_BYTES
                name.startsWith(IMAGE_ENTRY_PREFIX) -> MAX_IMAGE_BYTES
                else -> MAX_TOTAL_EXPANDED_BYTES
            }

        private fun String.shouldStoreEntry(): Boolean =
            this == MANIFEST_ENTRY || this == DATA_ENTRY || startsWith(IMAGE_ENTRY_PREFIX)

        private fun ZipOutputStream.writeJsonEntry(
            entryName: String,
            rawJson: String,
        ) {
            putNextEntry(ZipEntry(entryName))
            write(rawJson.toByteArray(StandardCharsets.UTF_8))
            closeEntry()
        }

        private suspend fun <T> runSuspendCatching(block: suspend () -> T): Result<T> =
            runCatching { block() }.onFailure { throwable ->
                if (throwable is CancellationException) {
                    throw throwable
                }
            }
    }

private data class BackupSnapshot(
    val payload: BackupPayload,
    val images: List<BackupImage>,
)

private data class BackupImage(
    val entryName: String,
    val file: File,
)

private data class RestoredBackupImage(
    val entryName: String,
    val file: File,
    val uri: String,
)

private fun BackupPayload.withRestoredImageUris(imageUrisByEntryName: Map<String, String>): BackupPayload =
    copy(
        sakes =
            sakes.map { sake ->
                sake.copy(
                    imageUris =
                        sake.imageUris.map { entryName ->
                            requireNotNull(imageUrisByEntryName[entryName]) {
                                "Backup image was not restored: $entryName"
                            }
                        },
                )
            },
    )

private fun AppSettings.toSerializable(): SerializableAppSettings =
    SerializableAppSettings(
        showHelpHints = showHelpHints,
        showReviewSoundness = showReviewSoundness,
        reviewModeId = reviewModeId,
    )

private fun SerializableAppSettings.toAppSettings(): AppSettings =
    AppSettings(
        showHelpHints = showHelpHints,
        showReviewSoundness = showReviewSoundness,
        reviewModeId = reviewModeId,
    )

private fun ReviewModeEntity.toSerializable(): SerializableReviewMode =
    SerializableReviewMode(
        id = id,
        label = label,
        isBuiltIn = isBuiltIn,
    )

private fun SerializableReviewMode.toEntity(): ReviewModeEntity =
    ReviewModeEntity(
        id = id,
        label = label,
        isBuiltIn = isBuiltIn,
    )

private fun ReviewModeItemEntity.toSerializable(): SerializableReviewModeItem =
    SerializableReviewModeItem(
        modeId = modeId,
        itemId = itemId,
        isEnabled = isEnabled,
    )

private fun SerializableReviewModeItem.toEntity(): ReviewModeItemEntity =
    ReviewModeItemEntity(
        modeId = modeId,
        itemId = itemId,
        isEnabled = isEnabled,
    )

private fun String.isSafeImageEntryName(): Boolean =
    startsWith(IMAGE_ENTRY_PREFIX) &&
        !contains("..") &&
        !startsWith("/") &&
        !startsWith("\\") &&
        !contains("\\")
