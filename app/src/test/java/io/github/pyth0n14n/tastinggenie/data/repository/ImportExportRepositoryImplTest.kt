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
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeColor
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeGrade
import io.github.pyth0n14n.tastinggenie.domain.model.enums.TasteLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Temperature
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
            assertEquals("content://review/image/1", payload.reviews.single().imageUri)
        }

    @Test
    fun importJson_roundTripUpsertsPayload() =
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
            assertEquals(LocalDate.parse("2026-03-17").toEpochDay(), storedReview.dateEpochDay)
            assertEquals("content://review/image/1", storedReview.imageUri)
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
            id = 1L,
            name = "テスト酒",
            grade = SakeGrade.JUNMAI.name,
            type = emptyList(),
            maker = "酒蔵A",
        )

    private fun sampleSerializableReview(): SerializableReview =
        SerializableReview(
            id = 2L,
            sakeId = 1L,
            date = "2026-03-17",
            temperature = Temperature.JOON.name,
            color = SakeColor.CLEAR.name,
            intensity = IntensityLevel.MEDIUM.name,
            scentTop = listOf(Aroma.MELON.name),
            sweet = TasteLevel.STRONG.name,
            review = OverallReview.GOOD.name,
            imageUri = "content://review/image/1",
        )

    private fun sampleSakeEntity(): SakeEntity =
        sampleSerializableSake().let { sake ->
            SakeEntity(
                id = sake.id,
                name = sake.name,
                grade = SakeGrade.valueOf(sake.grade),
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
            id = 2L,
            sakeId = 1L,
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
            imageUri = "content://review/image/1",
        )
}
