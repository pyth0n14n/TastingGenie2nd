package io.github.pyth0n14n.tastinggenie.feature.settings

import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.AppSettings
import io.github.pyth0n14n.tastinggenie.domain.model.InvalidBackupReferenceException
import io.github.pyth0n14n.tastinggenie.domain.model.UnsupportedSchemaVersionException
import io.github.pyth0n14n.tastinggenie.domain.repository.ImportExportRepository
import io.github.pyth0n14n.tastinggenie.domain.repository.SettingsRepository
import io.github.pyth0n14n.tastinggenie.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import org.junit.Assert
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
            val viewModel =
                SettingsViewModel(
                    settingsRepository = repository,
                    importExportRepository = FakeImportExportRepository(),
                )
            advanceUntilIdle()

            val state = viewModel.uiState.value
            Assert.assertFalse(state.isLoading)
            Assert.assertEquals(true, state.settings.showHelpHints)
            Assert.assertEquals(true, state.settings.showImagePreview)
        }

    @Test
    fun toggleImagePreview_updatesState() =
        runTest {
            val repository = FakeSettingsRepository()
            val viewModel =
                SettingsViewModel(
                    settingsRepository = repository,
                    importExportRepository = FakeImportExportRepository(),
                )
            advanceUntilIdle()

            viewModel.toggleImagePreview(enabled = false)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            Assert.assertEquals(false, state.settings.showImagePreview)
        }

    @Test
    fun toggleHelpHints_failureSetsError() =
        runTest {
            val repository = FailingUpdateSettingsRepository()
            val viewModel =
                SettingsViewModel(
                    settingsRepository = repository,
                    importExportRepository = FakeImportExportRepository(),
                )
            advanceUntilIdle()

            viewModel.toggleHelpHints(enabled = false)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            Assert.assertNotNull(state.error)
            Assert.assertEquals(R.string.error_save_settings, state.error?.messageResId)
        }

    @Test
    fun exportJson_successReturnsPayloadAndPublishesSuccessMessage() =
        runTest {
            val viewModel =
                SettingsViewModel(
                    settingsRepository = FakeSettingsRepository(),
                    importExportRepository =
                        FakeImportExportRepository(
                            exportJson = """{"schemaVersion":1}""",
                        ),
                )
            advanceUntilIdle()

            val rawJson = viewModel.exportJson()
            viewModel.completeExport(Result.success(Unit))

            val state = viewModel.uiState.value
            Assert.assertEquals("""{"schemaVersion":1}""", rawJson)
            Assert.assertEquals(R.string.message_export_success, state.messageResId)
            Assert.assertFalse(state.isProcessingTransfer)
            Assert.assertNull(state.error)
        }

    @Test
    fun exportJson_failureSetsError() =
        runTest {
            val viewModel =
                SettingsViewModel(
                    settingsRepository = FakeSettingsRepository(),
                    importExportRepository =
                        FakeImportExportRepository(
                            exportFailure = IllegalStateException("export failed"),
                        ),
                )
            advanceUntilIdle()

            val rawJson = viewModel.exportJson()

            val state = viewModel.uiState.value
            Assert.assertNull(rawJson)
            Assert.assertEquals(R.string.error_export_failed, state.error?.messageResId)
            Assert.assertFalse(state.isProcessingTransfer)
        }

    @Test
    fun exportJson_cancellationClearsBusyState() =
        runTest {
            val viewModel =
                SettingsViewModel(
                    settingsRepository = FakeSettingsRepository(),
                    importExportRepository =
                        FakeImportExportRepository(
                            exportFailure = kotlinx.coroutines.CancellationException("cancelled"),
                        ),
                )
            advanceUntilIdle()

            try {
                viewModel.exportJson()
                Assert.fail("Expected exportJson to throw CancellationException")
            } catch (_: kotlinx.coroutines.CancellationException) {
                // Expected.
            }

            val state = viewModel.uiState.value
            Assert.assertFalse(state.isProcessingTransfer)
            Assert.assertNull(state.error)
        }

    @Test
    fun importJson_successSetsSuccessMessage() =
        runTest {
            val repository = FakeImportExportRepository()
            val viewModel =
                SettingsViewModel(
                    settingsRepository = FakeSettingsRepository(),
                    importExportRepository = repository,
                )
            advanceUntilIdle()

            viewModel.importJson("""{"schemaVersion":1}""")

            val state = viewModel.uiState.value
            Assert.assertEquals("""{"schemaVersion":1}""", repository.importedJson)
            Assert.assertEquals(R.string.message_import_success, state.messageResId)
            Assert.assertNull(state.error)
            Assert.assertFalse(state.isProcessingTransfer)
        }

    @Test
    fun importJson_unsupportedVersionMapsToSpecificError() =
        runTest {
            val viewModel =
                SettingsViewModel(
                    settingsRepository = FakeSettingsRepository(),
                    importExportRepository =
                        FakeImportExportRepository(
                            importFailure = UnsupportedSchemaVersionException(version = 99),
                        ),
                )
            advanceUntilIdle()

            viewModel.importJson("""{"schemaVersion":99}""")

            val state = viewModel.uiState.value
            Assert.assertEquals(R.string.error_import_unsupported_version, state.error?.messageResId)
            Assert.assertFalse(state.isProcessingTransfer)
        }

    @Test
    fun importJson_invalidJsonMapsToSpecificError() =
        runTest {
            val viewModel =
                SettingsViewModel(
                    settingsRepository = FakeSettingsRepository(),
                    importExportRepository =
                        FakeImportExportRepository(
                            importFailure = SerializationException("invalid json"),
                        ),
                )
            advanceUntilIdle()

            viewModel.importJson("{not-json")

            val state = viewModel.uiState.value
            Assert.assertEquals(R.string.error_import_invalid_json, state.error?.messageResId)
            Assert.assertFalse(state.isProcessingTransfer)
        }

    @Test
    fun importJson_invalidPayloadMapsToSpecificError() =
        runTest {
            val viewModel =
                SettingsViewModel(
                    settingsRepository = FakeSettingsRepository(),
                    importExportRepository =
                        FakeImportExportRepository(
                            importFailure = InvalidBackupReferenceException(sakeId = 1L),
                        ),
                )
            advanceUntilIdle()

            viewModel.importJson("""{"schemaVersion":1}""")

            val state = viewModel.uiState.value
            Assert.assertEquals(R.string.error_import_invalid_payload, state.error?.messageResId)
            Assert.assertFalse(state.isProcessingTransfer)
        }

    @Test
    fun importJson_cancellationClearsBusyState() =
        runTest {
            val viewModel =
                SettingsViewModel(
                    settingsRepository = FakeSettingsRepository(),
                    importExportRepository =
                        FakeImportExportRepository(
                            importFailure = kotlinx.coroutines.CancellationException("cancelled"),
                        ),
                )
            advanceUntilIdle()

            try {
                viewModel.importJson("""{"schemaVersion":1}""")
                Assert.fail("Expected importJson to throw CancellationException")
            } catch (_: kotlinx.coroutines.CancellationException) {
                // Expected.
            }

            val state = viewModel.uiState.value
            Assert.assertFalse(state.isProcessingTransfer)
            Assert.assertNull(state.error)
        }

    @Test
    fun onImportFailed_setsGenericReadError() =
        runTest {
            val viewModel =
                SettingsViewModel(
                    settingsRepository = FakeSettingsRepository(),
                    importExportRepository = FakeImportExportRepository(),
                )
            advanceUntilIdle()

            viewModel.onImportFailed(IllegalStateException("read failed"))

            val state = viewModel.uiState.value
            Assert.assertEquals(R.string.error_import_failed, state.error?.messageResId)
            Assert.assertFalse(state.isProcessingTransfer)
        }

    @Test
    fun clearMessage_removesSuccessFeedback() =
        runTest {
            val viewModel =
                SettingsViewModel(
                    settingsRepository = FakeSettingsRepository(),
                    importExportRepository =
                        FakeImportExportRepository(
                            exportJson = """{"schemaVersion":1}""",
                        ),
                )
            advanceUntilIdle()
            viewModel.exportJson()
            viewModel.completeExport(Result.success(Unit))

            viewModel.clearMessage()

            Assert.assertNull(viewModel.uiState.value.messageResId)
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

private class FakeImportExportRepository(
    private val exportJson: String = """{"schemaVersion":1,"sakes":[],"reviews":[]}""",
    private val exportFailure: Throwable? = null,
    private val importFailure: Throwable? = null,
) : ImportExportRepository {
    var importedJson: String? = null

    override suspend fun exportJson(): Result<String> =
        when (val failure = exportFailure) {
            is kotlinx.coroutines.CancellationException -> throw failure
            null -> Result.success(exportJson)
            else -> Result.failure(failure)
        }

    override suspend fun importJson(rawJson: String): Result<Unit> {
        importedJson = rawJson
        return when (val failure = importFailure) {
            is kotlinx.coroutines.CancellationException -> throw failure
            null -> Result.success(Unit)
            else -> Result.failure(failure)
        }
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
