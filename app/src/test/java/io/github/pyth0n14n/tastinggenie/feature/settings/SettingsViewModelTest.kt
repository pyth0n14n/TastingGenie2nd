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
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

private val TEST_EXPORT_BYTES = "export-backup".toByteArray()
private val TEST_IMPORT_BYTES = "restore-backup".toByteArray()
private val TEST_INVALID_BYTES = "invalid-backup".toByteArray()

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
            Assert.assertEquals(false, state.settings.showReviewSoundness)
        }

    @Test
    fun toggleReviewSoundness_updatesState() =
        runTest {
            val repository = FakeSettingsRepository()
            val viewModel =
                SettingsViewModel(
                    settingsRepository = repository,
                    importExportRepository = FakeImportExportRepository(),
                )
            advanceUntilIdle()

            viewModel.toggleReviewSoundness(enabled = true)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            Assert.assertEquals(true, state.settings.showReviewSoundness)
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
    fun exportBackup_successPublishesSuccessMessage() =
        runTest {
            val writtenBackup = ByteArrayOutputStream()
            val viewModel =
                SettingsViewModel(
                    settingsRepository = FakeSettingsRepository(),
                    importExportRepository =
                        FakeImportExportRepository(
                            exportBytes = TEST_EXPORT_BYTES,
                        ),
                )
            advanceUntilIdle()

            viewModel.exportBackup { Result.success(writtenBackup) }
            advanceUntilIdle()

            val state = viewModel.uiState.value
            Assert.assertArrayEquals(TEST_EXPORT_BYTES, writtenBackup.toByteArray())
            Assert.assertEquals(R.string.message_export_success, state.messageResId)
            Assert.assertFalse(state.isProcessingTransfer)
            Assert.assertNull(state.error)
        }

    @Test
    fun exportBackup_repositoryFailureSetsError() =
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

            viewModel.exportBackup { Result.success(ByteArrayOutputStream()) }
            advanceUntilIdle()

            val state = viewModel.uiState.value
            Assert.assertEquals(R.string.error_export_failed, state.error?.messageResId)
            Assert.assertFalse(state.isProcessingTransfer)
        }

    @Test
    fun exportBackup_cancellationClearsBusyState() =
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

            viewModel.exportBackup { Result.success(ByteArrayOutputStream()) }
            advanceUntilIdle()

            val state = viewModel.uiState.value
            Assert.assertFalse(state.isProcessingTransfer)
            Assert.assertNull(state.error)
        }

    @Test
    fun importBackup_successSetsSuccessMessage() =
        runTest {
            val repository = FakeImportExportRepository()
            val viewModel =
                SettingsViewModel(
                    settingsRepository = FakeSettingsRepository(),
                    importExportRepository = repository,
                )
            advanceUntilIdle()

            viewModel.importBackup { Result.success(ByteArrayInputStream(TEST_IMPORT_BYTES)) }
            advanceUntilIdle()

            val state = viewModel.uiState.value
            Assert.assertArrayEquals(TEST_IMPORT_BYTES, repository.importedBytes)
            Assert.assertEquals(R.string.message_import_success, state.messageResId)
            Assert.assertNull(state.error)
            Assert.assertFalse(state.isProcessingTransfer)
        }

    @Test
    fun importBackup_unsupportedVersionMapsToSpecificError() =
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

            viewModel.importBackup { Result.success(ByteArrayInputStream(TEST_INVALID_BYTES)) }
            advanceUntilIdle()

            val state = viewModel.uiState.value
            Assert.assertEquals(R.string.error_import_unsupported_version, state.error?.messageResId)
            Assert.assertFalse(state.isProcessingTransfer)
        }

    @Test
    fun importBackup_invalidJsonMapsToSpecificError() =
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

            viewModel.importBackup { Result.success(ByteArrayInputStream(TEST_INVALID_BYTES)) }
            advanceUntilIdle()

            val state = viewModel.uiState.value
            Assert.assertEquals(R.string.error_import_invalid_backup, state.error?.messageResId)
            Assert.assertFalse(state.isProcessingTransfer)
        }

    @Test
    fun importBackup_invalidPayloadMapsToSpecificError() =
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

            viewModel.importBackup { Result.success(ByteArrayInputStream(TEST_INVALID_BYTES)) }
            advanceUntilIdle()

            val state = viewModel.uiState.value
            Assert.assertEquals(R.string.error_import_invalid_payload, state.error?.messageResId)
            Assert.assertFalse(state.isProcessingTransfer)
        }

    @Test
    fun importBackup_cancellationClearsBusyState() =
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

            viewModel.importBackup { Result.success(ByteArrayInputStream(TEST_INVALID_BYTES)) }
            advanceUntilIdle()

            val state = viewModel.uiState.value
            Assert.assertFalse(state.isProcessingTransfer)
            Assert.assertNull(state.error)
        }

    @Test
    fun importBackup_readFailureSetsGenericError() =
        runTest {
            val viewModel =
                SettingsViewModel(
                    settingsRepository = FakeSettingsRepository(),
                    importExportRepository = FakeImportExportRepository(),
                )
            advanceUntilIdle()

            viewModel.importBackup { Result.failure(IllegalStateException("read failed")) }
            advanceUntilIdle()

            val state = viewModel.uiState.value
            Assert.assertEquals(R.string.error_import_failed, state.error?.messageResId)
            Assert.assertFalse(state.isProcessingTransfer)
        }

    @Test
    fun clearTransferFeedback_removesSuccessFeedback() =
        runTest {
            val viewModel =
                SettingsViewModel(
                    settingsRepository = FakeSettingsRepository(),
                    importExportRepository =
                        FakeImportExportRepository(
                            exportBytes = TEST_EXPORT_BYTES,
                        ),
                )
            advanceUntilIdle()
            viewModel.exportBackup { Result.success(ByteArrayOutputStream()) }
            advanceUntilIdle()

            viewModel.clearTransferFeedback()

            Assert.assertNull(viewModel.uiState.value.messageResId)
        }

    @Test
    fun clearTransferFeedback_removesTransferErrorAndMessage() =
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
            viewModel.exportBackup { Result.success(ByteArrayOutputStream()) }
            advanceUntilIdle()

            viewModel.clearTransferFeedback()

            val state = viewModel.uiState.value
            Assert.assertNull(state.messageResId)
            Assert.assertNull(state.error)
        }

    @Test
    fun clearTransferFeedback_preservesNonTransferError() =
        runTest {
            val viewModel =
                SettingsViewModel(
                    settingsRepository = FailingUpdateSettingsRepository(),
                    importExportRepository = FakeImportExportRepository(),
                )
            advanceUntilIdle()
            viewModel.toggleHelpHints(enabled = false)
            advanceUntilIdle()

            viewModel.clearTransferFeedback()

            val state = viewModel.uiState.value
            Assert.assertEquals(R.string.error_save_settings, state.error?.messageResId)
        }

    @Test
    fun setSettingsVisible_reopenClearsTransferFeedbackSeenOnScreen() =
        runTest {
            val viewModel =
                SettingsViewModel(
                    settingsRepository = FakeSettingsRepository(),
                    importExportRepository =
                        FakeImportExportRepository(
                            exportBytes = TEST_EXPORT_BYTES,
                        ),
                )
            advanceUntilIdle()
            viewModel.setSettingsVisible(visible = true)

            viewModel.exportBackup { Result.success(ByteArrayOutputStream()) }
            advanceUntilIdle()
            Assert.assertEquals(R.string.message_export_success, viewModel.uiState.value.messageResId)

            viewModel.setSettingsVisible(visible = false)
            viewModel.setSettingsVisible(visible = true)

            val state = viewModel.uiState.value
            Assert.assertNull(state.messageResId)
            Assert.assertNull(state.error)
        }

    @Test
    fun setSettingsVisible_reopenKeepsTransferFeedbackCompletedOffScreenUntilSeen() =
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
            viewModel.setSettingsVisible(visible = false)

            viewModel.importBackup { Result.success(ByteArrayInputStream(TEST_INVALID_BYTES)) }
            advanceUntilIdle()
            val initialState = viewModel.uiState.value
            val initialError = initialState.error?.messageResId
            Assert.assertEquals(R.string.error_import_invalid_payload, initialError)

            viewModel.setSettingsVisible(visible = true)
            val reopenedState = viewModel.uiState.value
            val reopenedError = reopenedState.error?.messageResId
            Assert.assertEquals(R.string.error_import_invalid_payload, reopenedError)

            viewModel.setSettingsVisible(visible = false)
            viewModel.setSettingsVisible(visible = true)

            val state = viewModel.uiState.value
            Assert.assertNull(state.messageResId)
            Assert.assertNull(state.error)
        }
}

