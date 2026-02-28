package io.github.pyth0n14n.tastinggenie.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.pyth0n14n.tastinggenie.data.local.AppDatabase
import io.github.pyth0n14n.tastinggenie.data.local.entity.ReviewEntity
import io.github.pyth0n14n.tastinggenie.data.local.entity.SakeEntity
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

    private fun createSake(name: String): SakeEntity =
        SakeEntity(
            name = name,
            grade = SakeGrade.JUNMAI,
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
            imageUri = null,
        )
}
