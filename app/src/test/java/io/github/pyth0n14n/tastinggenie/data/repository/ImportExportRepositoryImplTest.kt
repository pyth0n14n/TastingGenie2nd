package io.github.pyth0n14n.tastinggenie.data.repository

import android.content.Context
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.pyth0n14n.tastinggenie.data.local.AppDatabase
import io.github.pyth0n14n.tastinggenie.data.local.entity.ReviewEntity
import io.github.pyth0n14n.tastinggenie.data.local.entity.SakeEntity
import io.github.pyth0n14n.tastinggenie.domain.model.BackupPayload
import io.github.pyth0n14n.tastinggenie.domain.model.CURRENT_SCHEMA_VERSION
import io.github.pyth0n14n.tastinggenie.domain.model.InvalidBackupArchiveException
import io.github.pyth0n14n.tastinggenie.domain.model.InvalidBackupReferenceException
import io.github.pyth0n14n.tastinggenie.domain.model.SerializableReview
import io.github.pyth0n14n.tastinggenie.domain.model.SerializableSake
import io.github.pyth0n14n.tastinggenie.domain.model.UnsupportedSchemaVersionException
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Aroma
import io.github.pyth0n14n.tastinggenie.domain.model.enums.IntensityLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.OverallReview
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeColor
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeGrade
import io.github.pyth0n14n.tastinggenie.domain.model.enums.TasteLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Temperature
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.LocalDate
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