private class FakeSettingsRepository : SettingsRepository {
    private val stream = MutableStateFlow(AppSettings())

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

    override suspend fun replaceSettings(settings: AppSettings) {
        stream.value = settings
    }
}

private class FakeImportExportRepository(
    private val exportBytes: ByteArray = byteArrayOf(),
    private val exportFailure: Throwable? = null,
    private val importFailure: Throwable? = null,
) : ImportExportRepository {
    var importedBytes: ByteArray? = null

    override suspend fun exportBackup(output: java.io.OutputStream): Result<Unit> =
        when (val failure = exportFailure) {
            is kotlinx.coroutines.CancellationException -> throw failure
            null -> {
                output.write(exportBytes)
                Result.success(Unit)
            }
            else -> Result.failure(failure)
        }

    override suspend fun restoreBackup(input: java.io.InputStream): Result<Unit> {
        importedBytes = input.readBytes()
        return when (val failure = importFailure) {
            is kotlinx.coroutines.CancellationException -> throw failure
            null -> Result.success(Unit)
            else -> Result.failure(failure)
        }
    }
}

private class FailingUpdateSettingsRepository : SettingsRepository {
    override fun observeSettings(): Flow<AppSettings> = flow { emit(AppSettings()) }

    override suspend fun getCurrentSettings(): AppSettings = AppSettings()

    override suspend fun updateShowHelpHints(enabled: Boolean) {
        error("settings write failed")
    }

    override suspend fun updateShowReviewSoundness(enabled: Boolean) {
        error("settings write failed")
    }

    override suspend fun updateReviewMode(modeId: String) {
        error("settings write failed")
    }

    override suspend fun replaceSettings(settings: AppSettings) {
        error("settings write failed")
    }
}
