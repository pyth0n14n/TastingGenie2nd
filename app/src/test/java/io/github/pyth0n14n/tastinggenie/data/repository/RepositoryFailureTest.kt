package io.github.pyth0n14n.tastinggenie.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import io.github.pyth0n14n.tastinggenie.data.local.AppDatabase
import io.github.pyth0n14n.tastinggenie.data.local.dao.ReviewDao
import io.github.pyth0n14n.tastinggenie.data.local.dao.SakeDao
import io.github.pyth0n14n.tastinggenie.data.local.entity.ReviewEntity
import io.github.pyth0n14n.tastinggenie.data.local.entity.SakeEntity
import io.github.pyth0n14n.tastinggenie.domain.model.SakeInput
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeGrade
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RepositoryFailureTest {
    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun sakeRepository_upsert_propagatesDaoFailure() =
        runTest {
            // Repository が例外を握りつぶさず呼び出し元へ伝播することを確認する。
            val repository =
                SakeRepositoryImpl(
                    database = database,
                    sakeDao = FailingSakeDao(),
                    reviewDao = database.reviewDao(),
                    sakeImageRepository = NoOpSakeImageRepository(),
                    ioDispatcher = StandardTestDispatcher(testScheduler),
                )

            try {
                repository.upsertSake(
                    SakeInput(
                        name = "失敗確認",
                        grade = SakeGrade.JUNMAI,
                    ),
                )
                fail("Expected DaoFailureException.")
            } catch (expected: DaoFailureException) {
                // expected
            }
        }

    @Test
    fun reviewRepository_observe_propagatesDaoFailure() =
        runTest {
            val repository = ReviewRepositoryImpl(FailingReviewDao(), StandardTestDispatcher(testScheduler))

            try {
                repository.observeReviews(1L).collect { }
                fail("Expected DaoFailureException.")
            } catch (expected: DaoFailureException) {
                // expected
            }
        }

    @Test
    fun reviewRepository_get_propagatesDaoFailure() =
        runTest {
            val repository = ReviewRepositoryImpl(FailingReviewDao(), StandardTestDispatcher(testScheduler))

            try {
                repository.getReview(1L)
                fail("Expected DaoFailureException.")
            } catch (expected: DaoFailureException) {
                // expected
            }
        }
}

private class FailingSakeDao : SakeDao {
    override fun observeAll(): Flow<List<SakeEntity>> = flow { throw DaoFailureException("DAO failure") }

    override suspend fun getById(id: Long): SakeEntity? = throw DaoFailureException("DAO failure")

    override suspend fun getAllOnce(): List<SakeEntity> = throw DaoFailureException("DAO failure")

    override suspend fun insert(entity: SakeEntity): Long = throw DaoFailureException("DAO failure")

    override suspend fun insertAll(entities: List<SakeEntity>): Unit = throw DaoFailureException("DAO failure")

    override suspend fun update(entity: SakeEntity): Int = throw DaoFailureException("DAO failure")

    override suspend fun deleteById(id: Long): Int = throw DaoFailureException("DAO failure")
}

private class FailingReviewDao : ReviewDao {
    override fun observeBySakeId(sakeId: Long): Flow<List<ReviewEntity>> =
        flow { throw DaoFailureException("DAO failure") }

    override suspend fun getById(id: Long): ReviewEntity? = throw DaoFailureException("DAO failure")

    override suspend fun getAllOnce(): List<ReviewEntity> = throw DaoFailureException("DAO failure")

    override suspend fun insert(entity: ReviewEntity): Long = throw DaoFailureException("DAO failure")

    override suspend fun insertAll(entities: List<ReviewEntity>): Unit = throw DaoFailureException("DAO failure")

    override suspend fun update(entity: ReviewEntity): Int = throw DaoFailureException("DAO failure")

    override suspend fun deleteById(id: Long): Int = throw DaoFailureException("DAO failure")

    override suspend fun deleteBySakeId(sakeId: Long): Int = throw DaoFailureException("DAO failure")
}

private class DaoFailureException(
    message: String,
) : IllegalStateException(message)

private class NoOpSakeImageRepository : io.github.pyth0n14n.tastinggenie.domain.repository.SakeImageRepository {
    override suspend fun importImage(
        sourceUri: String,
        previousImageUri: String?,
    ): String = sourceUri

    override suspend fun deleteImage(imageUri: String?) = Unit
}
