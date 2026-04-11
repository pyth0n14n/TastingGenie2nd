package io.github.pyth0n14n.tastinggenie.feature.review.edit

import androidx.lifecycle.SavedStateHandle
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.AppSettings
import io.github.pyth0n14n.tastinggenie.domain.model.enums.OverallReview
import io.github.pyth0n14n.tastinggenie.domain.model.enums.TasteLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Temperature
import io.github.pyth0n14n.tastinggenie.domain.repository.MasterDataRepository
import io.github.pyth0n14n.tastinggenie.domain.repository.SettingsRepository
import io.github.pyth0n14n.tastinggenie.feature.review.RecordingReviewRepository
import io.github.pyth0n14n.tastinggenie.feature.review.RecordingSakeRepository
import io.github.pyth0n14n.tastinggenie.feature.review.ReviewFakeMasterDataRepository
import io.github.pyth0n14n.tastinggenie.feature.review.TEST_REVIEW_ID
import io.github.pyth0n14n.tastinggenie.feature.review.TEST_SAKE_ID
import io.github.pyth0n14n.tastinggenie.feature.review.testReview
import io.github.pyth0n14n.tastinggenie.feature.review.testSake
import io.github.pyth0n14n.tastinggenie.navigation.AppDestination
import io.github.pyth0n14n.tastinggenie.testutil.MainDispatcherRule
import io.github.pyth0n14n.tastinggenie.ui.common.FieldValidationError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

private const val MAX_FIVE_STEP_VISCOSITY = 5

