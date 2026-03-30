package io.github.pyth0n14n.tastinggenie.feature.sake.list

import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.AppSettings
import io.github.pyth0n14n.tastinggenie.domain.model.MasterDataBundle
import io.github.pyth0n14n.tastinggenie.domain.model.MasterOption
import io.github.pyth0n14n.tastinggenie.domain.model.Sake
import io.github.pyth0n14n.tastinggenie.domain.model.SakeId
import io.github.pyth0n14n.tastinggenie.domain.model.SakeInput
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeGrade
import io.github.pyth0n14n.tastinggenie.domain.repository.MasterDataRepository
import io.github.pyth0n14n.tastinggenie.domain.repository.SakeRepository
import io.github.pyth0n14n.tastinggenie.domain.repository.SettingsRepository
import io.github.pyth0n14n.tastinggenie.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SakeListViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun uiState_updatesWhenRepositoryEmitsSakes() =
        runTest {
            val repository =
                FakeSakeRepository(
                    initial =
                        listOf(
                            Sake(
                                id = 1L,
                                name = "テスト銘柄",
                                grade = SakeGrade.JUNMAI,
                            ),
                        ),
                )
            val viewModel =
                SakeListViewModel(
                    repository,
                    FakeMasterDataRepository(),
                    FakeSettingsRepository(),
                )
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals(1, state.sakes.size)
            assertEquals("テスト銘柄", state.sakes.first().name)
            assertEquals("純米", state.gradeLabels[SakeGrade.JUNMAI.name])
            assertEquals(true, state.showImagePreview)
            assertEquals(null, state.error)
        }

    @Test
    fun uiState_setsErrorWhenRepositoryFails() =
        runTest {
            val repository = FailingObserveSakeRepository()
            val viewModel =
                SakeListViewModel(
                    repository,
                    FakeMasterDataRepository(),
                    FakeSettingsRepository(),
                )
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertNotNull(state.error)
            assertEquals(R.string.error_load_sakes, state.error?.messageResId)
        }

    @Test
    fun uiState_updatesWhenImagePreviewSettingChanges() =
        runTest {
            val settingsRepository = FakeSettingsRepository()
            val viewModel =
                SakeListViewModel(
                    FakeSakeRepository(initial = emptyList()),
                    FakeMasterDataRepository(),
                    settingsRepository,
                )
            advanceUntilIdle()

            settingsRepository.updateShowImagePreview(enabled = false)
            advanceUntilIdle()

            assertEquals(false, viewModel.uiState.value.showImagePreview)
        }
}

private class FakeSakeRepository(
    initial: List<Sake>,
) : SakeRepository {
    private val stream = MutableStateFlow(initial)

    override fun observeSakes(): Flow<List<Sake>> = stream

    override suspend fun getSake(id: SakeId): Sake? = stream.value.firstOrNull { it.id == id }

    override suspend fun upsertSake(input: SakeInput): SakeId {
        val id = input.id ?: ((stream.value.maxOfOrNull { it.id } ?: 0L) + 1L)
        val mapped =
            Sake(
                id = id,
                name = input.name,
                grade = input.grade,
            )
        val mutable = stream.value.toMutableList().apply { removeAll { it.id == id } }
        mutable.add(mapped)
        stream.value = mutable.sortedBy { it.id }
        return id
    }
}

private class FailingObserveSakeRepository : SakeRepository {
    override fun observeSakes(): Flow<List<Sake>> = flow { throw IllegalStateException("failed") }

    override suspend fun getSake(id: SakeId): Sake? = null

    override suspend fun upsertSake(input: SakeInput): SakeId = 1L
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

private class FakeSettingsRepository : SettingsRepository {
    private val stream = MutableStateFlow(AppSettings())

    override fun observeSettings(): Flow<AppSettings> = stream

    override suspend fun updateShowHelpHints(enabled: Boolean) {
        stream.value = stream.value.copy(showHelpHints = enabled)
    }

    override suspend fun updateShowImagePreview(enabled: Boolean) {
        stream.value = stream.value.copy(showImagePreview = enabled)
    }
}
