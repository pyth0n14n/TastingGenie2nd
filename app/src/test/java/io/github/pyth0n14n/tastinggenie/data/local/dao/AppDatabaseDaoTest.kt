package io.github.pyth0n14n.tastinggenie.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.pyth0n14n.tastinggenie.data.local.AppDatabase
import io.github.pyth0n14n.tastinggenie.data.local.entity.ReviewEntity
import io.github.pyth0n14n.tastinggenie.data.local.entity.SakeEntity
import io.github.pyth0n14n.tastinggenie.domain.model.enums.ReviewSoundness
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeGrade
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppDatabaseDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var sakeDao: SakeDao
    private lateinit var reviewDao: ReviewDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        sakeDao = database.sakeDao()
        reviewDao = database.reviewDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun observeAll_ordersByNameAsc() =
        runTest {
            sakeDao.insert(createSake(name = "B酒"))
            sakeDao.insert(createSake(name = "A酒"))
            sakeDao.insert(createSake(name = "C酒"))

            val observed = sakeDao.observeAll().first()

            assertEquals(listOf("A酒", "B酒", "C酒"), observed.map { it.name })
        }

    @Test
    fun observeBySakeId_ordersByDateDescThenIdDesc() =
        runTest {
            val sakeId = sakeDao.insert(createSake(name = "並び順確認酒"))
            val newerDate = LocalDate.parse("2026-02-28").toEpochDay()
            val olderDate = LocalDate.parse("2026-02-20").toEpochDay()
            // 同日データは id DESC で並ぶことを検証するため、挿入順を固定する。
            val olderId = reviewDao.insert(createReview(sakeId = sakeId, dateEpochDay = newerDate, comment = "同日_先"))
            val newerId = reviewDao.insert(createReview(sakeId = sakeId, dateEpochDay = newerDate, comment = "同日_後"))
            val oldestId = reviewDao.insert(createReview(sakeId = sakeId, dateEpochDay = olderDate, comment = "古い日"))

            val observed = reviewDao.observeBySakeId(sakeId).first()

            assertEquals(listOf(newerId, olderId, oldestId), observed.map { it.id })
        }

    @Test
    fun insertReview_withUnknownSakeId_throwsForeignKeyError() =
        runTest {
            // 親テーブルに存在しない sakeId を使い、FK 制約が有効かを確認する。
            val review =
                createReview(
                    sakeId = 999999L,
                    dateEpochDay = LocalDate.parse("2026-02-28").toEpochDay(),
                    comment = "不整合",
                )

            try {
                reviewDao.insert(review)
                fail("Expected foreign key constraint exception.")
            } catch (expected: Exception) {
                val message = expected.message.orEmpty()
                val upper = message.uppercase()
                if (!upper.contains("FOREIGN") && !upper.contains("CONSTRAINT")) {
                    fail("Unexpected exception: ${expected::class.simpleName} $message")
                }
            }
        }

    @Test
    fun deleteReview_removesRowById() =
        runTest {
            val sakeId = sakeDao.insert(createSake(name = "削除確認酒"))
            val firstId =
                reviewDao.insert(
                    createReview(
                        sakeId = sakeId,
                        dateEpochDay = LocalDate.parse("2026-02-10").toEpochDay(),
                        comment = "削除される",
                    ),
                )
            val secondId =
                reviewDao.insert(
                    createReview(
                        sakeId = sakeId,
                        dateEpochDay = LocalDate.parse("2026-02-11").toEpochDay(),
                        comment = "残る",
                    ),
                )

            val deleted = reviewDao.deleteById(firstId)
            val remaining = reviewDao.observeBySakeId(sakeId).first()

            assertEquals(1, deleted)
            assertEquals(listOf(secondId), remaining.map { it.id })
        }

    @Test
    fun deleteBySakeId_removesAllChildReviews() =
        runTest {
            val firstSakeId = sakeDao.insert(createSake(name = "親1"))
            val secondSakeId = sakeDao.insert(createSake(name = "親2"))
            reviewDao.insert(
                createReview(
                    sakeId = firstSakeId,
                    dateEpochDay = LocalDate.parse("2026-02-10").toEpochDay(),
                    comment = "削除1",
                ),
            )
            reviewDao.insert(
                createReview(
                    sakeId = firstSakeId,
                    dateEpochDay = LocalDate.parse("2026-02-11").toEpochDay(),
                    comment = "削除2",
                ),
            )
            val survivingId =
                reviewDao.insert(
                    createReview(
                        sakeId = secondSakeId,
                        dateEpochDay = LocalDate.parse("2026-02-12").toEpochDay(),
                        comment = "残る",
                    ),
                )

            val deleted = reviewDao.deleteBySakeId(firstSakeId)
            val remaining = reviewDao.getAllOnce()

            assertEquals(2, deleted)
            assertEquals(listOf(survivingId), remaining.map { it.id })
        }

    @Test
    fun deleteSake_removesOnlyTargetRow() =
        runTest {
            val firstId = sakeDao.insert(createSake(name = "削除対象"))
            val secondId = sakeDao.insert(createSake(name = "残る"))

            val deleted = sakeDao.deleteById(firstId)
            val remaining = sakeDao.getAllOnce()

            assertEquals(1, deleted)
            assertEquals(listOf(secondId), remaining.map { it.id })
        }

    private fun createSake(name: String): SakeEntity =
        SakeEntity(
            name = name,
            grade = SakeGrade.JUNMAI,
            imageUris = emptyList(),
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
        dateEpochDay: Long,
        comment: String,
    ): ReviewEntity =
        ReviewEntity(
            sakeId = sakeId,
            dateEpochDay = dateEpochDay,
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
            otherOverallReview = null,
        )
}