@OptIn(ExperimentalCoroutinesApi::class)
class ReviewEditViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun loadInitial_newMode_readsMasterDataAndSake() =
        runTest {
            val viewModel =
                reviewEditViewModel(
                    savedStateHandle = SavedStateHandle(mapOf(AppDestination.ARG_SAKE_ID to TEST_SAKE_ID)),
                )
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals(TEST_SAKE_ID, state.sakeId)
            assertEquals("テスト銘柄", state.sakeName)
            assertEquals(expectedDefaultDateText(), state.date)
            assertEquals(2, state.temperatureOptions.size)
        }

    @Test
    fun loadInitial_appliesReviewSoundnessVisibilitySetting() =
        runTest {
            val viewModel =
                reviewEditViewModel(
                    savedStateHandle = SavedStateHandle(mapOf(AppDestination.ARG_SAKE_ID to TEST_SAKE_ID)),
                    settingsRepository = FakeSettingsRepository(AppSettings(showReviewSoundness = false)),
                )
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.showReviewSoundness)
        }

    @Test
    fun save_withInvalidDate_setsValidationError() =
        runTest {
            val repository = RecordingReviewRepository()
            val viewModel =
                reviewEditViewModel(
                    savedStateHandle = SavedStateHandle(mapOf(AppDestination.ARG_SAKE_ID to TEST_SAKE_ID)),
                    reviewRepository = repository,
                )
            advanceUntilIdle()

            viewModel.onAction(ReviewEditAction.TextChanged(field = ReviewTextField.DATE, value = "invalid-date"))
            viewModel.save()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(null, state.error)
            assertEquals(FieldValidationError.INVALID_DATE, state.validationErrors[ReviewValidationField.DATE])
            assertTrue(repository.savedInputs.isEmpty())
        }

    @Test
    fun save_withInvalidNumericFields_setsFieldValidationErrors() =
        runTest {
            val repository = RecordingReviewRepository()
            val viewModel =
                reviewEditViewModel(
                    savedStateHandle = SavedStateHandle(mapOf(AppDestination.ARG_SAKE_ID to TEST_SAKE_ID)),
                    reviewRepository = repository,
                )
            advanceUntilIdle()

            viewModel.onAction(ReviewEditAction.TextChanged(field = ReviewTextField.PRICE, value = "100円"))
            viewModel.onAction(ReviewEditAction.TextChanged(field = ReviewTextField.VOLUME, value = "一合"))
            viewModel.save()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(
                FieldValidationError.INVALID_NUMBER,
                state.validationErrors[ReviewValidationField.PRICE],
            )
            assertEquals(
                FieldValidationError.INVALID_NUMBER,
                state.validationErrors[ReviewValidationField.VOLUME],
            )
            assertTrue(repository.savedInputs.isEmpty())
        }

    @Test
    fun save_withOutOfRangeReviewNumbers_setsRangeValidationErrors() =
        runTest {
            val repository = RecordingReviewRepository()
            val viewModel =
                reviewEditViewModel(
                    savedStateHandle = SavedStateHandle(mapOf(AppDestination.ARG_SAKE_ID to TEST_SAKE_ID)),
                    reviewRepository = repository,
                )
            advanceUntilIdle()

            viewModel.onAction(ReviewEditAction.TextChanged(field = ReviewTextField.PRICE, value = "1000001"))
            viewModel.onAction(ReviewEditAction.TextChanged(field = ReviewTextField.VOLUME, value = "0"))
            viewModel.save()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(
                FieldValidationError.INVALID_INTEGER_RANGE,
                state.validationErrors[ReviewValidationField.PRICE],
            )
            assertEquals(
                FieldValidationError.INVALID_INTEGER_RANGE,
                state.validationErrors[ReviewValidationField.VOLUME],
            )
            assertTrue(repository.savedInputs.isEmpty())
        }

    @Test
    fun save_withValidInput_callsUpsertAndMarksSaved() =
        runTest {
            val repository = RecordingReviewRepository()
            val viewModel =
                reviewEditViewModel(
                    savedStateHandle = SavedStateHandle(mapOf(AppDestination.ARG_SAKE_ID to TEST_SAKE_ID)),
                    reviewRepository = repository,
                )
            advanceUntilIdle()

            viewModel.onAction(ReviewEditAction.DateSelected(epochMillis = testDateMillis()))
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
    fun onDateSelected_formatsReviewDateText() =
        runTest {
            val viewModel =
                reviewEditViewModel(
                    savedStateHandle = SavedStateHandle(mapOf(AppDestination.ARG_SAKE_ID to TEST_SAKE_ID)),
                )
            advanceUntilIdle()

            viewModel.onAction(ReviewEditAction.DateSelected(epochMillis = testDateMillis()))

            val state = viewModel.uiState.value
            assertEquals("2026-03-14", state.date)
            assertEquals(null, state.error)
        }

    @Test
    fun save_withoutChangingDate_usesTodayByDefault() =
        runTest {
            val repository = RecordingReviewRepository()
            val viewModel =
                reviewEditViewModel(
                    savedStateHandle = SavedStateHandle(mapOf(AppDestination.ARG_SAKE_ID to TEST_SAKE_ID)),
                    reviewRepository = repository,
                )
            advanceUntilIdle()

            viewModel.onAction(
                ReviewEditAction.SelectionChanged(
                    field = ReviewSelectionField.TEMPERATURE,
                    value = Temperature.JOON.name,
                ),
            )
            viewModel.save()
            advanceUntilIdle()

            val savedInput = repository.savedInputs.single()
            val savedDate = savedInput.date.toString()
            assertEquals(expectedDefaultDateText(), savedDate)
        }

    @Test
    fun onTemperatureSelected_withUnexpectedValue_setsUiErrorWithoutCrashing() =
        runTest {
            val viewModel =
                reviewEditViewModel(
                    savedStateHandle = SavedStateHandle(mapOf(AppDestination.ARG_SAKE_ID to TEST_SAKE_ID)),
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
    fun blankSelection_clearsOptionalReviewRatings() =
        runTest {
            val viewModel =
                reviewEditViewModel(
                    savedStateHandle = SavedStateHandle(mapOf(AppDestination.ARG_SAKE_ID to TEST_SAKE_ID)),
                )
            advanceUntilIdle()

            viewModel.onAction(
                ReviewEditAction.SelectionChanged(
                    field = ReviewSelectionField.SWEET,
                    value = TasteLevel.STRONG.name,
                ),
            )
            viewModel.onAction(
                ReviewEditAction.SelectionChanged(
                    field = ReviewSelectionField.OVERALL_REVIEW,
                    value = OverallReview.GOOD.name,
                ),
            )
            viewModel.onAction(
                ReviewEditAction.SelectionChanged(
                    field = ReviewSelectionField.SWEET,
                    value = "",
                ),
            )
            viewModel.onAction(
                ReviewEditAction.SelectionChanged(
                    field = ReviewSelectionField.OVERALL_REVIEW,
                    value = "",
                ),
            )

            val state = viewModel.uiState.value
            assertEquals(null, state.sweet)
            assertEquals(null, state.review)
            assertEquals(null, state.error)
        }

    @Test
    fun viscosity_acceptsFiveStepMaximum() =
        runTest {
            val viewModel =
                reviewEditViewModel(
                    savedStateHandle = SavedStateHandle(mapOf(AppDestination.ARG_SAKE_ID to TEST_SAKE_ID)),
                )
            advanceUntilIdle()

            viewModel.onAction(
                ReviewEditAction.SelectionChanged(
                    field = ReviewSelectionField.VISCOSITY,
                    value = MAX_FIVE_STEP_VISCOSITY.toString(),
                ),
            )

            val state = viewModel.uiState.value
            assertEquals(MAX_FIVE_STEP_VISCOSITY, state.viscosity)
            assertEquals(null, state.error)
        }

    @Test
    fun loadInitial_editModeWhenSeedLoadFails_blocksSave() =
        runTest {
            val repository = RecordingReviewRepository(initial = listOf(testReview()))
            val viewModel =
                reviewEditViewModel(
                    savedStateHandle =
                        SavedStateHandle(
                            mapOf(
                                AppDestination.ARG_SAKE_ID to TEST_SAKE_ID,
                                AppDestination.ARG_REVIEW_ID to TEST_REVIEW_ID,
                            ),
                        ),
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
                reviewEditViewModel(
                    savedStateHandle =
                        SavedStateHandle(
                            mapOf(
                                AppDestination.ARG_SAKE_ID to TEST_SAKE_ID,
                                AppDestination.ARG_REVIEW_ID to TEST_REVIEW_ID,
                            ),
                        ),
                    reviewRepository = repository,
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
    override suspend fun getMasterData() = error("seed load failure")
}

private class FakeSettingsRepository(
    initial: AppSettings = AppSettings(),
) : SettingsRepository {
    private val stream = MutableStateFlow(initial)

    override fun observeSettings(): Flow<AppSettings> = stream

    override suspend fun updateShowHelpHints(enabled: Boolean) = Unit

    override suspend fun updateShowImagePreview(enabled: Boolean) = Unit

    override suspend fun updateShowReviewSoundness(enabled: Boolean) {
        stream.value = stream.value.copy(showReviewSoundness = enabled)
    }
}

private fun reviewEditViewModel(
    savedStateHandle: SavedStateHandle,
    sakeRepository: RecordingSakeRepository = RecordingSakeRepository(initial = listOf(testSake())),
    reviewRepository: RecordingReviewRepository = RecordingReviewRepository(),
    masterDataRepository: MasterDataRepository = ReviewFakeMasterDataRepository(),
    settingsRepository: SettingsRepository = FakeSettingsRepository(),
): ReviewEditViewModel =
    ReviewEditViewModel(
        savedStateHandle = savedStateHandle,
        sakeRepository = sakeRepository,
        reviewRepository = reviewRepository,
        masterDataRepository = masterDataRepository,
        settingsRepository = settingsRepository,
    )

private fun testDateMillis(): Long {
    val localDate = LocalDate.parse("2026-03-14")
    return localDate
        .atStartOfDay(ZoneOffset.UTC)
        .toInstant()
        .toEpochMilli()
}

private fun expectedDefaultDateText(): String = defaultReviewDateText()
