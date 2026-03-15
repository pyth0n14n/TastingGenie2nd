package io.github.pyth0n14n.tastinggenie.feature.review.image

import androidx.lifecycle.SavedStateHandle
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.feature.review.RecordingReviewRepository
import io.github.pyth0n14n.tastinggenie.feature.review.TEST_REVIEW_ID
import io.github.pyth0n14n.tastinggenie.feature.review.testReview
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
class ReviewImageViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun loadImage_populatesImageUri() =
        runTest {
            val viewModel =
                ReviewImageViewModel(
                    savedStateHandle = SavedStateHandle(mapOf(AppDestination.ARG_REVIEW_ID to TEST_REVIEW_ID)),
                    reviewRepository =
                        RecordingReviewRepository(
                            initial = listOf(testReview().copy(imageUri = "content://review/image/1")),
                        ),
                )
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals("content://review/image/1", state.imageUri)
        }

    @Test
    fun loadImage_missingReview_setsError() =
        runTest {
            val viewModel =
                ReviewImageViewModel(
                    savedStateHandle = SavedStateHandle(mapOf(AppDestination.ARG_REVIEW_ID to TEST_REVIEW_ID)),
                    reviewRepository = RecordingReviewRepository(),
                )
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertNotNull(state.error)
            assertEquals(R.string.error_load_review, state.error?.messageResId)
        }
}
