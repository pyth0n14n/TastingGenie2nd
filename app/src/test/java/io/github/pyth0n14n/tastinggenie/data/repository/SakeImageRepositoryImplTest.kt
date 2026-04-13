package io.github.pyth0n14n.tastinggenie.data.repository

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import io.github.pyth0n14n.tastinggenie.data.local.dao.SakeDao
import io.github.pyth0n14n.tastinggenie.data.local.entity.SakeEntity
import io.github.pyth0n14n.tastinggenie.data.local.query.SakeListSummaryRow
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeGrade
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
            val repository = SakeImageRepositoryImpl(context = context, ioDispatcher = Dispatchers.Unconfined)
            val firstSource = createSourceFile(context, name = "first.jpg", content = "first")
            val firstUri = repository.importImage(sourceUri = Uri.fromFile(firstSource).toString())
            val firstManagedFile = requireNotNull(Uri.parse(firstUri).path).let(::File)

            val secondSource = createSourceFile(context, name = "second.jpg", content = "second")
            val secondUri = repository.importImage(sourceUri = Uri.fromFile(secondSource).toString())
            val secondManagedFile = requireNotNull(Uri.parse(secondUri).path).let(::File)

            assertTrue(firstManagedFile.exists())
            assertTrue(secondManagedFile.exists())
            assertEquals("second", secondManagedFile.readText())
        }

    @Test
    fun cleanupUnusedImages_deletesOnlyUnreferencedManagedFiles() =
        runTest {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val usedSource = createSourceFile(context, name = "used.jpg", content = "used")
            val unusedSource = createSourceFile(context, name = "unused.jpg", content = "unused")
            val seedRepository = SakeImageRepositoryImpl(context = context, ioDispatcher = Dispatchers.Unconfined)
            val usedUri = seedRepository.importImage(sourceUri = Uri.fromFile(usedSource).toString())
            val unusedUri = seedRepository.importImage(sourceUri = Uri.fromFile(unusedSource).toString())
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

    private fun createSourceFile(
        context: Context,
        name: String,
        content: String,
    ): File =
        File(context.cacheDir, name).apply {
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

        override suspend fun update(entity: SakeEntity): Int = 0

        override suspend fun updatePinned(
            id: Long,
            isPinned: Boolean,
        ): Int = 0

        override suspend fun deleteById(id: Long): Int = 0
    }
}
