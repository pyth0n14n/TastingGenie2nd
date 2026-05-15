package io.github.pyth0n14n.tastinggenie.data.repository

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.pyth0n14n.tastinggenie.data.local.dao.SakeDao
import io.github.pyth0n14n.tastinggenie.data.local.entity.SakeEntity
import io.github.pyth0n14n.tastinggenie.data.local.query.SakeListSummaryRow
import io.github.pyth0n14n.tastinggenie.di.IoDispatcher
import io.github.pyth0n14n.tastinggenie.domain.repository.SakeImageImportException
import io.github.pyth0n14n.tastinggenie.domain.repository.SakeImageRepository
import io.github.pyth0n14n.tastinggenie.image.SAKE_MANAGED_IMAGE_DIRECTORY
import io.github.pyth0n14n.tastinggenie.image.ownedSakeImageFileOrNull
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject

internal const val SAKE_IMAGE_MAX_BYTES = 10L * 1024L * 1024L

class SakeImageRepositoryImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val sakeDao: SakeDao = NoOpSakeDao,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : SakeImageRepository {
        override suspend fun importImage(sourceUri: String): String =
            withContext(ioDispatcher) {
                val source = Uri.parse(sourceUri)
                val imageMimeType = source.requireSupportedImageMimeType(context)
                val targetFile = createManagedImageFile(imageMimeType)
                val importResult =
                    runCatching {
                        val inputStream =
                            checkNotNull(context.contentResolver.openInputStream(source)) {
                                "Failed to open input stream for $sourceUri"
                            }
                        inputStream.use { input ->
                            targetFile.outputStream().use { output ->
                                input.copyToLimited(output = output, maxBytes = SAKE_IMAGE_MAX_BYTES)
                            }
                        }
                    }
                importResult.exceptionOrNull()?.let { targetFile.delete() }
                importResult.getOrThrow()
                Uri.fromFile(targetFile).toString()
            }

        override suspend fun deleteImage(imageUri: String?) {
            withContext(ioDispatcher) {
                deleteManagedImage(imageUri)
            }
        }

        override suspend fun cleanupUnusedImages(): Int =
            withContext(ioDispatcher) {
                val referencedUris = sakeDao.getAllOnce().flatMap { sake -> sake.imageUris }.toSet()
                val managedDirectory = File(context.filesDir, SAKE_MANAGED_IMAGE_DIRECTORY)
                managedDirectory.listFiles().orEmpty().count { file ->
                    deleteIfUnused(file = file, referencedUris = referencedUris)
                }
            }

        private fun createManagedImageFile(mimeType: SakeImageMimeType): File {
            val directory = File(context.filesDir, SAKE_MANAGED_IMAGE_DIRECTORY).apply { mkdirs() }
            val filename = "${UUID.randomUUID()}.${mimeType.extension}"
            return File(directory, filename)
        }

        private fun deleteManagedImage(imageUri: String?) {
            val targetFile = context.ownedSakeImageFileOrNull(imageUri) ?: return
            if (targetFile.exists()) {
                targetFile.delete()
            }
        }

        private fun deleteIfUnused(
            file: File,
            referencedUris: Set<String>,
        ): Boolean {
            val uri = Uri.fromFile(file).toString()
            if (uri in referencedUris) {
                return false
            }
            return file.delete()
        }
    }

private fun Uri.requireSupportedImageMimeType(context: Context): SakeImageMimeType {
    val rawMimeType = context.contentResolver.getType(this)
    val supportedMimeType = SakeImageMimeType.from(rawMimeType)
    if (supportedMimeType != null) {
        return supportedMimeType
    }
    if (rawMimeType != null) {
        throw SakeImageImportException.UnsupportedMimeType(rawMimeType)
    }
    return SakeImageMimeType.fromExtension(path?.substringAfterLast('.', missingDelimiterValue = ""))
        ?: throw SakeImageImportException.UnsupportedMimeType(rawMimeType)
}

private fun java.io.InputStream.copyToLimited(
    output: java.io.OutputStream,
    maxBytes: Long,
): Long {
    var copiedBytes = 0L
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    while (true) {
        val readBytes = read(buffer)
        if (readBytes < 0) {
            return copiedBytes
        }
        copiedBytes += readBytes.toLong()
        if (copiedBytes > maxBytes) {
            throw SakeImageImportException.ImageTooLarge(maxBytes)
        }
        output.write(buffer, 0, readBytes)
    }
}

private enum class SakeImageMimeType(
    val mimeType: String,
    val extension: String,
) {
    JPEG(mimeType = "image/jpeg", extension = "jpg"),
    PNG(mimeType = "image/png", extension = "png"),
    WEBP(mimeType = "image/webp", extension = "webp"),
    ;

    companion object {
        fun from(mimeType: String?): SakeImageMimeType? {
            val normalizedMimeType = mimeType?.substringBefore(';')?.trim()?.lowercase()
            return entries.firstOrNull { it.mimeType == normalizedMimeType }
        }

        fun fromExtension(extension: String?): SakeImageMimeType? =
            when (extension?.trim()?.lowercase()) {
                "jpg", "jpeg" -> JPEG
                "png" -> PNG
                "webp" -> WEBP
                else -> null
            }
    }
}

private object NoOpSakeDao : SakeDao {
    override fun observeAll() = flowOf(emptyList<SakeEntity>())

    override fun observeListSummaries() = flowOf(emptyList<SakeListSummaryRow>())

    override suspend fun getById(id: Long) = null

    override suspend fun getAllOnce() = emptyList<SakeEntity>()

    override suspend fun insert(entity: SakeEntity) = 0L

    override suspend fun insertAll(entities: List<SakeEntity>) = Unit

    override suspend fun deleteAll() = Unit

    override suspend fun update(entity: SakeEntity) = 0

    override suspend fun updatePinned(
        id: Long,
        isPinned: Boolean,
    ) = 0

    override suspend fun deleteById(id: Long) = 0
}
