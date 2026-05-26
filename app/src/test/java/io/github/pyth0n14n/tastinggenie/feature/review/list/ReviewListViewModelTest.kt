package io.github.pyth0n14n.tastinggenie.feature.review.list

import androidx.lifecycle.SavedStateHandle
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.AppSettings
import io.github.pyth0n14n.tastinggenie.domain.model.SakeFoodReview
import io.github.pyth0n14n.tastinggenie.domain.model.enums.FoodCompatibility
import io.github.pyth0n14n.tastinggenie.domain.model.enums.OverallReview
import io.github.pyth0n14n.tastinggenie.domain.repository.SettingsRepository
import io.github.pyth0n14n.tastinggenie.feature.review.RecordingReviewRepository
import io.github.pyth0n14n.tastinggenie.feature.review.RecordingSakeFoodReviewRepository
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

@OptIn(ExperimentalCoroutinesApi::class)
class ReviewListViewModelTest {
    private companion object {
        const val RATED_AND_UNRATED_REVIEW_COUNT = 3
        const val TEST_FOOD_REVIEW_ID = 21L
        const val TEST_FOOD_REVIEW_YEAR = 2026
        const val TEST_FOOD_REVIEW_MONTH = 5
        const val TEST_FOOD_REVIEW_DAY = 17
    }

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
                    foodReviewRepository = RecordingSakeFoodReviewRepository(),
                    masterDataRepository = ReviewFakeMasterDataRepository(),
                )
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals("テスト銘柄", state.sakeName)
            assertEquals(false, state.hasSakeImage)
            assertEquals(1, state.reviews.size)
            assertEquals(1, state.reviewCount)
            assertEquals("4.00", state.averageOverallReviewText)
            assertEquals("やや良い", state.overallReviewLabels["GOOD"])
            assertEquals("常温", state.temperatureLabels["JOON"])
            assertEquals("メロン", state.aromaLabels["MELON"])
            assertEquals("やや強い", state.tasteLabels["STRONG"])
            assertEquals(null, state.loadError)
        }

    @Test
    fun uiState_averageOverallReviewIgnoresUnratedReviews() =
        runTest {
            val viewModel =
                ReviewListViewModel(
                    savedStateHandle = SavedStateHandle(mapOf(AppDestination.ARG_SAKE_ID to TEST_SAKE_ID)),
                    sakeRepository = RecordingSakeRepository(initial = listOf(testSake())),
                    reviewRepository =
                        RecordingReviewRepository(
                            initial =
                                listOf(
                                    testReview(id = 1L, otherOverallReview = OverallReview.GOOD),
                                    testReview(id = 2L, otherOverallReview = OverallReview.VERY_GOOD),
                                    testReview(id = 3L, otherOverallReview = null),
                                ),
                        ),
                    foodReviewRepository = RecordingSakeFoodReviewRepository(),
                    masterDataRepository = ReviewFakeMasterDataRepository(),
                )
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(RATED_AND_UNRATED_REVIEW_COUNT, state.reviewCount)
            assertEquals("4.50", state.averageOverallReviewText)
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
                    foodReviewRepository = RecordingSakeFoodReviewRepository(),
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
                    foodReviewRepository = RecordingSakeFoodReviewRepository(),
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
                    foodReviewRepository = RecordingSakeFoodReviewRepository(),
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
                    foodReviewRepository = RecordingSakeFoodReviewRepository(),
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
                    foodReviewRepository = RecordingSakeFoodReviewRepository(),
                    masterDataRepository = ReviewFakeMasterDataRepository(),
                )
            advanceUntilIdle()

            viewModel.deleteReview(TEST_REVIEW_ID)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(null, state.deleteError)
            assertEquals(1, state.reviews.size)
        }

    @Test
    fun deleteFoodReview_removesFoodReviewAndClearsDeleteError() =
        runTest {
            val foodReviewRepository = RecordingSakeFoodReviewRepository(initial = listOf(testFoodReview()))
            val viewModel =
                ReviewListViewModel(
                    savedStateHandle = SavedStateHandle(mapOf(AppDestination.ARG_SAKE_ID to TEST_SAKE_ID)),
                    sakeRepository = RecordingSakeRepository(initial = listOf(testSake())),
                    reviewRepository = RecordingReviewRepository(),
                    foodReviewRepository = foodReviewRepository,
                    masterDataRepository = ReviewFakeMasterDataRepository(),
                )
            advanceUntilIdle()

            viewModel.deleteFoodReview(TEST_FOOD_REVIEW_ID)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(listOf(TEST_FOOD_REVIEW_ID), foodReviewRepository.deletedReviewIds)
            assertTrue(state.foodReviews.isEmpty())
            assertEquals(null, state.deleteError)
        }

    @Test
    fun deleteFoodReview_setsDeleteErrorWhenRepositoryFails() =
        runTest {
            val foodReviewRepository =
                RecordingSakeFoodReviewRepository(initial = listOf(testFoodReview())).apply {
                    deleteFailure = IllegalStateException("delete failed")
                }
            val viewModel =
                ReviewListViewModel(
                    savedStateHandle = SavedStateHandle(mapOf(AppDestination.ARG_SAKE_ID to TEST_SAKE_ID)),
                    sakeRepository = RecordingSakeRepository(initial = listOf(testSake())),
                    reviewRepository = RecordingReviewRepository(),
                    foodReviewRepository = foodReviewRepository,
                    masterDataRepository = ReviewFakeMasterDataRepository(),
                )
            advanceUntilIdle()

            viewModel.deleteFoodReview(TEST_FOOD_REVIEW_ID)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertNotNull(state.deleteError)
            assertEquals(R.string.error_delete_food_review, state.deleteError?.messageResId)
            assertEquals(1, state.foodReviews.size)
        }

    @Test
    fun emptyFabCoachmark_showsOnlyWhenOnboardingCompletedAndUnseen() =
        runTest {
            val settingsRepository =
                FakeSettingsRepository(
                    AppSettings(
                        onboardingCompleted = true,
                        reviewEmptyFabCoachmarkSeen = false,
                    ),
                )
            val viewModel =
                ReviewListViewModel(
                    savedStateHandle = SavedStateHandle(mapOf(AppDestination.ARG_SAKE_ID to TEST_SAKE_ID)),
                    sakeRepository = RecordingSakeRepository(initial = listOf(testSake())),
                    reviewRepository = RecordingReviewRepository(),
                    foodReviewRepository = RecordingSakeFoodReviewRepository(),
                    masterDataRepository = ReviewFakeMasterDataRepository(),
                    settingsRepository = settingsRepository,
                )
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.shouldShowReviewEmptyFabCoachmark)

            settingsRepository.updateReviewEmptyFabCoachmarkSeen(seen = true)
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.shouldShowReviewEmptyFabCoachmark)
        }

    @Test
    fun emptyFabCoachmark_hidesWhenReviewExistsOrOnboardingIncomplete() =
        runTest {
            val withReviewSettings = FakeSettingsRepository(AppSettings(onboardingCompleted = true))
            val withReview =
                ReviewListViewModel(
                    savedStateHandle = SavedStateHandle(mapOf(AppDestination.ARG_SAKE_ID to TEST_SAKE_ID)),
                    sakeRepository = RecordingSakeRepository(initial = listOf(testSake())),
                    reviewRepository = RecordingReviewRepository(initial = listOf(testReview())),
                    foodReviewRepository = RecordingSakeFoodReviewRepository(),
                    masterDataRepository = ReviewFakeMasterDataRepository(),
                    settingsRepository = withReviewSettings,
                )
            val onboardingIncomplete =
                ReviewListViewModel(
                    savedStateHandle = SavedStateHandle(mapOf(AppDestination.ARG_SAKE_ID to TEST_SAKE_ID)),
                    sakeRepository = RecordingSakeRepository(initial = listOf(testSake())),
                    reviewRepository = RecordingReviewRepository(),
                    foodReviewRepository = RecordingSakeFoodReviewRepository(),
                    masterDataRepository = ReviewFakeMasterDataRepository(),
                    settingsRepository = FakeSettingsRepository(AppSettings(onboardingCompleted = false)),
                )
            advanceUntilIdle()

            assertFalse(withReview.uiState.value.shouldShowReviewEmptyFabCoachmark)
            assertFalse(onboardingIncomplete.uiState.value.shouldShowReviewEmptyFabCoachmark)
            assertTrue(withReviewSettings.getCurrentSettings().reviewEmptyFabCoachmarkSeen)
        }

    @Test
    fun emptyFabCoachmark_dismissAndFabClickMarkSeen() =
        runTest {
            val settingsRepository =
                FakeSettingsRepository(
                    AppSettings(
                        onboardingCompleted = true,
                        reviewEmptyFabCoachmarkSeen = false,
                    ),
                )
            val viewModel =
                ReviewListViewModel(
                    savedStateHandle = SavedStateHandle(mapOf(AppDestination.ARG_SAKE_ID to TEST_SAKE_ID)),
                    sakeRepository = RecordingSakeRepository(initial = listOf(testSake())),
                    reviewRepository = RecordingReviewRepository(),
                    foodReviewRepository = RecordingSakeFoodReviewRepository(),
                    masterDataRepository = ReviewFakeMasterDataRepository(),
                    settingsRepository = settingsRepository,
                )
            advanceUntilIdle()

            viewModel.dismissReviewEmptyFabCoachmark()
            advanceUntilIdle()

            assertTrue(settingsRepository.getCurrentSettings().reviewEmptyFabCoachmarkSeen)

            settingsRepository.replaceSettings(
                settingsRepository.getCurrentSettings().copy(reviewEmptyFabCoachmarkSeen = false),
            )
            advanceUntilIdle()
            var added = false
            viewModel.addReviewFromFab { added = true }
            advanceUntilIdle()

            assertTrue(added)
            assertTrue(settingsRepository.getCurrentSettings().reviewEmptyFabCoachmarkSeen)
        }

    private fun testFoodReview(): SakeFoodReview =
        SakeFoodReview(
            id = TEST_FOOD_REVIEW_ID,
            sakeId = TEST_SAKE_ID,
            date = LocalDate.of(TEST_FOOD_REVIEW_YEAR, TEST_FOOD_REVIEW_MONTH, TEST_FOOD_REVIEW_DAY),
            dish = "焼き鳥",
            foodCompatibility = FoodCompatibility.GOOD,
        )
}

