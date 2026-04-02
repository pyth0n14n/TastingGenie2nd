package io.github.pyth0n14n.tastinggenie.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.pyth0n14n.tastinggenie.data.local.AppDatabase
import io.github.pyth0n14n.tastinggenie.data.local.entity.ReviewEntity
import io.github.pyth0n14n.tastinggenie.data.local.entity.SakeEntity
import io.github.pyth0n14n.tastinggenie.domain.model.SakeInput
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeGrade
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    private lateinit var repository: SakeRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        imageRepository = RecordingImageRepository()
        repository =
            SakeRepositoryImpl(
                database = database,
                sakeDao = database.sakeDao(),
                reviewDao = database.reviewDao(),
                sakeImageRepository = imageRepository,
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
    fun deleteSake_removesReviewsAndDeletesImage() =
        runTest {
            val sakeId =
                database.sakeDao().insert(
                    createSake(
                        name = "削除対象",
                        imageUri = "file:///images/sakes/target.jpg",
                    ),
                )
            database.reviewDao().insert(createReview(sakeId = sakeId, comment = "1件目"))
            database.reviewDao().insert(createReview(sakeId = sakeId, comment = "2件目"))

            val result = repository.deleteSake(sakeId)

            assertEquals(true, result.isDeleted)
            assertEquals(emptyList<Long>(), database.sakeDao().getAllOnce().map { it.id })
            assertEquals(emptyList<Long>(), database.reviewDao().getAllOnce().map { it.id })
            assertEquals(listOf("file:///images/sakes/target.jpg"), imageRepository.deletedUris)
        }

    @Test
    fun deleteSake_surfacesImageCleanupFailureWithoutRollingBackDbDelete() =
        runTest {
            val imageUri = "file:///images/sakes/fail.jpg"
            val sakeId =
                database.sakeDao().insert(
                    createSake(
                        name = "削除対象",
                        imageUri = imageUri,
                    ),
                )
            imageRepository.failOnDelete = imageUri

            val result = repository.deleteSake(sakeId)

            assertEquals(true, result.isDeleted)
            assertEquals("cleanup failed", result.imageCleanupErrorCauseKey)
            assertEquals(emptyList<Long>(), database.sakeDao().getAllOnce().map { it.id })
        }

    private fun createSake(
        name: String,
        imageUri: String? = null,
    ): SakeEntity =
        SakeEntity(
            name = name,
            grade = SakeGrade.JUNMAI,
            imageUri = imageUri,
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
    ): ReviewEntity =
        ReviewEntity(
            sakeId = sakeId,
            dateEpochDay = LocalDate.parse("2026-03-14").toEpochDay(),
            bar = null,
            price = null,
            volume = null,
            temperature = null,
            color = null,
            viscosity = null,
            intensity = null,
            scentTop = emptyList(),
            scentBase = emptyList(),
            scentMouth = emptyList(),
            sweet = null,
            sour = null,
            bitter = null,
            umami = null,
            sharp = null,
            scene = null,
            dish = null,
            comment = comment,
            review = null,
        )
}

private class RecordingImageRepository : io.github.pyth0n14n.tastinggenie.domain.repository.SakeImageRepository {
    val deletedUris = mutableListOf<String>()
    var failOnDelete: String? = null

    override suspend fun importImage(
        sourceUri: String,
        previousImageUri: String?,
    ): String = sourceUri

    override suspend fun deleteImage(imageUri: String?) {
        imageUri?.let { uri ->
            deletedUris += uri
            if (uri == failOnDelete) {
                error("cleanup failed")
            }
        }
    }
}
