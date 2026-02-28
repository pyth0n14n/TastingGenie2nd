package io.github.pyth0n14n.tastinggenie.data.repository

import io.github.pyth0n14n.tastinggenie.domain.model.ReviewInput
import io.github.pyth0n14n.tastinggenie.domain.model.enums.OverallReview
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReviewRepositoryImplTest {
    @Test
    fun upsertReview_insertsAndObserves() = runTest {
        val dao = FakeReviewDao()
        val repository = ReviewRepositoryImpl(dao, StandardTestDispatcher(testScheduler))
        val reviewId = repository.upsertReview(
            ReviewInput(
                sakeId = 1L,
                date = LocalDate.parse("2026-02-22"),
                comment = "香りが良い",
                review = OverallReview.GOOD,
            ),
        )

        val loaded = repository.getReview(reviewId)
        val observed = repository.observeReviews(1L).first { it.isNotEmpty() }

        assertNotNull(loaded)
        assertEquals("香りが良い", loaded?.comment)
        assertEquals(1, observed.size)
    }

    @Test
    fun upsertReview_updatesExistingEntity() = runTest {
        val dao = FakeReviewDao()
        val repository = ReviewRepositoryImpl(dao, StandardTestDispatcher(testScheduler))
        val reviewId = repository.upsertReview(
            ReviewInput(
                sakeId = 2L,
                date = LocalDate.parse("2026-02-20"),
                comment = "更新前",
            ),
        )
        repository.upsertReview(
            ReviewInput(
                id = reviewId,
                sakeId = 2L,
                date = LocalDate.parse("2026-02-21"),
                comment = "更新後",
                review = OverallReview.VERY_GOOD,
            ),
        )

        val loaded = repository.getReview(reviewId)

        assertEquals("更新後", loaded?.comment)
        assertEquals(LocalDate.parse("2026-02-21"), loaded?.date)
        assertEquals(OverallReview.VERY_GOOD, loaded?.review)
    }
}
