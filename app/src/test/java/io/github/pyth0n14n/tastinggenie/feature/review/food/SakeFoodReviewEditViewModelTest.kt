package io.github.pyth0n14n.tastinggenie.feature.review.food

import androidx.lifecycle.SavedStateHandle
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.SakeFoodReview
import io.github.pyth0n14n.tastinggenie.domain.model.enums.FoodCompatibility
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Temperature
import io.github.pyth0n14n.tastinggenie.feature.review.RecordingSakeFoodReviewRepository
import io.github.pyth0n14n.tastinggenie.feature.review.RecordingSakeRepository
import io.github.pyth0n14n.tastinggenie.feature.review.ReviewFakeMasterDataRepository
import io.github.pyth0n14n.tastinggenie.feature.review.TEST_SAKE_ID
import io.github.pyth0n14n.tastinggenie.feature.review.testSake
import io.github.pyth0n14n.tastinggenie.navigation.AppDestination
import io.github.pyth0n14n.tastinggenie.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class SakeFoodReviewEditViewModelTest {
    private companion object {
        const val TEST_FOOD_REVIEW_ID = 31L
        const val TEST_FOOD_REVIEW_YEAR = 2026
        const val TEST_FOOD_REVIEW_MONTH = 5
        const val TEST_FOOD_REVIEW_DAY = 17
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun loadInitial_newMode_populatesSakeAndTemperatureOptions() =
        runTest {
            val viewModel = foodReviewEditViewModel()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals(TEST_SAKE_ID, state.sakeId)
            assertEquals("テスト銘柄", state.sakeName)
            assertEquals(listOf("JOON", "HANABIE"), state.temperatureOptions.map { it.value })
            assertEquals(null, state.error)
        }

    @Test
    fun save_withValidInput_callsUpsertAndMarksSaved() =
        runTest {
            val repository = RecordingSakeFoodReviewRepository()
            val viewModel = foodReviewEditViewModel(foodReviewRepository = repository)
            advanceUntilIdle()

            viewModel.onDishChanged(" 刺身 ")
            viewModel.onCompatibilityChanged(FoodCompatibility.GOOD.name)
            viewModel.onTemperatureChanged(Temperature.JOON.name)
            viewModel.onBarChanged(" テスト店 ")
            viewModel.onCommentChanged(" よく合う ")
            viewModel.save()
            advanceUntilIdle()

            val saved = repository.savedInputs.single()
            assertEquals(TEST_SAKE_ID, saved.sakeId)
            assertEquals("刺身", saved.dish)
            assertEquals(FoodCompatibility.GOOD, saved.foodCompatibility)
            assertEquals(Temperature.JOON, saved.temperature)
            assertEquals("テスト店", saved.bar)
            assertEquals("よく合う", saved.freeComment)
            assertTrue(viewModel.uiState.value.isSaved)
            assertEquals(null, viewModel.uiState.value.error)
        }

    @Test
    fun save_calledTwiceBeforeFirstSaveFinishes_persistsOnlyOnce() =
        runTest {
            val repository = RecordingSakeFoodReviewRepository()
            val viewModel = foodReviewEditViewModel(foodReviewRepository = repository)
            advanceUntilIdle()
            viewModel.onDishChanged("刺身")
            viewModel.onCompatibilityChanged(FoodCompatibility.GOOD.name)

            viewModel.save()
            viewModel.save()
            advanceUntilIdle()

            assertEquals(1, repository.savedInputs.size)
            assertTrue(viewModel.uiState.value.isSaved)
        }

    @Test
    fun save_withoutDish_setsValidationErrorAndDoesNotPersist() =
        runTest {
            val repository = RecordingSakeFoodReviewRepository()
            val viewModel = foodReviewEditViewModel(foodReviewRepository = repository)
            advanceUntilIdle()

            viewModel.onCompatibilityChanged(FoodCompatibility.GOOD.name)
            viewModel.save()
            advanceUntilIdle()

            assertTrue(repository.savedInputs.isEmpty())
            assertEquals(
                R.string.error_invalid_food_review_input,
                viewModel.uiState.value.error
                    ?.messageResId,
            )
            assertFalse(viewModel.uiState.value.isSaved)
        }

    @Test
    fun save_withoutCompatibility_setsValidationErrorAndDoesNotPersist() =
        runTest {
            val repository = RecordingSakeFoodReviewRepository()
            val viewModel = foodReviewEditViewModel(foodReviewRepository = repository)
            advanceUntilIdle()

            viewModel.onDishChanged("刺身")
            viewModel.save()
            advanceUntilIdle()

            assertTrue(repository.savedInputs.isEmpty())
            assertEquals(
                R.string.error_invalid_food_review_input,
                viewModel.uiState.value.error
                    ?.messageResId,
            )
            assertFalse(viewModel.uiState.value.isSaved)
        }

    @Test
    fun loadInitial_editMode_populatesExistingReview() =
        runTest {
            val viewModel =
                foodReviewEditViewModel(
                    savedStateHandle =
                        SavedStateHandle(
                            mapOf(
                                AppDestination.ARG_SAKE_ID to TEST_SAKE_ID,
                                AppDestination.ARG_FOOD_REVIEW_ID to TEST_FOOD_REVIEW_ID,
                            ),
                        ),
                    foodReviewRepository = RecordingSakeFoodReviewRepository(initial = listOf(testFoodReview())),
                )
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals(TEST_FOOD_REVIEW_ID, state.reviewId)
            assertEquals("2026-05-17", state.date)
            assertEquals("焼き鳥", state.dish)
            assertEquals(FoodCompatibility.SLIGHTLY_GOOD, state.foodCompatibility)
            assertEquals(Temperature.JOON, state.temperature)
            assertEquals("相性メモ", state.freeComment)
            assertEquals(null, state.error)
        }

    @Test
    fun save_editMode_preservesReviewIdAndUpdatesFields() =
        runTest {
            val repository = RecordingSakeFoodReviewRepository(initial = listOf(testFoodReview()))
            val viewModel =
                foodReviewEditViewModel(
                    savedStateHandle =
                        SavedStateHandle(
                            mapOf(
                                AppDestination.ARG_SAKE_ID to TEST_SAKE_ID,
                                AppDestination.ARG_FOOD_REVIEW_ID to TEST_FOOD_REVIEW_ID,
                            ),
                        ),
                    foodReviewRepository = repository,
                )
            advanceUntilIdle()

            viewModel.onDishChanged("塩辛")
            viewModel.onCompatibilityChanged(FoodCompatibility.BAD.name)
            viewModel.save()
            advanceUntilIdle()

            val saved = repository.savedInputs.single()
            assertEquals(TEST_FOOD_REVIEW_ID, saved.id)
            assertEquals("塩辛", saved.dish)
            assertEquals(FoodCompatibility.BAD, saved.foodCompatibility)
            assertTrue(viewModel.uiState.value.isSaved)
        }

    private fun foodReviewEditViewModel(
        savedStateHandle: SavedStateHandle = SavedStateHandle(mapOf(AppDestination.ARG_SAKE_ID to TEST_SAKE_ID)),
        sakeRepository: RecordingSakeRepository = RecordingSakeRepository(initial = listOf(testSake())),
        foodReviewRepository: RecordingSakeFoodReviewRepository = RecordingSakeFoodReviewRepository(),
    ): SakeFoodReviewEditViewModel =
        SakeFoodReviewEditViewModel(
            savedStateHandle = savedStateHandle,
            sakeRepository = sakeRepository,
            foodReviewRepository = foodReviewRepository,
            masterDataRepository = ReviewFakeMasterDataRepository(),
        )

    private fun testFoodReview(): SakeFoodReview =
        SakeFoodReview(
            id = TEST_FOOD_REVIEW_ID,
            sakeId = TEST_SAKE_ID,
            date = LocalDate.of(TEST_FOOD_REVIEW_YEAR, TEST_FOOD_REVIEW_MONTH, TEST_FOOD_REVIEW_DAY),
            bar = "テスト店",
            dish = "焼き鳥",
            foodCompatibility = FoodCompatibility.SLIGHTLY_GOOD,
            temperature = Temperature.JOON,
            freeComment = "相性メモ",
        )
}
