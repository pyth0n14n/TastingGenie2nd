package io.github.pyth0n14n.tastinggenie.data.repository

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import io.github.pyth0n14n.tastinggenie.data.local.dao.SakeDao
import io.github.pyth0n14n.tastinggenie.data.local.entity.SakeEntity
import io.github.pyth0n14n.tastinggenie.data.local.query.SakeListSummaryRow
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeGrade
import io.github.pyth0n14n.tastinggenie.domain.repository.SakeImageImportException
import io.github.pyth0n14n.tastinggenie.image.SAKE_MANAGED_IMAGE_DIRECTORY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SakeImageRepositoryImplTest {
    @Test
    fun importImage_copiesEachSelectionIntoManagedDirectory() =
        runTest {
            val context = ApplicationProvider.getApplicationContext<Context>()
            clearManagedImages(context)
            val repository = SakeImageRepositoryImpl(context = context, ioDispatcher = Dispatchers.Unconfined)
            val firstSource = createSourceImage(context, name = "first.any", mimeType = "image/jpeg", content = "first")
            val firstUri = repository.importImage(sourceUri = firstSource.toString())
            val firstManagedFile = requireNotNull(Uri.parse(firstUri).path).let(::File)

            val secondSource =
                createSourceImage(context, name = "second.txt", mimeType = "image/jpeg", content = "second")
            val secondUri = repository.importImage(sourceUri = secondSource.toString())
            val secondManagedFile = requireNotNull(Uri.parse(secondUri).path).let(::File)

            assertTrue(firstManagedFile.exists())
            assertTrue(secondManagedFile.exists())
            assertEquals("jpg", firstManagedFile.extension)
            assertEquals("jpg", secondManagedFile.extension)
            assertEquals("second", secondManagedFile.readText())
        }

    @Test
    fun importImage_rejectsNonImageMimeType() =
        runTest {
            val context = ApplicationProvider.getApplicationContext<Context>()
            clearManagedImages(context)
            val repository = SakeImageRepositoryImpl(context = context, ioDispatcher = Dispatchers.Unconfined)
            val source = createSourceImage(context, name = "not-image.jpg", mimeType = "text/plain", content = "text")

            try {
                repository.importImage(sourceUri = source.toString())
                fail("Expected unsupported MIME type to fail")
            } catch (exception: SakeImageImportException.UnsupportedMimeType) {
                assertEquals("Unsupported image MIME type: text/plain", exception.message)
            }

            assertEquals(0, managedImageFiles(context).size)
        }

    @Test
    fun importImage_deletesPartialFileWhenImageExceedsMaximumSize() =
        runTest {
            val context = ApplicationProvider.getApplicationContext<Context>()
            clearManagedImages(context)
            val repository = SakeImageRepositoryImpl(context = context, ioDispatcher = Dispatchers.Unconfined)
            val source =
                createSourceImage(
                    context = context,
                    name = "large.png",
                    mimeType = "image/png",
                    sizeBytes = SAKE_IMAGE_MAX_BYTES + 1,
                )

            try {
                repository.importImage(sourceUri = source.toString())
                fail("Expected oversized image to fail")
            } catch (exception: SakeImageImportException.ImageTooLarge) {
                assertEquals("Image exceeds maximum size: $SAKE_IMAGE_MAX_BYTES bytes", exception.message)
            }

            assertEquals(0, managedImageFiles(context).size)
        }

    @Test
    fun importImage_usesPngExtensionForPngMimeType() =
        runTest {
            val context = ApplicationProvider.getApplicationContext<Context>()
            clearManagedImages(context)
            val repository = SakeImageRepositoryImpl(context = context, ioDispatcher = Dispatchers.Unconfined)
            val source = createSourceImage(context, name = "source.jpg", mimeType = "image/png", content = "png")

            val importedUri = repository.importImage(sourceUri = source.toString())
            val managedFile = requireNotNull(Uri.parse(importedUri).path).let(::File)

            assertTrue(managedFile.exists())
            assertEquals("png", managedFile.extension)
            assertEquals("png", managedFile.readText())
        }

    @Test
    fun importImage_usesFileExtensionWhenMimeTypeIsMissing() =
        runTest {
            val context = ApplicationProvider.getApplicationContext<Context>()
            clearManagedImages(context)
            val repository = SakeImageRepositoryImpl(context = context, ioDispatcher = Dispatchers.Unconfined)
            val source = createSourceFile(context, name = "captured.jpg", content = "captured")

            val importedUri = repository.importImage(sourceUri = Uri.fromFile(source).toString())
            val managedFile = requireNotNull(Uri.parse(importedUri).path).let(::File)

            assertTrue(managedFile.exists())
            assertEquals("jpg", managedFile.extension)
            assertEquals("captured", managedFile.readText())
        }

    @Test
    fun cleanupUnusedImages_deletesOnlyUnreferencedManagedFiles() =
        runTest {
            val context = ApplicationProvider.getApplicationContext<Context>()
            clearManagedImages(context)
            val usedSource = createSourceImage(context, name = "used.jpg", mimeType = "image/jpeg", content = "used")
            val unusedSource =
                createSourceImage(context, name = "unused.jpg", mimeType = "image/jpeg", content = "unused")
            val seedRepository = SakeImageRepositoryImpl(context = context, ioDispatcher = Dispatchers.Unconfined)
            val usedUri = seedRepository.importImage(sourceUri = usedSource.toString())
            val unusedUri = seedRepository.importImage(sourceUri = unusedSource.toString())
            val repository =
                SakeImageRepositoryImpl(
                    context = context,
                    sakeDao = FakeSakeDao(referencedUris = setOf(usedUri)),
                    ioDispatcher = Dispatchers.Unconfined,
                )

            val deletedCount = repository.cleanupUnusedImages()

            assertEquals(1, deletedCount)
            assertTrue(requireNotNull(Uri.parse(usedUri).path).let(::File).exists())
            assertFalse(requireNotNull(Uri.parse(unusedUri).path).let(::File).exists())
        }

    @Test
    fun deleteImage_ignoresFilesOutsideManagedDirectory() =
        runTest {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val repository = SakeImageRepositoryImpl(context = context, ioDispatcher = Dispatchers.Unconfined)
            val unmanagedFile = createSourceFile(context, name = "outside.jpg", content = "keep")

            repository.deleteImage(Uri.fromFile(unmanagedFile).toString())

            assertTrue(unmanagedFile.exists())
        }

    @Test
    fun deleteImage_deletesOwnedCaptureFilesInCacheDirectory() =
        runTest {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val repository = SakeImageRepositoryImpl(context = context, ioDispatcher = Dispatchers.Unconfined)
            val captureFile =
                File(context.cacheDir, "images/sakes/capture/captured.jpg").apply {
                    parentFile?.mkdirs()
                    writeText("captured")
                }

            repository.deleteImage(Uri.fromFile(captureFile).toString())

            assertFalse(captureFile.exists())
        }

    private fun createSourceImage(
        context: Context,
        name: String,
        mimeType: String,
        content: String,
    ): Uri {
        val sourceFile = createSourceFile(context = context, name = name, content = content)
        return TestImageContentProvider.register(file = sourceFile, mimeType = mimeType)
    }

    private fun createSourceImage(
        context: Context,
        name: String,
        mimeType: String,
        sizeBytes: Long,
    ): Uri {
        val sourceFile = createSourceFile(context = context, name = name, sizeBytes = sizeBytes)
        return TestImageContentProvider.register(file = sourceFile, mimeType = mimeType)
    }

    private fun createSourceFile(
        context: Context,
        name: String,
        sizeBytes: Long,
    ): File =
        File(context.cacheDir, "source-images/$name").apply {
            parentFile?.mkdirs()
            outputStream().use { output ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE) { 'x'.code.toByte() }
                var remainingBytes = sizeBytes
                while (remainingBytes > 0L) {
                    val writeBytes = minOf(buffer.size.toLong(), remainingBytes).toInt()
                    output.write(buffer, 0, writeBytes)
                    remainingBytes -= writeBytes.toLong()
                }
            }
        }

    private fun clearManagedImages(context: Context) {
        managedImageFiles(context).forEach { file -> file.delete() }
    }

    private fun managedImageFiles(context: Context): List<File> =
        File(context.filesDir, SAKE_MANAGED_IMAGE_DIRECTORY).listFiles().orEmpty().toList()

    private fun createSourceFile(
        context: Context,
        name: String,
        content: String,
    ): File =
        File(context.cacheDir, "source-images/$name").apply {
            parentFile?.mkdirs()
            writeText(content)
        }

    private class FakeSakeDao(
        referencedUris: Set<String>,
    ) : SakeDao {
        private val rows =
            referencedUris.mapIndexed { index, imageUri ->
                SakeEntity(
                    id = index.toLong() + 1L,
                    name = "酒$index",
                    grade = SakeGrade.JUNMAI,
                    imageUris = listOf(imageUri),
                    gradeOther = null,
                    type = emptyList(),
                    typeOther = null,
                    maker = null,
                    prefecture = null,
                    alcohol = null,
                    kojiMai = null,
                    kojiPolish = null,
                    kakeMai = null,
                    kakePolish = null,
                    sakeDegree = null,
                    acidity = null,
                    amino = null,
                    yeast = null,
                    water = null,
                )
            }

        override fun observeAll(): Flow<List<SakeEntity>> = flowOf(rows)

        override fun observeListSummaries(): Flow<List<SakeListSummaryRow>> = flowOf(emptyList())

        override suspend fun getById(id: Long): SakeEntity? = rows.firstOrNull { it.id == id }

        override suspend fun getAllOnce(): List<SakeEntity> = rows

        override suspend fun insert(entity: SakeEntity): Long = entity.id

        override suspend fun insertAll(entities: List<SakeEntity>) = Unit

        override suspend fun deleteAll() = Unit

        override suspend fun update(entity: SakeEntity): Int = 0

        override suspend fun updatePinned(
            id: Long,
            isPinned: Boolean,
        ): Int = 0

        override suspend fun deleteById(id: Long): Int = 0
    }
}