private class FakeSettingsRepository(
    initial: AppSettings = AppSettings(),
) : SettingsRepository {
    private val stream = MutableStateFlow(initial)

    override fun observeSettings(): Flow<AppSettings> = stream

    override suspend fun getCurrentSettings(): AppSettings = stream.value

    override suspend fun updateShowHelpHints(enabled: Boolean) {
        stream.value = stream.value.copy(showHelpHints = enabled)
    }

    override suspend fun updateShowReviewSoundness(enabled: Boolean) {
        stream.value = stream.value.copy(showReviewSoundness = enabled)
    }

    override suspend fun updateReviewMode(modeId: String) {
        stream.value = stream.value.copy(reviewModeId = modeId)
    }

    override suspend fun updateOnboardingCompleted(completed: Boolean) {
        stream.value = stream.value.copy(onboardingCompleted = completed)
    }

    override suspend fun updateSakeEmptyFabCoachmarkSeen(seen: Boolean) {
        stream.value = stream.value.copy(sakeEmptyFabCoachmarkSeen = seen)
    }

    override suspend fun updateReviewEmptyFabCoachmarkSeen(seen: Boolean) {
        stream.value = stream.value.copy(reviewEmptyFabCoachmarkSeen = seen)
    }

    override suspend fun replaceSettings(settings: AppSettings) {
        stream.value = settings
    }
}
