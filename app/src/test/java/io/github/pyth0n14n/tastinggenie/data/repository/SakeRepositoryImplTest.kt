package io.github.pyth0n14n.tastinggenie.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.pyth0n14n.tastinggenie.data.local.AppDatabase
import io.github.pyth0n14n.tastinggenie.data.local.entity.ReviewEntity
import io.github.pyth0n14n.tastinggenie.data.local.entity.SakeEntity
import io.github.pyth0n14n.tastinggenie.domain.model.AppSettings
import io.github.pyth0n14n.tastinggenie.domain.model.SakeInput
import io.github.pyth0n14n.tastinggenie.domain.model.enums.OverallReview
import io.github.pyth0n14n.tastinggenie.domain.model.enums.ReviewSoundness
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeGrade
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SakeRepositoryImplTest {
    private lateinit var database: AppDatabase
    private lateinit var imageRepository: RecordingImageRepository
    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var repository: SakeRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        imageRepository = RecordingImageRepository()
        settingsRepository = FakeSettingsRepository()
        repository =
            SakeRepositoryImpl(
                database = database,
                sakeDao = database.sakeDao(),
                reviewDao = database.reviewDao(),
                sakeImageRepository = imageRepository,
                settingsRepository = settingsRepository,
                ioDispatcher = UnconfinedTestDispatcher(),
            )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun upsertSake_insertsAndObserves() =
        runTest {
            val createdId =
                repository.upsertSake(
                    SakeInput(
                        name = "テスト酒",
                        grade = SakeGrade.JUNMAI,
                    ),
                )

            val loaded = repository.getSake(createdId)
            val observed = repository.observeSakes().first { it.isNotEmpty() }

            assertNotNull(loaded)
            assertEquals("テスト酒", loaded?.name)
            assertEquals(1, observed.size)
        }

    @Test
    fun upsertSake_updatesExistingEntity() =
        runTest {
            val createdId =
                repository.upsertSake(
                    SakeInput(
                        name = "更新前",
                        grade = SakeGrade.JUNMAI,
                    ),
                )
            repository.upsertSake(
                SakeInput(
                    id = createdId,
                    name = "更新後",
                    grade = SakeGrade.GINJO,
                ),
            )

            val loaded = repository.getSake(createdId)

            assertEquals("更新後", loaded?.name)
            assertEquals(SakeGrade.GINJO, loaded?.grade)
        }

    @Test
    fun observeSakeListSummaries_sortsPinnedFirstAndLoadsLatestOverallReview() =
        runTest {
            val pinnedId =
                database.sakeDao().insert(
                    createSake(
                        name = "後ろ",
                        isPinned = true,
                    ),
                )
            val normalId =
                database.sakeDao().insert(
                    createSake(
                        name = "前",
                        isPinned = false,
                    ),
                )
            database.reviewDao().insert(
                createReview(
                    sakeId = normalId,
                    comment = "old",
                    overallReview = OverallReview.BAD,
                    date = LocalDate.parse("2026-03-13"),
                ),
            )
            database.reviewDao().insert(
                createReview(
                    sakeId = normalId,
                    comment = "new",
                    overallReview = OverallReview.GOOD,
                    date = LocalDate.parse("2026-03-14"),
                ),
            )

            val summaries = repository.observeSakeListSummaries().first { it.size == 2 }

            assertEquals(pinnedId, summaries.first().sake.id)
            assertEquals(normalId, summaries.last().sake.id)
            assertEquals(OverallReview.GOOD, summaries.last().latestOverallReview)
        }

    @Test
    fun setPinned_updatesStoredFlag() =
        runTest {
            val createdId =
                repository.upsertSake(
                    SakeInput(
                        name = "固定対象",
                        grade = SakeGrade.JUNMAI,
                    ),
                )

            repository.setPinned(id = createdId, isPinned = true)

            val loaded = repository.getSake(createdId)
            assertEquals(true, loaded?.isPinned)
        }

    @Test
    fun deleteSake_removesReviewsAndLeavesImageCleanupToManualByDefault() =
        runTest {
            val sakeId =
                database.sakeDao().insert(
                    createSake(
                        name = "削除対象",
                        imageUris = listOf("file:///images/sakes/target.jpg"),
                    ),
                )
            database.reviewDao().insert(createReview(sakeId = sakeId, comment = "1件目"))
            database.reviewDao().insert(createReview(sakeId = sakeId, comment = "2件目"))

            val result = repository.deleteSake(sakeId)

            assertEquals(true, result.isDeleted)
            assertEquals(emptyList<Long>(), database.sakeDao().getAllOnce().map { it.id })
            assertEquals(emptyList<Long>(), database.reviewDao().getAllOnce().map { it.id })
            assertEquals(0, imageRepository.cleanupCalls)
        }

    @Test
    fun deleteSake_runsUnusedCleanupWhenAutoDeleteEnabled() =
        runTest {
            settingsRepository.updateAutoDeleteUnusedImages(enabled = true)
            val sakeId =
                database.sakeDao().insert(
                    createSake(
                        name = "削除対象",
                        imageUris = listOf("file:///images/sakes/target.jpg"),
                    ),
                )

            val result = repository.deleteSake(sakeId)

            assertEquals(true, result.isDeleted)
            assertEquals(1, imageRepository.cleanupCalls)
            assertEquals(false, result.hasImageCleanupError)
        }

    @Test
    fun deleteSake_surfacesImageCleanupFailureWithoutRollingBackDbDelete() =
        runTest {
            settingsRepository.updateAutoDeleteUnusedImages(enabled = true)
            val imageUri = "file:///images/sakes/fail.jpg"
            val sakeId =
                database.sakeDao().insert(
                    createSake(
                        name = "削除対象",
                        imageUris = listOf(imageUri),
                    ),
                )
            imageRepository.cleanupFailure = IllegalStateException("cleanup failed")

            val result = repository.deleteSake(sakeId)

            assertEquals(true, result.isDeleted)
            assertEquals(true, result.hasImageCleanupError)
            assertEquals("cleanup failed", result.imageCleanupErrorCauseKey)
            assertEquals(emptyList<Long>(), database.sakeDao().getAllOnce().map { it.id })
        }

    @Test
    fun deleteSake_marksCleanupFailureEvenWhenExceptionMessageIsNull() =
        runTest {
            settingsRepository.updateAutoDeleteUnusedImages(enabled = true)
            val imageUri = "file:///images/sakes/fail-null-message.jpg"
            val sakeId =
                database.sakeDao().insert(
                    createSake(
                        name = "削除対象",
                        imageUris = listOf(imageUri),
                    ),
                )
            imageRepository.cleanupFailure = IllegalStateException()

            val result = repository.deleteSake(sakeId)

            assertEquals(true, result.isDeleted)
            assertEquals(true, result.hasImageCleanupError)
            assertEquals(null, result.imageCleanupErrorCauseKey)
            assertEquals(emptyList<Long>(), database.sakeDao().getAllOnce().map { it.id })
        }

    private fun createSake(
        name: String,
        imageUris: List<String> = emptyList(),
        isPinned: Boolean = false,
    ): SakeEntity =
        SakeEntity(
            name = name,
            grade = SakeGrade.JUNMAI,
            isPinned = isPinned,
            imageUris = imageUris,
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

    private fun createReview(
        sakeId: Long,
        comment: String,
        overallReview: OverallReview? = null,
        date: LocalDate = LocalDate.parse("2026-03-14"),
    ): ReviewEntity =
        ReviewEntity(
            sakeId = sakeId,
            dateEpochDay = date.toEpochDay(),
            bar = null,
            price = null,
            volume = null,
            temperature = null,
            scene = null,
            dish = null,
            appearanceSoundness = ReviewSoundness.SOUND,
            appearanceColor = null,
            appearanceViscosity = null,
            aromaSoundness = ReviewSoundness.SOUND,
            aromaIntensity = null,
            aromaExamples = emptyList(),
            aromaMainNote = null,
            aromaComplexity = null,
            tasteSoundness = ReviewSoundness.SOUND,
            tasteAttack = null,
            tasteTextureRoundness = null,
            tasteTextureSmoothness = null,
            tasteMainNote = null,
            tasteSweetness = null,
            tasteSourness = null,
            tasteBitterness = null,
            tasteUmami = null,
            tasteInPalateAroma = emptyList(),
            tasteAftertaste = null,
            tasteComplexity = null,
            otherIndividuality = null,
            otherCautions = comment,
            otherOverallReview = overallReview,
        )
}

private class RecordingImageRepository : io.github.pyth0n14n.tastinggenie.domain.repository.SakeImageRepository {
    val deletedUris = mutableListOf<String>()
    var deleteFailure: Throwable? = null
    var cleanupFailure: Throwable? = null
    var cleanupCalls: Int = 0

    override suspend fun importImage(sourceUri: String): String = sourceUri

    override suspend fun deleteImage(imageUri: String?) {
        imageUri?.let { uri ->
            deletedUris += uri
            deleteFailure?.let { throw it }
        }
    }

    override suspend fun cleanupUnusedImages(): Int {
        cleanupCalls += 1
        cleanupFailure?.let { throw it }
        return cleanupCalls
    }
}

private class FakeSettingsRepository : io.github.pyth0n14n.tastinggenie.domain.repository.SettingsRepository {
    private val stream = MutableStateFlow(AppSettings())

    override fun observeSettings(): Flow<AppSettings> = stream

    override suspend fun getCurrentSettings(): AppSettings = stream.value

    override suspend fun updateShowHelpHints(enabled: Boolean) = Unit

    override suspend fun updateShowImagePreview(enabled: Boolean) = Unit

    override suspend fun updateShowReviewSoundness(enabled: Boolean) = Unit

    override suspend fun updateAutoDeleteUnusedImages(enabled: Boolean) {
        stream.value = stream.value.copy(autoDeleteUnusedImages = enabled)
    }
}
