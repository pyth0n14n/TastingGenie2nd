package io.github.pyth0n14n.tastinggenie.feature.review.list

import androidx.lifecycle.SavedStateHandle
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.feature.review.RecordingReviewRepository
import io.github.pyth0n14n.tastinggenie.feature.review.RecordingSakeRepository
import io.github.pyth0n14n.tastinggenie.feature.review.ReviewFakeMasterDataRepository
import io.github.pyth0n14n.tastinggenie.feature.review.TEST_SAKE_ID
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
            assertEquals(1, state.reviews.size)
            assertEquals("好き", state.overallReviewLabels["GOOD"])
            assertEquals(null, state.error)
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
            assertNotNull(state.error)
            assertEquals(R.string.error_load_sake, state.error?.messageResId)
        }
}
