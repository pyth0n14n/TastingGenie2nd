package io.github.pyth0n14n.tastinggenie.feature.sake.edit

import androidx.lifecycle.SavedStateHandle
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.MasterDataBundle
import io.github.pyth0n14n.tastinggenie.domain.model.MasterOption
import io.github.pyth0n14n.tastinggenie.domain.model.Sake
import io.github.pyth0n14n.tastinggenie.domain.model.SakeId
import io.github.pyth0n14n.tastinggenie.domain.model.SakeInput
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeGrade
import io.github.pyth0n14n.tastinggenie.domain.repository.MasterDataRepository
import io.github.pyth0n14n.tastinggenie.domain.repository.SakeRepository
import io.github.pyth0n14n.tastinggenie.navigation.AppDestination
import io.github.pyth0n14n.tastinggenie.testutil.MainDispatcherRule
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

@OptIn(ExperimentalCoroutinesApi::class)
class SakeEditViewModelTest {
    companion object {
        private const val EXISTING_SAKE_ID = 7L
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun loadInitial_newMode_readsMasterData() =
        runTest {
            val sakeRepository = RecordingSakeRepository()
            val masterRepository = FakeMasterDataRepository()
            val viewModel =
                SakeEditViewModel(
                    savedStateHandle = SavedStateHandle(),
                    sakeRepository = sakeRepository,
                    masterDataRepository = masterRepository,
                )
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals(2, state.gradeOptions.size)
            assertEquals(null, state.sakeId)
        }

    @Test
    fun save_withInvalidInput_setsValidationError() =
        runTest {
            val viewModel =
                SakeEditViewModel(
                    savedStateHandle = SavedStateHandle(),
                    sakeRepository = RecordingSakeRepository(),
                    masterDataRepository = FakeMasterDataRepository(),
                )
            advanceUntilIdle()
            viewModel.onNameChanged("")
            viewModel.save()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertNotNull(state.error)
            assertEquals(R.string.error_invalid_sake_input, state.error?.messageResId)
        }

    @Test
    fun save_withValidInput_callsUpsertAndMarksSaved() =
        runTest {
            val repository = RecordingSakeRepository()
            val viewModel =
                SakeEditViewModel(
                    savedStateHandle = SavedStateHandle(),
                    sakeRepository = repository,
                    masterDataRepository = FakeMasterDataRepository(),
                )
            advanceUntilIdle()
            viewModel.onNameChanged("保存テスト")
            viewModel.onGradeSelected(SakeGrade.JUNMAI.name)
            viewModel.save()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.isSaved)
            assertEquals(1, repository.savedInputs.size)
            assertEquals("保存テスト", repository.savedInputs.first().name)
            assertEquals(SakeGrade.JUNMAI, repository.savedInputs.first().grade)
        }

    @Test
    fun onGradeSelected_withUnexpectedValue_setsUiErrorWithoutCrashing() =
        runTest {
            val viewModel =
                SakeEditViewModel(
                    savedStateHandle = SavedStateHandle(),
                    sakeRepository = RecordingSakeRepository(),
                    masterDataRepository = FakeMasterDataRepository(),
                )
            advanceUntilIdle()

            viewModel.onGradeSelected("BROKEN_VALUE")

            val state = viewModel.uiState.value
            assertEquals(null, state.grade)
            assertNotNull(state.error)
            assertEquals(R.string.error_invalid_sake_grade, state.error?.messageResId)
            assertEquals("BROKEN_VALUE", state.error?.causeKey)
        }

    @Test
    fun loadInitial_editMode_populatesExistingSake() =
        runTest {
            val repository =
                RecordingSakeRepository(
                    initial =
                        listOf(
                            Sake(
                                id = EXISTING_SAKE_ID,
                                name = "既存銘柄",
                                grade = SakeGrade.GINJO,
                            ),
                        ),
                )
            val viewModel =
                SakeEditViewModel(
                    savedStateHandle = SavedStateHandle(mapOf(AppDestination.ARG_SAKE_ID to EXISTING_SAKE_ID)),
                    sakeRepository = repository,
                    masterDataRepository = FakeMasterDataRepository(),
                )
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals(EXISTING_SAKE_ID, state.sakeId)
            assertEquals("既存銘柄", state.name)
            assertEquals(SakeGrade.GINJO, state.grade)
        }
}

private class RecordingSakeRepository(
    initial: List<Sake> = emptyList(),
) : SakeRepository {
    private val stream = MutableStateFlow(initial)
    val savedInputs = mutableListOf<SakeInput>()

    override fun observeSakes(): Flow<List<Sake>> = stream

    override suspend fun getSake(id: SakeId): Sake? = stream.value.firstOrNull { it.id == id }

    override suspend fun upsertSake(input: SakeInput): SakeId {
        savedInputs.add(input)
        val id = input.id ?: ((stream.value.maxOfOrNull { it.id } ?: 0L) + 1L)
        val mapped =
            Sake(
                id = id,
                name = input.name,
                grade = input.grade,
            )
        val mutable = stream.value.toMutableList().apply { removeAll { it.id == id } }
        mutable.add(mapped)
        stream.value = mutable
        return id
    }
}

private class FakeMasterDataRepository : MasterDataRepository {
    override suspend fun getMasterData(): MasterDataBundle =
        MasterDataBundle(
            sakeGrades =
                listOf(
                    MasterOption(value = SakeGrade.JUNMAI.name, label = "純米"),
                    MasterOption(value = SakeGrade.GINJO.name, label = "吟醸"),
                ),
            classifications = emptyList(),
            temperatures = emptyList(),
            colors = emptyList(),
            prefectures = emptyList(),
            intensityLevels = emptyList(),
            tasteLevels = emptyList(),
            overallReviews = emptyList(),
            aromaCategories = emptyList(),
        )
}