private const val BACKUP_MANIFEST_ENTRY = "backup.json"
private const val SAMPLE_IMAGE_ENTRY = "images/sakes/101-sample.jpg"

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ImportExportRepositoryImplTest {
    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var sakeImageRepository: SakeImageRepositoryImpl
    private val json = Json { ignoreUnknownKeys = true }

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database =
            Room
                .inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        sakeImageRepository = SakeImageRepositoryImpl(context = context, ioDispatcher = Dispatchers.Unconfined)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun exportBackup_serializesStoredSakesReviewsAndImages() =
        runTest {
            val repository = createRepository()
            val imageUri = createManagedImageUri(content = "export-image")
            database.sakeDao().insert(sampleSakeEntity(imageUri = imageUri))
            database.reviewDao().insert(sampleReviewEntity())

            val rawZip = repository.exportBackup().getOrThrow()
            val zipEntries = unzipEntries(rawZip)
            val payload =
                json.decodeFromString<BackupPayload>(
                    zipEntries.getValue(BACKUP_MANIFEST_ENTRY).decodeToString(),
                )

            assertEquals(CURRENT_SCHEMA_VERSION, payload.schemaVersion)
            assertEquals(1, payload.sakes.size)
            assertEquals("テスト酒", payload.sakes.single().name)
            assertEquals(1, payload.reviews.size)
            val imagePath = requireNotNull(payload.sakes.single().imagePath)
            assertTrue(imagePath.startsWith("images/sakes/101-"))
            assertArrayEquals("export-image".encodeToByteArray(), zipEntries.getValue(imagePath))
        }

    @Test
    fun importBackup_roundTripCreatesFreshLocalIdsAndRestoresImages() =
        runTest {
            val repository = createRepository()
            val payload =
                BackupPayload(
                    schemaVersion = CURRENT_SCHEMA_VERSION,
                    sakes = listOf(sampleSerializableSake(imagePath = SAMPLE_IMAGE_ENTRY)),
                    reviews = listOf(sampleSerializableReview()),
                )

            repository
                .importBackup(
                    createBackupZip(
                        payload = payload,
                        imageEntries = mapOf(SAMPLE_IMAGE_ENTRY to "import-image".encodeToByteArray()),
                    ),
                ).getOrThrow()

            val storedSake = database.sakeDao().getAllOnce().single()
            val storedReview = database.reviewDao().getAllOnce().single()
            assertEquals("テスト酒", storedSake.name)
            assertEquals(SakeGrade.JUNMAI, storedSake.grade)
            assertTrue(storedSake.imageUri?.isNotBlank() == true)
            assertEquals("import-image", sakeImageRepository.exportImage(storedSake.imageUri)?.bytes?.decodeToString())
            assertTrue(storedSake.id != sampleSerializableSake().id)
            assertEquals(storedSake.id, storedReview.sakeId)
            assertTrue(storedReview.id != sampleSerializableReview().id)
            assertEquals(LocalDate.parse("2026-03-17").toEpochDay(), storedReview.dateEpochDay)
        }

    @Test
    fun importBackup_unsupportedSchemaVersion_returnsFailure() =
        runTest {
            val repository = createRepository()
            val payload =
                BackupPayload(
                    schemaVersion = 99,
                    sakes = emptyList(),
                    reviews = emptyList(),
                )

            val result = repository.importBackup(createBackupZip(payload = payload))

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is UnsupportedSchemaVersionException)
        }

    @Test
    fun importBackup_invalidArchive_returnsFailure() =
        runTest {
            val repository = createRepository()

            val result = repository.importBackup("not-a-zip".encodeToByteArray())

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is InvalidBackupArchiveException)
        }

    @Test
    fun importBackup_brokenManifest_returnsFailure() =
        runTest {
            val repository = createRepository()

            val result =
                repository.importBackup(
                    createRawZip(
                        entries = mapOf(BACKUP_MANIFEST_ENTRY to "{not-json".encodeToByteArray()),
                    ),
                )

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is SerializationException)
        }

    @Test
    fun importBackup_unknownSakeReference_returnsFailure() =
        runTest {
            val repository = createRepository()
            val payload =
                BackupPayload(
                    schemaVersion = CURRENT_SCHEMA_VERSION,
                    sakes = emptyList(),
                    reviews = listOf(sampleSerializableReview()),
                )

            val result = repository.importBackup(createBackupZip(payload = payload))

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is InvalidBackupReferenceException)
            assertTrue(database.reviewDao().getAllOnce().isEmpty())
        }

    @Test
    fun importBackup_missingImageEntry_returnsFailure() =
        runTest {
            val repository = createRepository()
            val payload =
                BackupPayload(
                    schemaVersion = CURRENT_SCHEMA_VERSION,
                    sakes = listOf(sampleSerializableSake(imagePath = SAMPLE_IMAGE_ENTRY)),
                    reviews = emptyList(),
                )

            val result = repository.importBackup(createBackupZip(payload = payload))

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is IllegalArgumentException)
            assertTrue(database.sakeDao().getAllOnce().isEmpty())
        }

    @Test
    fun importBackup_existingLocalIdsDoNotOverwriteUnrelatedRows() =
        runTest {
            val repository = createRepository()
            database.sakeDao().insert(sampleSakeEntity(imageUri = createManagedImageUri(content = "existing")))
            database.reviewDao().insert(sampleReviewEntity())

            val payload =
                BackupPayload(
                    schemaVersion = CURRENT_SCHEMA_VERSION,
                    sakes = listOf(sampleSerializableSake(name = "バックアップ酒")),
                    reviews = listOf(sampleSerializableReview().copy(comment = "imported")),
                )

            repository.importBackup(createBackupZip(payload = payload)).getOrThrow()

            val storedSakes = database.sakeDao().getAllOnce()
            val storedReviews = database.reviewDao().getAllOnce()
            assertEquals(2, storedSakes.size)
            assertEquals(2, storedReviews.size)
            assertTrue(storedSakes.any { sake -> sake.id == sampleSakeEntity().id && sake.name == "テスト酒" })
            assertTrue(storedSakes.any { sake -> sake.id != sampleSerializableSake().id && sake.name == "バックアップ酒" })
            assertTrue(storedReviews.any { review -> review.id == sampleReviewEntity().id && review.comment == null })
            assertTrue(
                storedReviews.any { review ->
                    review.id != sampleSerializableReview().id && review.comment == "imported"
                },
            )
        }

    @Test
    fun importBackup_blankSakeName_returnsFailure() =
        runTest {
            val repository = createRepository()
            val payload =
                BackupPayload(
                    schemaVersion = CURRENT_SCHEMA_VERSION,
                    sakes = listOf(sampleSerializableSake(name = "   ")),
                    reviews = emptyList(),
                )

            val result = repository.importBackup(createBackupZip(payload = payload))

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is IllegalArgumentException)
            assertTrue(database.sakeDao().getAllOnce().isEmpty())
        }

    @Test
    fun importBackup_outOfRangeViscosity_returnsFailure() =
        runTest {
            val repository = createRepository()
            val payload =
                BackupPayload(
                    schemaVersion = CURRENT_SCHEMA_VERSION,
                    sakes = listOf(sampleSerializableSake()),
                    reviews = listOf(sampleSerializableReview().copy(viscosity = 4)),
                )

            val result = repository.importBackup(createBackupZip(payload = payload))

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is IllegalArgumentException)
            assertTrue(database.reviewDao().getAllOnce().isEmpty())
        }

    @Test
    fun exportBackup_cancellationPropagates() =
        runTest {
            val repository = createRepository(ioDispatcher = CancellationDispatcher())

            try {
                repository.exportBackup()
                org.junit.Assert.fail("Expected exportBackup to throw CancellationException")
            } catch (_: CancellationException) {
                // Expected.
            }
        }

    @Test
    fun importBackup_cancellationPropagates() =
        runTest {
            val repository = createRepository(ioDispatcher = CancellationDispatcher())

            try {
                repository.importBackup(
                    createBackupZip(
                        payload =
                            BackupPayload(
                                schemaVersion = CURRENT_SCHEMA_VERSION,
                                sakes = emptyList(),
                                reviews = emptyList(),
                            ),
                    ),
                )
                org.junit.Assert.fail("Expected importBackup to throw CancellationException")
            } catch (_: CancellationException) {
                // Expected.
            }
        }

    private fun createRepository(
        ioDispatcher: CoroutineDispatcher = Dispatchers.Unconfined,
    ): ImportExportRepositoryImpl =
        ImportExportRepositoryImpl(
            database = database,
            json = json,
            sakeImageRepository = sakeImageRepository,
            ioDispatcher = ioDispatcher,
        )

    private suspend fun createManagedImageUri(content: String): String {
        val source = File(context.cacheDir, "backup-${System.nanoTime()}.jpg").apply { writeText(content) }
        return sakeImageRepository.importImage(sourceUri = Uri.fromFile(source).toString())
    }

    private fun sampleSerializableSake(
        name: String = "テスト酒",
        imagePath: String? = null,
    ): SerializableSake =
        SerializableSake(
            id = 101L,
            name = name,
            grade = SakeGrade.JUNMAI.name,
            imagePath = imagePath,
            gradeOther = null,
            type = emptyList(),
            maker = "酒蔵A",
        )

    private fun sampleSerializableReview(): SerializableReview =
        SerializableReview(
            id = 202L,
            sakeId = 101L,
            date = "2026-03-17",
            temperature = Temperature.JOON.name,
            color = SakeColor.CLEAR.name,
            intensity = IntensityLevel.MEDIUM.name,
            scentTop = listOf(Aroma.MELON.name),
            sweet = TasteLevel.STRONG.name,
            review = OverallReview.GOOD.name,
        )

    private fun sampleSakeEntity(imageUri: String? = null): SakeEntity =
        sampleSerializableSake().let { sake ->
            SakeEntity(
                id = sake.id,
                name = sake.name,
                grade = SakeGrade.valueOf(sake.grade),
                imageUri = imageUri,
                gradeOther = sake.gradeOther,
                type = emptyList(),
                typeOther = null,
                maker = sake.maker,
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

    private fun sampleReviewEntity(): ReviewEntity =
        ReviewEntity(
            id = 202L,
            sakeId = 101L,
            dateEpochDay = LocalDate.parse("2026-03-17").toEpochDay(),
            bar = null,
            price = null,
            volume = null,
            temperature = Temperature.JOON,
            color = SakeColor.CLEAR,
            viscosity = null,
            intensity = IntensityLevel.MEDIUM,
            scentTop = listOf(Aroma.MELON),
            scentBase = emptyList(),
            scentMouth = emptyList(),
            sweet = TasteLevel.STRONG,
            sour = null,
            bitter = null,
            umami = null,
            sharp = null,
            scene = null,
            dish = null,
            comment = null,
            review = OverallReview.GOOD,
        )
}

private fun createBackupZip(
    payload: BackupPayload,
    imageEntries: Map<String, ByteArray> = emptyMap(),
): ByteArray =
    createRawZip(
        entries =
            linkedMapOf<String, ByteArray>().apply {
                put(
                    BACKUP_MANIFEST_ENTRY,
                    Json.encodeToString(BackupPayload.serializer(), payload).encodeToByteArray(),
                )
                putAll(imageEntries)
            },
    )

private fun createRawZip(entries: Map<String, ByteArray>): ByteArray {
    val output = ByteArrayOutputStream()
    ZipOutputStream(output).use { zipOutput ->
        entries.forEach { (name, bytes) ->
            zipOutput.putNextEntry(ZipEntry(name))
            zipOutput.write(bytes)
            zipOutput.closeEntry()
        }
    }
    return output.toByteArray()
}

private fun unzipEntries(zipBytes: ByteArray): Map<String, ByteArray> {
    val entries = linkedMapOf<String, ByteArray>()
    ZipInputStream(ByteArrayInputStream(zipBytes)).use { zipInput ->
        var entry = zipInput.nextEntry
        while (entry != null) {
            entries[entry.name] = zipInput.readBytes()
            zipInput.closeEntry()
            entry = zipInput.nextEntry
        }
    }
    return entries
}

private class CancellationDispatcher : CoroutineDispatcher() {
    override fun dispatch(
        context: kotlin.coroutines.CoroutineContext,
        block: Runnable,
    ) = throw CancellationException("cancelled")
}
