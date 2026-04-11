package io.github.pyth0n14n.tastinggenie.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.pyth0n14n.tastinggenie.data.local.AppDatabase
import io.github.pyth0n14n.tastinggenie.data.local.entity.ReviewEntity
import io.github.pyth0n14n.tastinggenie.data.local.entity.SakeEntity
import io.github.pyth0n14n.tastinggenie.domain.model.BackupPayload
import io.github.pyth0n14n.tastinggenie.domain.model.CURRENT_SCHEMA_VERSION
import io.github.pyth0n14n.tastinggenie.domain.model.InvalidBackupReferenceException
import io.github.pyth0n14n.tastinggenie.domain.model.SerializableReview
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ImportExportRepositoryImplTest {
    private lateinit var database: AppDatabase
    private val json = Json { ignoreUnknownKeys = true }

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
            Room
                .inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                .allowMainThreadQueries()
                .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun exportJson_serializesStoredSakesAndReviews() =
        runTest {
            val repository = createRepository()
            database.sakeDao().insert(sampleSakeEntity())
            database.reviewDao().insert(sampleReviewEntity())

            val rawJson = repository.exportJson().getOrThrow()
            val payload = json.decodeFromString<BackupPayload>(rawJson)

            assertEquals(CURRENT_SCHEMA_VERSION, payload.schemaVersion)
            assertEquals(1, payload.sakes.size)
            assertEquals("テスト酒", payload.sakes.single().name)
            assertEquals(1, payload.reviews.size)
        }

    @Test
    fun importJson_roundTripCreatesFreshLocalIdsAndDropsImageUris() =
        runTest {
            val repository = createRepository()
            val payload =
                BackupPayload(
                    schemaVersion = CURRENT_SCHEMA_VERSION,
                    sakes = listOf(sampleSerializableSake()),
                    reviews = listOf(sampleSerializableReview()),
                )

            repository.importJson(json.encodeToString(payload)).getOrThrow()

            val storedSake = database.sakeDao().getAllOnce().single()
            val storedReview = database.reviewDao().getAllOnce().single()
            assertEquals("テスト酒", storedSake.name)
            assertEquals(SakeGrade.JUNMAI, storedSake.grade)
            assertEquals(null, storedSake.imageUri)
            assertEquals(null, storedSake.gradeOther)
            assertTrue(storedSake.id != sampleSerializableSake().id)
            assertEquals(storedSake.id, storedReview.sakeId)
            assertTrue(storedReview.id != sampleSerializableReview().id)
            assertEquals(LocalDate.parse("2026-03-17").toEpochDay(), storedReview.dateEpochDay)
        }

    @Test
    fun importJson_unsupportedSchemaVersion_returnsFailure() =
        runTest {
            val repository = createRepository()
            val payload =
                BackupPayload(
                    schemaVersion = 99,
                    sakes = emptyList(),
                    reviews = emptyList(),
                )

            val result = repository.importJson(json.encodeToString(payload))

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is UnsupportedSchemaVersionException)
        }

    @Test
    fun importJson_brokenJson_returnsFailure() =
        runTest {
            val repository = createRepository()
            val result = repository.importJson("{not-json")

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is SerializationException)
        }

    @Test
    fun importJson_unknownSakeReference_returnsFailure() =
        runTest {
            val repository = createRepository()
            val payload =
                BackupPayload(
                    schemaVersion = CURRENT_SCHEMA_VERSION,
                    sakes = emptyList(),
                    reviews = listOf(sampleSerializableReview()),
                )

            val result = repository.importJson(json.encodeToString(payload))

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is InvalidBackupReferenceException)
            assertTrue(database.reviewDao().getAllOnce().isEmpty())
        }

    @Test
    fun importJson_existingLocalIdsDoNotOverwriteUnrelatedRows() =
        runTest {
            val repository = createRepository()
            database.sakeDao().insert(sampleSakeEntity())
            database.reviewDao().insert(sampleReviewEntity())

            val payload =
                BackupPayload(
                    schemaVersion = CURRENT_SCHEMA_VERSION,
                    sakes = listOf(sampleSerializableSake().copy(name = "バックアップ酒")),
                    reviews = listOf(sampleSerializableReview().copy(otherCautions = "imported")),
                )

            repository.importJson(json.encodeToString(payload)).getOrThrow()

            val storedSakes = database.sakeDao().getAllOnce()
            val storedReviews = database.reviewDao().getAllOnce()
            assertEquals(2, storedSakes.size)
            assertEquals(2, storedReviews.size)
            assertTrue(storedSakes.any { sake -> sake.id == sampleSakeEntity().id && sake.name == "テスト酒" })
            assertTrue(storedSakes.any { sake -> sake.id != sampleSerializableSake().id && sake.name == "バックアップ酒" })
            assertTrue(
                storedReviews.any { review ->
                    review.id == sampleReviewEntity().id && review.otherCautions == null
                },
            )
            assertTrue(
                storedReviews.any { review ->
                    review.id != sampleSerializableReview().id && review.otherCautions == "imported"
                },
            )
        }

    @Test
    fun importJson_existingLocalSakeIdDoesNotSatisfyBackupReference() =
        runTest {
            val repository = createRepository()
            database.sakeDao().insert(sampleSakeEntity())
            val payload =
                BackupPayload(
                    schemaVersion = CURRENT_SCHEMA_VERSION,
                    sakes = emptyList(),
                    reviews = listOf(sampleSerializableReview()),
                )

            val result = repository.importJson(json.encodeToString(payload))

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is InvalidBackupReferenceException)
        }

    @Test
    fun importJson_blankSakeName_returnsFailure() =
        runTest {
            val repository = createRepository()
            val payload =
                BackupPayload(
                    schemaVersion = CURRENT_SCHEMA_VERSION,
                    sakes = listOf(sampleSerializableSake().copy(name = "   ")),
                    reviews = emptyList(),
                )

            val result = repository.importJson(json.encodeToString(payload))

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is IllegalArgumentException)
            assertTrue(database.sakeDao().getAllOnce().isEmpty())
        }

    @Test
    fun importJson_outOfRangeViscosity_returnsFailure() =
        runTest {
            val repository = createRepository()
            val payload =
                BackupPayload(
                    schemaVersion = CURRENT_SCHEMA_VERSION,
                    sakes = listOf(sampleSerializableSake()),
                    reviews = listOf(sampleSerializableReview().copy(appearanceViscosity = 6)),
                )

            val result = repository.importJson(json.encodeToString(payload))

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is IllegalArgumentException)
            assertTrue(database.reviewDao().getAllOnce().isEmpty())
        }

    @Test
    fun exportJson_cancellationPropagates() =
        runTest {
            val repository = createRepository(ioDispatcher = CancellationDispatcher())

            try {
                repository.exportJson()
                org.junit.Assert.fail("Expected exportJson to throw CancellationException")
            } catch (_: CancellationException) {
                // Expected.
            }
        }

    @Test
    fun importJson_cancellationPropagates() =
        runTest {
            val repository = createRepository(ioDispatcher = CancellationDispatcher())

            try {
                repository.importJson("""{"schemaVersion":1}""")
                org.junit.Assert.fail("Expected importJson to throw CancellationException")
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
            ioDispatcher = ioDispatcher,
        )

    private fun sampleSerializableSake(): SerializableSake =
        SerializableSake(
            id = 101L,
            name = "テスト酒",
            grade = SakeGrade.JUNMAI.name,
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
            appearanceSoundness = ReviewSoundness.SOUND.name,
            appearanceColor = SakeColor.CLEAR.name,
            aromaSoundness = ReviewSoundness.SOUND.name,
            aromaIntensity = IntensityLevel.MEDIUM.name,
            aromaExamples = listOf(Aroma.MELON.name),
            tasteSoundness = ReviewSoundness.SOUND.name,
            tasteSweetness = TasteLevel.STRONG.name,
            otherOverallReview = OverallReview.GOOD.name,
        )

    private fun sampleSakeEntity(): SakeEntity =
        sampleSerializableSake().let { sake ->
            SakeEntity(
                id = sake.id,
                name = sake.name,
                grade = SakeGrade.valueOf(sake.grade),
                imageUri = "file:///images/sakes/1.jpg",
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
            scene = null,
            dish = null,
            appearanceSoundness = ReviewSoundness.SOUND,
            appearanceColor = SakeColor.CLEAR,
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
            tasteMainNote = null,
            tasteSweetness = TasteLevel.STRONG,
            tasteSourness = null,
            tasteBitterness = null,
            tasteUmami = null,
            tasteInPalateAroma = emptyList(),
            tasteAftertaste = null,
            tasteComplexity = null,
            otherIndividuality = null,
            otherCautions = null,
            otherOverallReview = OverallReview.GOOD,
        )
}

private class CancellationDispatcher : CoroutineDispatcher() {
    override fun dispatch(
        context: kotlin.coroutines.CoroutineContext,
        block: Runnable,
    ) = throw CancellationException("cancelled")
}
