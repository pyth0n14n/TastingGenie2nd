package io.github.pyth0n14n.tastinggenie.data.repository

import android.content.Context
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.pyth0n14n.tastinggenie.data.local.AppDatabase
import io.github.pyth0n14n.tastinggenie.data.local.entity.ReviewEntity
import io.github.pyth0n14n.tastinggenie.data.local.entity.ReviewModeEntity
import io.github.pyth0n14n.tastinggenie.data.local.entity.ReviewModeItemEntity
import io.github.pyth0n14n.tastinggenie.data.local.entity.SakeEntity
import io.github.pyth0n14n.tastinggenie.domain.model.AppSettings
import io.github.pyth0n14n.tastinggenie.domain.model.BackupManifest
import io.github.pyth0n14n.tastinggenie.domain.model.BackupPayload
import io.github.pyth0n14n.tastinggenie.domain.model.CURRENT_SCHEMA_VERSION
import io.github.pyth0n14n.tastinggenie.domain.model.InvalidBackupReferenceException
import io.github.pyth0n14n.tastinggenie.domain.model.SerializableAppSettings
import io.github.pyth0n14n.tastinggenie.domain.model.SerializableReview
import io.github.pyth0n14n.tastinggenie.domain.model.SerializableReviewMode
import io.github.pyth0n14n.tastinggenie.domain.model.SerializableReviewModeItem
import io.github.pyth0n14n.tastinggenie.domain.model.SerializableSake
import io.github.pyth0n14n.tastinggenie.domain.model.UnsupportedSchemaVersionException
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Aroma
import io.github.pyth0n14n.tastinggenie.domain.model.enums.IntensityLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.OverallReview
import io.github.pyth0n14n.tastinggenie.domain.model.enums.ReviewSoundness
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeColor
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeGrade
import io.github.pyth0n14n.tastinggenie.domain.model.enums.TasteLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Temperature
import io.github.pyth0n14n.tastinggenie.domain.repository.SettingsRepository
import io.github.pyth0n14n.tastinggenie.image.SAKE_MANAGED_IMAGE_DIRECTORY
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
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

