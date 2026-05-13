package io.github.pyth0n14n.tastinggenie.feature.sake.edit

import androidx.lifecycle.SavedStateHandle
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.Sake
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeGrade
import io.github.pyth0n14n.tastinggenie.domain.repository.SakeImageImportException
import io.github.pyth0n14n.tastinggenie.navigation.AppDestination
import io.github.pyth0n14n.tastinggenie.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SakeEditViewModelImageImportTest {
    companion object {
        private const val EXISTING_SAKE_ID = 7L
        private const val TEST_IMAGE_MAX_BYTES = 10L
        private const val PICKED_IMAGE_URI = "content://picked/image/1"
        private const val EXISTING_IMAGE_URI = "file:///images/sakes/existing.jpg"
        private const val IMPORTED_IMAGE_URI = "file:///images/sakes/imported.jpg"
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun save_withSelectedImage_importsManagedImageAndPersistsUri() =
        runTest {
            val repository = RecordingSakeRepository()
            val imageRepository = RecordingSakeImageRepository(importedUri = IMPORTED_IMAGE_URI)
            val viewModel =
                SakeEditViewModel(
                    savedStateHandle = SavedStateHandle(),
                    sakeRepository = repository,
                    sakeImageRepository = imageRepository,
                    masterDataRepository = FakeMasterDataRepository(),
                )
            advanceUntilIdle()

            viewModel.onTextChanged(SakeTextField.NAME, "保存テスト")
            viewModel.onGradeSelected(SakeGrade.JUNMAI.name)
            viewModel.onImageSelected(PICKED_IMAGE_URI)
            viewModel.save()
            advanceUntilIdle()

            val saved = repository.savedInputs.single()
            assertEquals(listOf(IMPORTED_IMAGE_URI), saved.imageUris)
            assertEquals(listOf(PICKED_IMAGE_URI), imageRepository.importedSources)
            assertTrue(imageRepository.deletedUris.isEmpty())
        }

    @Test
    fun save_withUnsupportedImageMimeType_setsImageFormatError() =
        runTest {
            val imageRepository =
                RecordingSakeImageRepository(
                    importFailure = SakeImageImportException.UnsupportedMimeType("text/plain"),
                )
            val viewModel =
                SakeEditViewModel(
                    savedStateHandle = SavedStateHandle(),
                    sakeRepository = RecordingSakeRepository(),
                    sakeImageRepository = imageRepository,
                    masterDataRepository = FakeMasterDataRepository(),
                )
            advanceUntilIdle()

            viewModel.onTextChanged(SakeTextField.NAME, "保存テスト")
            viewModel.onGradeSelected(SakeGrade.JUNMAI.name)
            viewModel.onImageSelected(PICKED_IMAGE_URI)
            viewModel.save()
            advanceUntilIdle()

            assertEquals(
                R.string.error_sake_image_unsupported_type,
                viewModel.uiState.value.error
                    ?.messageResId,
            )
        }

    @Test
    fun save_withTooLargeImage_setsImageSizeError() =
        runTest {
            val imageRepository =
                RecordingSakeImageRepository(
                    importFailure = SakeImageImportException.ImageTooLarge(TEST_IMAGE_MAX_BYTES),
                )
            val viewModel =
                SakeEditViewModel(
                    savedStateHandle = SavedStateHandle(),
                    sakeRepository = RecordingSakeRepository(),
                    sakeImageRepository = imageRepository,
                    masterDataRepository = FakeMasterDataRepository(),
                )
            advanceUntilIdle()

            viewModel.onTextChanged(SakeTextField.NAME, "保存テスト")
            viewModel.onGradeSelected(SakeGrade.JUNMAI.name)
            viewModel.onImageSelected(PICKED_IMAGE_URI)
            viewModel.save()
            advanceUntilIdle()

            assertEquals(
                R.string.error_sake_image_too_large,
                viewModel.uiState.value.error
                    ?.messageResId,
            )
        }

    @Test
    fun onImageSelected_withDuplicateUri_keepsSinglePreviewAndSingleImport() =
        runTest {
            val repository = RecordingSakeRepository()
            val imageRepository = RecordingSakeImageRepository(importedUri = IMPORTED_IMAGE_URI)
            val viewModel =
                SakeEditViewModel(
                    savedStateHandle = SavedStateHandle(),
                    sakeRepository = repository,
                    sakeImageRepository = imageRepository,
                    masterDataRepository = FakeMasterDataRepository(),
                )
            advanceUntilIdle()

            viewModel.onTextChanged(SakeTextField.NAME, "保存テスト")
            viewModel.onGradeSelected(SakeGrade.JUNMAI.name)
            viewModel.onImageSelected(PICKED_IMAGE_URI)
            viewModel.onImageSelected(PICKED_IMAGE_URI)
            viewModel.save()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            val saved = repository.savedInputs.single()
            assertEquals(listOf(PICKED_IMAGE_URI), state.imagePreviewUris)
            assertEquals(listOf(PICKED_IMAGE_URI), imageRepository.importedSources)
            assertEquals(listOf(IMPORTED_IMAGE_URI), saved.imageUris)
        }

    @Test
    fun save_withImageDeletion_deletesPersistedImageAfterSave() =
        runTest {
            val repository =
                RecordingSakeRepository(
                    initial =
                        listOf(
                            Sake(
                                id = EXISTING_SAKE_ID,
                                name = "既存銘柄",
                                grade = SakeGrade.JUNMAI,
                                imageUris = listOf(EXISTING_IMAGE_URI),
                            ),
                        ),
                )
            val imageRepository = RecordingSakeImageRepository(importedUri = IMPORTED_IMAGE_URI)
            val viewModel =
                SakeEditViewModel(
                    savedStateHandle = SavedStateHandle(mapOf(AppDestination.ARG_SAKE_ID to EXISTING_SAKE_ID)),
                    sakeRepository = repository,
                    sakeImageRepository = imageRepository,
                    masterDataRepository = FakeMasterDataRepository(),
                )
            advanceUntilIdle()

            viewModel.removeImage(EXISTING_IMAGE_URI)
            viewModel.save()
            advanceUntilIdle()

            val saved = repository.savedInputs.single()
            assertEquals(emptyList<String>(), saved.imageUris)
            assertTrue(imageRepository.deletedUris.isEmpty())
        }

    @Test
    fun save_whenSakeSaveFails_cleansUpImportedImage() =
        runTest {
            val repository = RecordingSakeRepository(upsertFailure = IllegalStateException("boom"))
            val imageRepository = RecordingSakeImageRepository(importedUri = IMPORTED_IMAGE_URI)
            val viewModel =
                SakeEditViewModel(
                    savedStateHandle = SavedStateHandle(),
                    sakeRepository = repository,
                    sakeImageRepository = imageRepository,
                    masterDataRepository = FakeMasterDataRepository(),
                )
            advanceUntilIdle()

            viewModel.onTextChanged(SakeTextField.NAME, "保存テスト")
            viewModel.onGradeSelected(SakeGrade.JUNMAI.name)
            viewModel.onImageSelected(PICKED_IMAGE_URI)
            viewModel.save()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(R.string.error_save_sake, state.error?.messageResId)
            assertEquals(listOf(PICKED_IMAGE_URI), imageRepository.importedSources)
            assertEquals(listOf(IMPORTED_IMAGE_URI), imageRepository.deletedUris)
        }
}
