package io.github.pyth0n14n.tastinggenie.feature.review.detail

import androidx.lifecycle.SavedStateHandle
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.Review
import io.github.pyth0n14n.tastinggenie.domain.model.ReviewInput
import io.github.pyth0n14n.tastinggenie.feature.review.RecordingReviewRepository
import io.github.pyth0n14n.tastinggenie.feature.review.RecordingSakeRepository
import io.github.pyth0n14n.tastinggenie.feature.review.ReviewFakeMasterDataRepository
import io.github.pyth0n14n.tastinggenie.feature.review.TEST_REVIEW_ID
import io.github.pyth0n14n.tastinggenie.feature.review.testReview
import io.github.pyth0n14n.tastinggenie.feature.review.testSake
import io.github.pyth0n14n.tastinggenie.navigation.AppDestination
import io.github.pyth0n14n.tastinggenie.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReviewDetailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun loadInitial_populatesDetailState() =
        runTest {
            val viewModel =
                ReviewDetailViewModel(
                    savedStateHandle = SavedStateHandle(mapOf(AppDestination.ARG_REVIEW_ID to TEST_REVIEW_ID)),
                    reviewRepository = RecordingReviewRepository(initial = listOf(testReview())),
                    sakeRepository = RecordingSakeRepository(initial = listOf(testSake())),
                    masterDataRepository = ReviewFakeMasterDataRepository(),
                )
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals("テスト銘柄", state.sakeName)
            assertEquals(TEST_REVIEW_ID, state.review?.id)
            assertEquals("常温", state.temperatureLabels["JOON"])
        }

    @Test
    fun loadInitial_missingReview_setsError() =
        runTest {
            val viewModel =
                ReviewDetailViewModel(
                    savedStateHandle = SavedStateHandle(mapOf(AppDestination.ARG_REVIEW_ID to TEST_REVIEW_ID)),
                    reviewRepository = RecordingReviewRepository(),
                    sakeRepository = RecordingSakeRepository(initial = listOf(testSake())),
                    masterDataRepository = ReviewFakeMasterDataRepository(),
                )
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertNotNull(state.error)
            assertEquals(R.string.error_load_review, state.error?.messageResId)
        }

    @Test
    fun refresh_reloadsUpdatedReviewAfterEditSave() =
        runTest {
            val reviewRepository =
                RecordingReviewRepository(
                    initial = listOf(testReview().copy(comment = "更新前")),
                )
            val viewModel =
                ReviewDetailViewModel(
                    savedStateHandle = SavedStateHandle(mapOf(AppDestination.ARG_REVIEW_ID to TEST_REVIEW_ID)),
                    reviewRepository = reviewRepository,
                    sakeRepository = RecordingSakeRepository(initial = listOf(testSake())),
                    masterDataRepository = ReviewFakeMasterDataRepository(),
                )
            advanceUntilIdle()

            reviewRepository.upsertReview(testReview().copy(comment = "更新後").toInput())
            viewModel.refresh()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals("更新後", state.review?.comment)
        }
}

private fun Review.toInput(): ReviewInput =
    ReviewInput(
        id = id,
        sakeId = sakeId,
        date = date,
        bar = bar,
        price = price,
        volume = volume,
        temperature = temperature,
        color = color,
        viscosity = viscosity,
        intensity = intensity,
        scentTop = scentTop,
        scentBase = scentBase,
        scentMouth = scentMouth,
        sweet = sweet,
        sour = sour,
        bitter = bitter,
        umami = umami,
        sharp = sharp,
        scene = scene,
        dish = dish,
        comment = comment,
        review = review,
    )