private const val SAMPLE_SAKE_ID = 101L
private const val SAMPLE_REVIEW_ID = 202L
private const val UNSUPPORTED_BACKUP_VERSION = 99
private const val EXISTING_SAKE_ID = 1L

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ImportExportRepositoryImplTest {
    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var settingsRepository: BackupSettingsRepository
    private val json = Json { ignoreUnknownKeys = true }

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        File(context.filesDir, SAKE_MANAGED_IMAGE_DIRECTORY).deleteRecursively()
        database =
            Room
                .inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        settingsRepository = BackupSettingsRepository()
    }

    @After
    fun tearDown() {
        database.close()
        File(context.filesDir, SAKE_MANAGED_IMAGE_DIRECTORY).deleteRecursively()
    }

    @Test
    fun exportBackup_writesZipWithDataSettingsAndImages() =
        runTest {
            val repository = createRepository()
            val imageFile = createManagedImage("exported image")
            database.sakeDao().insert(sampleSakeEntity(imageUris = listOf(Uri.fromFile(imageFile).toString())))
            database.reviewDao().insert(sampleReviewEntity())
            database.reviewModeDao().upsertModes(listOf(ReviewModeEntity("normal", "通常", true)))
            database.reviewModeDao().upsertModeItems(listOf(ReviewModeItemEntity("normal", "APPEARANCE_COLOR", true)))
            settingsRepository.replaceSettings(AppSettings(showHelpHints = false, reviewModeId = "normal"))

            val output = ByteArrayOutputStream()
            repository.exportBackup(output).getOrThrow()
            val entries = readZip(output.toByteArray())
            val payload = decodePayload(entries)

            assertTrue(entries.containsKey("manifest.json"))
            assertTrue(entries.containsKey("data.json"))
            assertEquals(CURRENT_SCHEMA_VERSION, payload.schemaVersion)
            assertEquals(false, payload.settings.showHelpHints)
            assertEquals("テスト酒", payload.sakes.single().name)
            assertEquals("桃色", payload.reviews.single().appearanceColorOther)
            assertEquals("normal", payload.reviewModes.single().id)
            val imageEntry =
                payload.sakes
                    .single()
                    .imageUris
                    .single()
            assertTrue(imageEntry.startsWith("images/sakes/"))
            assertArrayEquals("exported image".toByteArray(), entries.getValue(imageEntry))
        }

    @Test
    fun restoreBackup_replacesExistingDataSettingsAndImageUris() =
        runTest {
            val repository = createRepository()
            val existingImage = createManagedImage("old image")
            database.sakeDao().insert(
                sampleSakeEntity(
                    id = EXISTING_SAKE_ID,
                    name = "既存酒",
                    imageUris = listOf(Uri.fromFile(existingImage).toString()),
                ),
            )
            settingsRepository.replaceSettings(AppSettings(showHelpHints = true))
            val payload =
                samplePayload(
                    settings = SerializableAppSettings(showHelpHints = false, reviewModeId = "normal"),
                    sakes = listOf(sampleSerializableSake().copy(imageUris = listOf("images/sakes/source.jpg"))),
                    reviews = listOf(sampleSerializableReview()),
                )
            val backupBytes =
                createBackupZip(
                    payload,
                    images = mapOf("images/sakes/source.jpg" to "new image".toByteArray()),
                )

            repository.restoreBackup(ByteArrayInputStream(backupBytes)).getOrThrow()

            val storedSake = database.sakeDao().getAllOnce().single()
            val storedReview = database.reviewDao().getAllOnce().single()
            assertEquals(SAMPLE_SAKE_ID, storedSake.id)
            assertEquals("テスト酒", storedSake.name)
            assertEquals(SAMPLE_REVIEW_ID, storedReview.id)
            assertEquals(storedSake.id, storedReview.sakeId)
            assertEquals(false, settingsRepository.getCurrentSettings().showHelpHints)
            val restoredUri = storedSake.imageUris.single()
            assertNotEquals("images/sakes/source.jpg", restoredUri)
            assertEquals("new image", File(checkNotNull(Uri.parse(restoredUri).path)).readText())
            assertFalse(existingImage.exists())
        }

    @Test
    fun restoreBackup_missingDataEntryReturnsFailureAndPreservesExistingData() =
        runTest {
            val repository = createRepository()
            database.sakeDao().insert(sampleSakeEntity(id = EXISTING_SAKE_ID, name = "既存酒"))
            val brokenZip =
                createZip(
                    mapOf(
                        "manifest.json" to
                            json
                                .encodeToString(
                                    BackupManifest.serializer(),
                                    BackupManifest(schemaVersion = CURRENT_SCHEMA_VERSION),
                                ).toByteArray(),
                    ),
                )

            val result = repository.restoreBackup(ByteArrayInputStream(brokenZip))

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is SerializationException)
            assertEquals(
                "既存酒",
                database
                    .sakeDao()
                    .getAllOnce()
                    .single()
                    .name,
            )
        }

    @Test
    fun restoreBackup_unsupportedVersionReturnsFailure() =
        runTest {
            val repository = createRepository()
            val backupBytes = createBackupZip(samplePayload(), manifestVersion = UNSUPPORTED_BACKUP_VERSION)

            val result = repository.restoreBackup(ByteArrayInputStream(backupBytes))

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is UnsupportedSchemaVersionException)
        }

    @Test
    fun restoreBackup_unknownSakeReferenceReturnsFailureAndPreservesExistingData() =
        runTest {
            val repository = createRepository()
            database.sakeDao().insert(sampleSakeEntity(id = EXISTING_SAKE_ID, name = "既存酒"))
            val payload = samplePayload(sakes = emptyList(), reviews = listOf(sampleSerializableReview()))
            val backupBytes = createBackupZip(payload)

            val result = repository.restoreBackup(ByteArrayInputStream(backupBytes))

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is InvalidBackupReferenceException)
            assertEquals(
                "既存酒",
                database
                    .sakeDao()
                    .getAllOnce()
                    .single()
                    .name,
            )
        }

    @Test
    fun restoreBackup_missingReferencedImageReturnsFailure() =
        runTest {
            val repository = createRepository()
            val payload =
                samplePayload(
                    sakes =
                        listOf(
                            sampleSerializableSake().copy(imageUris = listOf("images/sakes/missing.jpg")),
                        ),
                )
            val backupBytes = createBackupZip(payload)

            val result = repository.restoreBackup(ByteArrayInputStream(backupBytes))

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        }

    @Test
    fun restoreBackup_settingsFailureReturnsFailureAndPreservesExistingData() =
        runTest {
            val repository = createRepository(settingsRepository = FailingReplaceSettingsRepository())
            database.sakeDao().insert(sampleSakeEntity(id = EXISTING_SAKE_ID, name = "既存酒"))
            val backupBytes = createBackupZip(samplePayload())

            val result = repository.restoreBackup(ByteArrayInputStream(backupBytes))

            assertTrue(result.isFailure)
            assertEquals(
                "既存酒",
                database
                    .sakeDao()
                    .getAllOnce()
                    .single()
                    .name,
            )
        }

    @Test
    fun restoreBackup_duplicateReviewModeItemIdsReturnsFailure() =
        runTest {
            val repository = createRepository()
            val duplicateItem = SerializableReviewModeItem("normal", "APPEARANCE_COLOR", true)
            val payload =
                samplePayload(
                    reviewModeItems = listOf(duplicateItem, duplicateItem.copy(isEnabled = false)),
                )
            val backupBytes = createBackupZip(payload)

            val result = repository.restoreBackup(ByteArrayInputStream(backupBytes))

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        }

    @Test
    fun exportBackup_cancellationPropagates() =
        runTest {
            val repository = createRepository(ioDispatcher = CancellationDispatcher())

            try {
                repository.exportBackup(ByteArrayOutputStream())
                org.junit.Assert.fail("Expected exportBackup to throw CancellationException")
            } catch (_: CancellationException) {
                // Expected.
            }
        }

    private fun createRepository(
        ioDispatcher: CoroutineDispatcher = Dispatchers.Unconfined,
        settingsRepository: SettingsRepository = this.settingsRepository,
    ): ImportExportRepositoryImpl =
        ImportExportRepositoryImpl(
            context = context,
            database = database,
            settingsRepository = settingsRepository,
            json = json,
            ioDispatcher = ioDispatcher,
        )

    private fun createManagedImage(content: String): File {
        val directory = File(context.filesDir, SAKE_MANAGED_IMAGE_DIRECTORY).apply { mkdirs() }
        return File(directory, "image-${System.nanoTime()}.jpg").apply { writeText(content) }
    }

    private fun samplePayload(
        settings: SerializableAppSettings = SerializableAppSettings(reviewModeId = "normal"),
        sakes: List<SerializableSake> = listOf(sampleSerializableSake()),
        reviews: List<SerializableReview> = listOf(sampleSerializableReview()),
        reviewModeItems: List<SerializableReviewModeItem> =
            listOf(SerializableReviewModeItem("normal", "APPEARANCE_COLOR", true)),
    ): BackupPayload =
        BackupPayload(
            schemaVersion = CURRENT_SCHEMA_VERSION,
            sakes = sakes,
            reviews = reviews,
            reviewModes = listOf(SerializableReviewMode("normal", "通常", true)),
            reviewModeItems = reviewModeItems,
            settings = settings,
        )

    private fun sampleSerializableSake(): SerializableSake =
        SerializableSake(
            id = SAMPLE_SAKE_ID,
            name = "テスト酒",
            grade = SakeGrade.JUNMAI.name,
            isPinned = true,
            type = emptyList(),
            maker = "酒蔵A",
            city = "諏訪市",
        )

    private fun sampleSerializableReview(): SerializableReview =
        SerializableReview(
            id = SAMPLE_REVIEW_ID,
            sakeId = SAMPLE_SAKE_ID,
            date = "2026-03-17",
            temperature = Temperature.JOON.name,
            appearanceSoundness = ReviewSoundness.SOUND.name,
            appearanceColor = SakeColor.OTHER.name,
            appearanceColorOther = "桃色",
            aromaSoundness = ReviewSoundness.SOUND.name,
            aromaIntensity = IntensityLevel.MEDIUM.name,
            aromaExamples = listOf(Aroma.MELON.name),
            tasteSoundness = ReviewSoundness.SOUND.name,
            tasteSweetness = TasteLevel.STRONG.name,
            otherOverallReview = OverallReview.GOOD.name,
        )

    private fun sampleSakeEntity(
        id: Long = SAMPLE_SAKE_ID,
        name: String = "テスト酒",
        imageUris: List<String> = emptyList(),
    ): SakeEntity =
        SakeEntity(
            id = id,
            name = name,
            grade = SakeGrade.JUNMAI,
            isPinned = true,
            imageUris = imageUris,
            gradeOther = null,
            type = emptyList(),
            typeOther = null,
            maker = "酒蔵A",
            prefecture = null,
            city = "諏訪市",
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

    private fun sampleReviewEntity(): ReviewEntity =
        ReviewEntity(
            id = SAMPLE_REVIEW_ID,
            sakeId = SAMPLE_SAKE_ID,
            dateEpochDay = LocalDate.parse("2026-03-17").toEpochDay(),
            bar = null,
            price = null,
            volume = null,
            temperature = Temperature.JOON,
            dish = null,
            foodCompatibility = null,
            appearanceSoundness = ReviewSoundness.SOUND,
            appearanceColor = SakeColor.OTHER,
            appearanceColorOther = "桃色",
            appearanceViscosity = null,
            aromaSoundness = ReviewSoundness.SOUND,
            aromaIntensity = IntensityLevel.MEDIUM,
            aromaExamples = listOf(Aroma.MELON),
            aromaMainNote = null,
            aromaComplexity = null,
            tasteSoundness = ReviewSoundness.SOUND,
            tasteAttack = null,
            tasteTextureRoundness = null,
            tasteTextureSmoothness = null,
            tasteTextureNote = null,
            tasteSweetness = TasteLevel.STRONG,
            tasteSourness = null,
            tasteBitterness = null,
            tasteUmami = null,
            tasteDescription = null,
            tasteSweetDryness = null,
            tasteInPalateAromaIntensity = null,
            tasteInPalateAroma = emptyList(),
            tasteAftertaste = null,
            tasteAftertasteNote = null,
            tasteComplexity = null,
            otherIndividuality = null,
            otherCautions = null,
            otherSakeTypes = emptyList(),
            otherFreeComment = null,
            otherOverallReview = OverallReview.GOOD,
        )

    private fun createBackupZip(
        payload: BackupPayload,
        images: Map<String, ByteArray> = emptyMap(),
        manifestVersion: Int = CURRENT_SCHEMA_VERSION,
    ): ByteArray =
        createZip(
            buildMap {
                put(
                    "manifest.json",
                    json
                        .encodeToString(
                            BackupManifest.serializer(),
                            BackupManifest(schemaVersion = manifestVersion),
                        ).toByteArray(),
                )
                put("data.json", json.encodeToString(BackupPayload.serializer(), payload).toByteArray())
                putAll(images)
            },
        )

    private fun createZip(entries: Map<String, ByteArray>): ByteArray {
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            entries.forEach { (name, bytes) ->
                zip.putNextEntry(ZipEntry(name))
                zip.write(bytes)
                zip.closeEntry()
            }
        }
        return output.toByteArray()
    }

    private fun readZip(bytes: ByteArray): Map<String, ByteArray> {
        val entries = mutableMapOf<String, ByteArray>()
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                entries[entry.name] = zip.readBytes()
                zip.closeEntry()
            }
        }
        return entries
    }

    private fun decodePayload(entries: Map<String, ByteArray>): BackupPayload =
        json.decodeFromString(
            BackupPayload.serializer(),
            String(entries.getValue("data.json")),
        )
}

