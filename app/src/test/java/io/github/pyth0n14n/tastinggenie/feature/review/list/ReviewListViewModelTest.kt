package io.github.pyth0n14n.tastinggenie.feature.review.list

import androidx.lifecycle.SavedStateHandle
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.feature.review.RecordingReviewRepository
import io.github.pyth0n14n.tastinggenie.feature.review.RecordingSakeRepository
import io.github.pyth0n14n.tastinggenie.feature.review.ReviewFakeMasterDataRepository
import io.github.pyth0n14n.tastinggenie.feature.review.TEST_REVIEW_ID
import io.github.pyth0n14n.tastinggenie.feature.review.TEST_SAKE_ID
import io.github.pyth0n14n.tastinggenie.feature.review.testReview
import io.github.pyth0n14n.tastinggenie.feature.review.testSake
import io.github.pyth0n14n.tastinggenie.navigation.AppDestination
import io.github.pyth0n14n.tastinggenie.testutil.MainDispatcherRule
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReviewListViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun uiState_updatesWhenRepositoryEmitsReviews() =
        runTest {
            val viewModel =
                ReviewListViewModel(
                    savedStateHandle = SavedStateHandle(mapOf(AppDestination.ARG_SAKE_ID to TEST_SAKE_ID)),
                    sakeRepository = RecordingSakeRepository(initial = listOf(testSake())),
                    reviewRepository = RecordingReviewRepository(initial = listOf(testReview())),
                    masterDataRepository = ReviewFakeMasterDataRepository(),
                )
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals("テスト銘柄", state.sakeName)
            assertEquals(false, state.hasSakeImage)
            assertEquals(1, state.reviews.size)
            assertEquals("好き", state.overallReviewLabels["GOOD"])
            assertEquals(null, state.loadError)
        }

    @Test
    fun uiState_marksImageAvailableWhenParentSakeHasImage() =
        runTest {
            val viewModel =
                ReviewListViewModel(
                    savedStateHandle = SavedStateHandle(mapOf(AppDestination.ARG_SAKE_ID to TEST_SAKE_ID)),
                    sakeRepository =
                        RecordingSakeRepository(
                            initial = listOf(testSake(imageUri = "file:///images/sakes/1.jpg")),
                        ),
                    reviewRepository = RecordingReviewRepository(initial = listOf(testReview())),
                    masterDataRepository = ReviewFakeMasterDataRepository(),
                )
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals(true, state.hasSakeImage)
        }

    @Test
    fun uiState_setsErrorWhenSakeIsMissing() =
        runTest {
            val viewModel =
                ReviewListViewModel(
                    savedStateHandle = SavedStateHandle(mapOf(AppDestination.ARG_SAKE_ID to TEST_SAKE_ID)),
                    sakeRepository = RecordingSakeRepository(),
                    reviewRepository = RecordingReviewRepository(),
                    masterDataRepository = ReviewFakeMasterDataRepository(),
                )
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertNotNull(state.loadError)
            assertEquals(R.string.error_load_sake, state.loadError?.messageResId)
        }

    @Test
    fun deleteReview_removesReviewAndClearsDeleteError() =
        runTest {
            val reviewRepository = RecordingReviewRepository(initial = listOf(testReview()))
            val viewModel =
                ReviewListViewModel(
                    savedStateHandle = SavedStateHandle(mapOf(AppDestination.ARG_SAKE_ID to TEST_SAKE_ID)),
                    sakeRepository = RecordingSakeRepository(initial = listOf(testSake())),
                    reviewRepository = reviewRepository,
                    masterDataRepository = ReviewFakeMasterDataRepository(),
                )
            advanceUntilIdle()

            viewModel.deleteReview(TEST_REVIEW_ID)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(listOf(TEST_REVIEW_ID), reviewRepository.deletedReviewIds)
            assertTrue(state.reviews.isEmpty())
            assertEquals(null, state.deleteError)
        }

    @Test
    fun deleteReview_setsDeleteErrorWhenRepositoryFails() =
        runTest {
            val reviewRepository =
                RecordingReviewRepository(initial = listOf(testReview())).apply {
                    deleteFailure = IllegalStateException("delete failed")
                }
            val viewModel =
                ReviewListViewModel(
                    savedStateHandle = SavedStateHandle(mapOf(AppDestination.ARG_SAKE_ID to TEST_SAKE_ID)),
                    sakeRepository = RecordingSakeRepository(initial = listOf(testSake())),
                    reviewRepository = reviewRepository,
                    masterDataRepository = ReviewFakeMasterDataRepository(),
                )
            advanceUntilIdle()

            viewModel.deleteReview(TEST_REVIEW_ID)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertNotNull(state.deleteError)
            assertEquals(R.string.error_delete_review, state.deleteError?.messageResId)
            assertEquals(1, state.reviews.size)
        }

    @Test
    fun deleteReview_doesNotExposeCancellationAsDeleteError() =
        runTest {
            val reviewRepository =
                RecordingReviewRepository(initial = listOf(testReview())).apply {
                    deleteFailure = CancellationException("cancelled")
                }
            val viewModel =
                ReviewListViewModel(
                    savedStateHandle = SavedStateHandle(mapOf(AppDestination.ARG_SAKE_ID to TEST_SAKE_ID)),
                    sakeRepository = RecordingSakeRepository(initial = listOf(testSake())),
                    reviewRepository = reviewRepository,
                    masterDataRepository = ReviewFakeMasterDataRepository(),
                )
            advanceUntilIdle()

            viewModel.deleteReview(TEST_REVIEW_ID)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(null, state.deleteError)
            assertEquals(1, state.reviews.size)
        }
}
