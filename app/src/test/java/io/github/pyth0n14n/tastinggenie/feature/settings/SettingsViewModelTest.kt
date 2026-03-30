package io.github.pyth0n14n.tastinggenie.feature.settings

import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.AppSettings
import io.github.pyth0n14n.tastinggenie.domain.model.InvalidBackupArchiveException
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
    fun exportBackup_successPublishesSuccessMessage() =
        runTest {
            var writtenZip: ByteArray? = null
            val viewModel =
                SettingsViewModel(
                    settingsRepository = FakeSettingsRepository(),
                    importExportRepository =
                        FakeImportExportRepository(
                            exportZip = SAMPLE_BACKUP_ZIP,
                        ),
                )
            advanceUntilIdle()

            viewModel.exportBackup { rawZip ->
                writtenZip = rawZip
                Result.success(Unit)
            }
            advanceUntilIdle()

            val state = viewModel.uiState.value
            Assert.assertArrayEquals(SAMPLE_BACKUP_ZIP, writtenZip)
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

            viewModel.exportBackup { Result.success(Unit) }
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

            viewModel.exportBackup { Result.success(Unit) }
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

            viewModel.importBackup { Result.success(SAMPLE_BACKUP_ZIP) }
            advanceUntilIdle()

            val state = viewModel.uiState.value
            Assert.assertArrayEquals(SAMPLE_BACKUP_ZIP, repository.importedZip)
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

            viewModel.importBackup { Result.success(SAMPLE_BACKUP_ZIP) }
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

            viewModel.importBackup { Result.success(SAMPLE_BACKUP_ZIP) }
            advanceUntilIdle()

            val state = viewModel.uiState.value
            Assert.assertEquals(R.string.error_import_invalid_json, state.error?.messageResId)
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

            viewModel.importBackup { Result.success(SAMPLE_BACKUP_ZIP) }
            advanceUntilIdle()

            val state = viewModel.uiState.value
            Assert.assertEquals(R.string.error_import_invalid_payload, state.error?.messageResId)
            Assert.assertFalse(state.isProcessingTransfer)
        }

    @Test
    fun importBackup_invalidArchiveMapsToSpecificError() =
        runTest {
            val viewModel =
                SettingsViewModel(
                    settingsRepository = FakeSettingsRepository(),
                    importExportRepository =
                        FakeImportExportRepository(
                            importFailure = InvalidBackupArchiveException("broken zip"),
                        ),
                )
            advanceUntilIdle()

            viewModel.importBackup { Result.success(SAMPLE_BACKUP_ZIP) }
            advanceUntilIdle()

            val state = viewModel.uiState.value
            Assert.assertEquals(R.string.error_import_invalid_json, state.error?.messageResId)
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

            viewModel.importBackup { Result.success(SAMPLE_BACKUP_ZIP) }
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
                            exportZip = SAMPLE_BACKUP_ZIP,
                        ),
                )
            advanceUntilIdle()
            viewModel.exportBackup { Result.success(Unit) }
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
            viewModel.exportBackup { Result.success(Unit) }
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
                            exportZip = SAMPLE_BACKUP_ZIP,
                        ),
                )
            advanceUntilIdle()
            viewModel.setSettingsVisible(visible = true)

            viewModel.exportBackup { Result.success(Unit) }
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

            viewModel.importBackup { Result.success(SAMPLE_BACKUP_ZIP) }
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

    override suspend fun updateShowHelpHints(enabled: Boolean) {
        stream.value = stream.value.copy(showHelpHints = enabled)
    }

    override suspend fun updateShowImagePreview(enabled: Boolean) {
        stream.value = stream.value.copy(showImagePreview = enabled)
    }
}

private class FakeImportExportRepository(
    private val exportZip: ByteArray = SAMPLE_BACKUP_ZIP,
    private val exportFailure: Throwable? = null,
    private val importFailure: Throwable? = null,
) : ImportExportRepository {
    var importedZip: ByteArray? = null

    override suspend fun exportBackup(): Result<ByteArray> =
        when (val failure = exportFailure) {
            is kotlinx.coroutines.CancellationException -> throw failure
            null -> Result.success(exportZip)
            else -> Result.failure(failure)
        }

    override suspend fun importBackup(rawZip: ByteArray): Result<Unit> {
        importedZip = rawZip
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

private const val ZIP_HEADER_P: Byte = 0x50
private const val ZIP_HEADER_K: Byte = 0x4B
private const val ZIP_HEADER_LOCAL_FILE: Byte = 0x03
private const val ZIP_HEADER_VERSION: Byte = 0x04

private val SAMPLE_BACKUP_ZIP =
    byteArrayOf(
        ZIP_HEADER_P,
        ZIP_HEADER_K,
        ZIP_HEADER_LOCAL_FILE,
        ZIP_HEADER_VERSION,
    )
