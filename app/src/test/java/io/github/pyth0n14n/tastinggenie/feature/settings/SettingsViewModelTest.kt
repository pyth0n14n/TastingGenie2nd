package io.github.pyth0n14n.tastinggenie.feature.settings

import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.AppSettings
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
class SettingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun loadInitial_populatesSettings() =
        runTest {
            val repository = FakeSettingsRepository()
            val viewModel = SettingsViewModel(settingsRepository = repository)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals(true, state.settings.showHelpHints)
            assertEquals(true, state.settings.showImagePreview)
        }

    @Test
    fun toggleImagePreview_updatesState() =
        runTest {
            val repository = FakeSettingsRepository()
            val viewModel = SettingsViewModel(settingsRepository = repository)
            advanceUntilIdle()

            viewModel.toggleImagePreview(enabled = false)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(false, state.settings.showImagePreview)
        }

    @Test
    fun toggleHelpHints_failureSetsError() =
        runTest {
            val repository = FailingUpdateSettingsRepository()
            val viewModel = SettingsViewModel(settingsRepository = repository)
            advanceUntilIdle()

            viewModel.toggleHelpHints(enabled = false)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertNotNull(state.error)
            assertEquals(R.string.error_save_settings, state.error?.messageResId)
        }
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

private class FailingUpdateSettingsRepository : SettingsRepository {
    override fun observeSettings(): Flow<AppSettings> = flow { emit(AppSettings()) }

    override suspend fun updateShowHelpHints(enabled: Boolean) {
        error("settings write failed")
    }

    override suspend fun updateShowImagePreview(enabled: Boolean) {
        error("settings write failed")
    }
}
