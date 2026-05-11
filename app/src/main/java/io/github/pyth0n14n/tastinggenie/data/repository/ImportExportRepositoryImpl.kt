package io.github.pyth0n14n.tastinggenie.data.repository

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.pyth0n14n.tastinggenie.data.local.AppDatabase
import io.github.pyth0n14n.tastinggenie.data.local.entity.ReviewModeEntity
import io.github.pyth0n14n.tastinggenie.data.local.entity.ReviewModeItemEntity
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
                            val restoredSettings = restoredPayload.settings.toAppSettings()
                            settingsRepository.replaceSettings(restoredSettings)
                            database.withTransaction {
                                database.reviewDao().deleteAll()
                                database.sakeDao().deleteAll()
                                database.reviewModeDao().deleteAllModeItems()
                                database.reviewModeDao().deleteAllModes()
                                database.reviewModeDao().upsertModes(restoredReviewModes)
                                database.reviewModeDao().upsertModeItems(restoredReviewModeItems)
                                database.sakeDao().insertAll(restoredSakes)
                                database.reviewDao().insertAll(restoredReviews)
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
            if (manifest.schemaVersion != CURRENT_SCHEMA_VERSION) {
                throw UnsupportedSchemaVersionException(version = manifest.schemaVersion)
            }
            val payload =
                backupJson.decodeFromString(
                    BackupPayload.serializer(),
                    String(dataBytes, StandardCharsets.UTF_8),
                )
            if (payload.schemaVersion != CURRENT_SCHEMA_VERSION) {
                throw UnsupportedSchemaVersionException(version = payload.schemaVersion)
            }
            validatePayload(payload, entries)
            return payload
        }

        private fun validatePayload(
            payload: BackupPayload,
            entries: Map<String, ByteArray>,
        ) {
            val sakeIds = payload.sakes.map { it.id }
            require(sakeIds.size == sakeIds.toSet().size) { "Backup contains duplicate sake ids" }
            val allowedSakeIds = sakeIds.toSet()
            payload.reviews.firstOrNull { it.sakeId !in allowedSakeIds }?.let { review ->
                throw InvalidBackupReferenceException(sakeId = review.sakeId)
            }
            val reviewIds = payload.reviews.map { it.id }
            require(reviewIds.size == reviewIds.toSet().size) { "Backup contains duplicate review ids" }
            val modeIds = payload.reviewModes.map { it.id }
            require(modeIds.size == modeIds.toSet().size) { "Backup contains duplicate review mode ids" }
            val modeItemIds = payload.reviewModeItems.map { it.modeId to it.itemId }
            require(modeItemIds.size == modeItemIds.toSet().size) { "Backup contains duplicate review mode item ids" }
            val allowedModeIds = modeIds.toSet()
            payload.reviewModeItems.firstOrNull { it.modeId !in allowedModeIds }?.let { item ->
                throw IllegalArgumentException("Review mode item references unknown modeId: ${item.modeId}")
            }
            payload.sakes.flatMap { it.imageUris }.forEach { entryName ->
                require(entryName.isSafeImageEntryName()) { "Invalid backup image path: $entryName" }
                require(entries.containsKey(entryName)) { "Backup image is missing: $entryName" }
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
                    val restoredFile = createManagedImageFile(entryName)
                    restoredFile.writeBytes(checkNotNull(entries[entryName]))
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
            ZipInputStream(input).use { zip ->
                while (true) {
                    val entry = zip.nextEntry ?: break
                    if (!entry.isDirectory) {
                        val output = ByteArrayOutputStream()
                        zip.copyTo(output)
                        entries[entry.name] = output.toByteArray()
                    }
                    zip.closeEntry()
                }
            }
            return entries
        }

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
        autoDeleteUnusedImages = autoDeleteUnusedImages,
        reviewModeId = reviewModeId,
    )

private fun SerializableAppSettings.toAppSettings(): AppSettings =
    AppSettings(
        showHelpHints = showHelpHints,
        showReviewSoundness = showReviewSoundness,
        autoDeleteUnusedImages = autoDeleteUnusedImages,
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
