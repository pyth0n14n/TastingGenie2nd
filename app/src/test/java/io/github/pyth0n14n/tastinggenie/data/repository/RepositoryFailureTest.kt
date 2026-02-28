package io.github.pyth0n14n.tastinggenie.data.repository

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
import org.junit.Assert.fail
import org.junit.Test

class RepositoryFailureTest {
    @Test
    fun sakeRepository_upsert_propagatesDaoFailure() =
        runTest {
            // Repository が例外を握りつぶさず呼び出し元へ伝播することを確認する。
            val repository = SakeRepositoryImpl(FailingSakeDao(), StandardTestDispatcher(testScheduler))

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
}

private class FailingReviewDao : ReviewDao {
    override fun observeBySakeId(sakeId: Long): Flow<List<ReviewEntity>> =
        flow { throw DaoFailureException("DAO failure") }

    override suspend fun getById(id: Long): ReviewEntity? = throw DaoFailureException("DAO failure")

    override suspend fun getAllOnce(): List<ReviewEntity> = throw DaoFailureException("DAO failure")

    override suspend fun insert(entity: ReviewEntity): Long = throw DaoFailureException("DAO failure")

    override suspend fun insertAll(entities: List<ReviewEntity>): Unit = throw DaoFailureException("DAO failure")

    override suspend fun update(entity: ReviewEntity): Int = throw DaoFailureException("DAO failure")
}

private class DaoFailureException(
    message: String,
) : IllegalStateException(message)
