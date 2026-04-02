package io.github.pyth0n14n.tastinggenie.data.repository

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.pyth0n14n.tastinggenie.di.IoDispatcher
import io.github.pyth0n14n.tastinggenie.domain.repository.SakeImageRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject

private const val SAKE_IMAGE_DIRECTORY = "images/sakes"

class SakeImageRepositoryImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : SakeImageRepository {
        override suspend fun importImage(
            sourceUri: String,
            previousImageUri: String?,
        ): String =
            withContext(ioDispatcher) {
                val source = Uri.parse(sourceUri)
                val targetFile = createManagedImageFile(source)
                val inputStream =
                    checkNotNull(context.contentResolver.openInputStream(source)) {
                        "Failed to open input stream for $sourceUri"
                    }
                inputStream.use { input ->
                    targetFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                deleteManagedImage(previousImageUri)
                Uri.fromFile(targetFile).toString()
            }

        override suspend fun deleteImage(imageUri: String?) {
            withContext(ioDispatcher) {
                deleteManagedImage(imageUri)
            }
        }

        private fun createManagedImageFile(source: Uri): File {
            val directory = File(context.filesDir, SAKE_IMAGE_DIRECTORY).apply { mkdirs() }
            val extension = source.extensionOrNull(context)
            val filename =
                buildString {
                    append(UUID.randomUUID())
                    extension?.let { append('.').append(it) }
                }
            return File(directory, filename)
        }

        private fun deleteManagedImage(imageUri: String?) {
            val targetFile = imageUri?.toManagedFileOrNull() ?: return
            if (targetFile.exists()) {
                targetFile.delete()
            }
        }

        private fun String.toManagedFileOrNull(): File? {
            val parsed = Uri.parse(this)
            val file =
                parsed.path
                    ?.takeIf { parsed.scheme == SCHEME_FILE }
                    ?.let(::File)
            val canonicalRoot = File(context.filesDir, SAKE_IMAGE_DIRECTORY).canonicalFile
            val canonicalFile = file?.canonicalFile
            val rootPath = canonicalRoot.path + File.separator
            return canonicalFile?.takeIf { candidate ->
                candidate.path.startsWith(rootPath)
            }
        }
    }

private const val SCHEME_FILE = "file"

private fun Uri.extensionOrNull(context: Context): String? {
    val contentResolverExtension =
        context.contentResolver
            .getType(this)
            ?.let { mimeType -> MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) }
    if (!contentResolverExtension.isNullOrBlank()) {
        return contentResolverExtension
    }
    val name = lastPathSegment.orEmpty()
    return name.substringAfterLast('.', "").lowercase().ifBlank { null }
}
