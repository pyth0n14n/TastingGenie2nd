package io.github.pyth0n14n.tastinggenie.feature.review.edit

import androidx.lifecycle.SavedStateHandle
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Temperature
import io.github.pyth0n14n.tastinggenie.domain.repository.MasterDataRepository
import io.github.pyth0n14n.tastinggenie.feature.review.RecordingReviewRepository
import io.github.pyth0n14n.tastinggenie.feature.review.RecordingSakeRepository
import io.github.pyth0n14n.tastinggenie.feature.review.ReviewFakeMasterDataRepository
import io.github.pyth0n14n.tastinggenie.feature.review.TEST_REVIEW_ID
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
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReviewEditViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun loadInitial_newMode_readsMasterDataAndSake() =
        runTest {
            val viewModel =
                ReviewEditViewModel(
                    savedStateHandle = SavedStateHandle(mapOf(AppDestination.ARG_SAKE_ID to TEST_SAKE_ID)),
                    sakeRepository = RecordingSakeRepository(initial = listOf(testSake())),
                    reviewRepository = RecordingReviewRepository(),
                    masterDataRepository = ReviewFakeMasterDataRepository(),
                )
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals(TEST_SAKE_ID, state.sakeId)
            assertEquals("テスト銘柄", state.sakeName)
            assertEquals(2, state.temperatureOptions.size)
        }

    @Test
    fun save_withInvalidDate_setsValidationError() =
        runTest {
            val repository = RecordingReviewRepository()
            val viewModel =
                ReviewEditViewModel(
                    savedStateHandle = SavedStateHandle(mapOf(AppDestination.ARG_SAKE_ID to TEST_SAKE_ID)),
                    sakeRepository = RecordingSakeRepository(initial = listOf(testSake())),
                    reviewRepository = repository,
                    masterDataRepository = ReviewFakeMasterDataRepository(),
                )
            advanceUntilIdle()

            viewModel.onAction(ReviewEditAction.TextChanged(field = ReviewTextField.DATE, value = "invalid-date"))
            viewModel.save()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(R.string.error_invalid_review_input, state.error?.messageResId)
            assertTrue(repository.savedInputs.isEmpty())
        }

    @Test
    fun save_withValidInput_callsUpsertAndMarksSaved() =
        runTest {
            val repository = RecordingReviewRepository()
            val viewModel =
                ReviewEditViewModel(
                    savedStateHandle = SavedStateHandle(mapOf(AppDestination.ARG_SAKE_ID to TEST_SAKE_ID)),
                    sakeRepository = RecordingSakeRepository(initial = listOf(testSake())),
                    reviewRepository = repository,
                    masterDataRepository = ReviewFakeMasterDataRepository(),
                )
            advanceUntilIdle()

            viewModel.onAction(ReviewEditAction.TextChanged(field = ReviewTextField.DATE, value = "2026-03-14"))
            viewModel.onAction(
                ReviewEditAction.SelectionChanged(
                    field = ReviewSelectionField.TEMPERATURE,
                    value = Temperature.JOON.name,
                ),
            )
            viewModel.save()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.isSaved)
            assertEquals(1, repository.savedInputs.size)
            assertEquals(TEST_SAKE_ID, repository.savedInputs.first().sakeId)
            assertEquals(Temperature.JOON, repository.savedInputs.first().temperature)
        }

    @Test
    fun onTemperatureSelected_withUnexpectedValue_setsUiErrorWithoutCrashing() =
        runTest {
            val viewModel =
                ReviewEditViewModel(
                    savedStateHandle = SavedStateHandle(mapOf(AppDestination.ARG_SAKE_ID to TEST_SAKE_ID)),
                    sakeRepository = RecordingSakeRepository(initial = listOf(testSake())),
                    reviewRepository = RecordingReviewRepository(),
                    masterDataRepository = ReviewFakeMasterDataRepository(),
                )
            advanceUntilIdle()

            viewModel.onAction(
                ReviewEditAction.SelectionChanged(
                    field = ReviewSelectionField.TEMPERATURE,
                    value = "BROKEN_VALUE",
                ),
            )

            val state = viewModel.uiState.value
            assertEquals(null, state.temperature)
            assertNotNull(state.error)
            assertEquals(R.string.error_invalid_review_selection, state.error?.messageResId)
            assertEquals("BROKEN_VALUE", state.error?.causeKey)
        }

    @Test
    fun loadInitial_editModeWhenSeedLoadFails_blocksSave() =
        runTest {
            val repository = RecordingReviewRepository(initial = listOf(testReview()))
            val viewModel =
                ReviewEditViewModel(
                    savedStateHandle =
                        SavedStateHandle(
                            mapOf(
                                AppDestination.ARG_SAKE_ID to TEST_SAKE_ID,
                                AppDestination.ARG_REVIEW_ID to TEST_REVIEW_ID,
                            ),
                        ),
                    sakeRepository = RecordingSakeRepository(initial = listOf(testSake())),
                    reviewRepository = repository,
                    masterDataRepository = ThrowingMasterDataRepository(),
                )
            advanceUntilIdle()

            viewModel.onAction(ReviewEditAction.TextChanged(field = ReviewTextField.DATE, value = "2026-03-14"))
            viewModel.save()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.isEditTargetMissing)
            assertEquals(TEST_REVIEW_ID, state.reviewId)
            assertEquals(R.string.error_load_review, state.error?.messageResId)
            assertTrue(repository.savedInputs.isEmpty())
        }

    @Test
    fun loadInitial_editModeWithMissingReview_setsErrorAndBlocksSave() =
        runTest {
            val repository = RecordingReviewRepository()
            val viewModel =
                ReviewEditViewModel(
                    savedStateHandle =
                        SavedStateHandle(
                            mapOf(
                                AppDestination.ARG_SAKE_ID to TEST_SAKE_ID,
                                AppDestination.ARG_REVIEW_ID to TEST_REVIEW_ID,
                            ),
                        ),
                    sakeRepository = RecordingSakeRepository(initial = listOf(testSake())),
                    reviewRepository = repository,
                    masterDataRepository = ReviewFakeMasterDataRepository(),
                )
            advanceUntilIdle()

            viewModel.onAction(ReviewEditAction.TextChanged(field = ReviewTextField.DATE, value = "2026-03-14"))
            viewModel.save()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.isEditTargetMissing)
            assertEquals(R.string.error_load_review, state.error?.messageResId)
            assertTrue(repository.savedInputs.isEmpty())
        }
}

private class ThrowingMasterDataRepository : MasterDataRepository {
    override suspend fun getMasterData() = throw IllegalStateException("seed load failure")
}