private class BackupSettingsRepository(
    initialSettings: AppSettings = AppSettings(),
) : SettingsRepository {
    private val stream = MutableStateFlow(initialSettings)

    override fun observeSettings(): Flow<AppSettings> = stream

    override suspend fun getCurrentSettings(): AppSettings = stream.value

    override suspend fun updateShowHelpHints(enabled: Boolean) {
        stream.value = stream.value.copy(showHelpHints = enabled)
    }

    override suspend fun updateShowReviewSoundness(enabled: Boolean) {
        stream.value = stream.value.copy(showReviewSoundness = enabled)
    }

    override suspend fun updateAutoDeleteUnusedImages(enabled: Boolean) {
        stream.value = stream.value.copy(autoDeleteUnusedImages = enabled)
    }

    override suspend fun updateReviewMode(modeId: String) {
        stream.value = stream.value.copy(reviewModeId = modeId)
    }

    override suspend fun replaceSettings(settings: AppSettings) {
        stream.value = settings
    }
}

private class FailingReplaceSettingsRepository(
    initialSettings: AppSettings = AppSettings(),
) : SettingsRepository {
    private val stream = MutableStateFlow(initialSettings)

    override fun observeSettings(): Flow<AppSettings> = stream

    override suspend fun getCurrentSettings(): AppSettings = stream.value

    override suspend fun updateShowHelpHints(enabled: Boolean) {
        stream.value = stream.value.copy(showHelpHints = enabled)
    }

    override suspend fun updateShowReviewSoundness(enabled: Boolean) {
        stream.value = stream.value.copy(showReviewSoundness = enabled)
    }

    override suspend fun updateAutoDeleteUnusedImages(enabled: Boolean) {
        stream.value = stream.value.copy(autoDeleteUnusedImages = enabled)
    }

    override suspend fun updateReviewMode(modeId: String) {
        stream.value = stream.value.copy(reviewModeId = modeId)
    }

    override suspend fun replaceSettings(settings: AppSettings) {
        error("settings write failed")
    }
}

private class CancellationDispatcher : CoroutineDispatcher() {
    override fun dispatch(
        context: kotlin.coroutines.CoroutineContext,
        block: Runnable,
    ) = throw CancellationException("cancelled")
}
