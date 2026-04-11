package io.github.pyth0n14n.tastinggenie.data.repository

import io.github.pyth0n14n.tastinggenie.domain.model.ReviewInput
import io.github.pyth0n14n.tastinggenie.domain.model.enums.OverallReview
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

private const val DELETE_TEST_SAKE_ID = 3L
private const val MISSING_REVIEW_ID = 999L

@OptIn(ExperimentalCoroutinesApi::class)
class ReviewRepositoryImplTest {
    @Test
    fun upsertReview_insertsAndObserves() =
        runTest {
            val dao = FakeReviewDao()
            val repository = ReviewRepositoryImpl(dao, StandardTestDispatcher(testScheduler))
            val reviewId =
                repository.upsertReview(
                    ReviewInput(
                        sakeId = 1L,
                        date = LocalDate.parse("2026-02-22"),
                        otherCautions = "香りが良い",
                        otherOverallReview = OverallReview.GOOD,
                    ),
                )

            val loaded = repository.getReview(reviewId)
            val observed = repository.observeReviews(1L).first { it.isNotEmpty() }

            assertNotNull(loaded)
            assertEquals("香りが良い", loaded?.otherCautions)
            assertEquals(1, observed.size)
        }

    @Test
    fun upsertReview_updatesExistingEntity() =
        runTest {
            val dao = FakeReviewDao()
            val repository = ReviewRepositoryImpl(dao, StandardTestDispatcher(testScheduler))
            val reviewId =
                repository.upsertReview(
                    ReviewInput(
                        sakeId = 2L,
                        date = LocalDate.parse("2026-02-20"),
                        otherCautions = "更新前",
                    ),
                )
            repository.upsertReview(
                ReviewInput(
                    id = reviewId,
                    sakeId = 2L,
                    date = LocalDate.parse("2026-02-21"),
                    otherCautions = "更新後",
                    otherOverallReview = OverallReview.VERY_GOOD,
                ),
            )

            val loaded = repository.getReview(reviewId)

            assertEquals("更新後", loaded?.otherCautions)
            assertEquals(LocalDate.parse("2026-02-21"), loaded?.date)
            assertEquals(OverallReview.VERY_GOOD, loaded?.otherOverallReview)
        }

    @Test
    fun deleteReview_removesEntityFromRepository() =
        runTest {
            val dao = FakeReviewDao()
            val repository = ReviewRepositoryImpl(dao, StandardTestDispatcher(testScheduler))
            val reviewId =
                repository.upsertReview(
                    ReviewInput(
                        sakeId = DELETE_TEST_SAKE_ID,
                        date = LocalDate.parse("2026-02-23"),
                        otherCautions = "削除対象",
                    ),
                )

            val deleted = repository.deleteReview(reviewId)
            val observed = repository.observeReviews(DELETE_TEST_SAKE_ID).first()

            assertEquals(true, deleted)
            assertEquals(null, repository.getReview(reviewId))
            assertTrue(observed.isEmpty())
        }

    @Test
    fun deleteReview_returnsFalseWhenEntityDoesNotExist() =
        runTest {
            val dao = FakeReviewDao()
            val repository = ReviewRepositoryImpl(dao, StandardTestDispatcher(testScheduler))

            val deleted = repository.deleteReview(MISSING_REVIEW_ID)

            assertFalse(deleted)
        }
}
