package io.github.pyth0n14n.tastinggenie.feature.sake.list

import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.Sake
import io.github.pyth0n14n.tastinggenie.domain.model.SakeId
import io.github.pyth0n14n.tastinggenie.domain.model.SakeInput
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeGrade
import io.github.pyth0n14n.tastinggenie.domain.repository.SakeRepository
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
            val viewModel = SakeListViewModel(repository)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals(1, state.sakes.size)
            assertEquals("テスト銘柄", state.sakes.first().name)
            assertEquals(null, state.error)
        }

    @Test
    fun uiState_setsErrorWhenRepositoryFails() =
        runTest {
            val repository = FailingObserveSakeRepository()
            val viewModel = SakeListViewModel(repository)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertNotNull(state.error)
            assertEquals(R.string.error_load_sakes, state.error?.messageResId)
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
