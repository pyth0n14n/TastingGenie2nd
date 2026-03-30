package io.github.pyth0n14n.tastinggenie.feature.sake.edit

import androidx.lifecycle.SavedStateHandle
import io.github.pyth0n14n.tastinggenie.domain.model.Sake
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeGrade
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
class SakeEditViewModelImageCleanupTest {
    companion object {
        private const val EXISTING_SAKE_ID = 7L
        private const val EXISTING_IMAGE_URI = "file:///images/sakes/existing.jpg"
        private const val IMPORTED_IMAGE_URI = "file:///images/sakes/imported.jpg"
        private const val PICKED_IMAGE_URI = "content://picked/image/1"
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun save_withReplacementCleanupFailure_stillMarksSaved() =
        runTest {
            val repository =
                RecordingSakeRepository(
                    initial =
                        listOf(
                            Sake(
                                id = EXISTING_SAKE_ID,
                                name = "既存銘柄",
                                grade = SakeGrade.JUNMAI,
                                imageUri = EXISTING_IMAGE_URI,
                            ),
                        ),
                )
            val imageRepository =
                RecordingSakeImageRepository(
                    importedUri = IMPORTED_IMAGE_URI,
                    deleteFailures = setOf(EXISTING_IMAGE_URI),
                )
            val viewModel =
                SakeEditViewModel(
                    savedStateHandle = SavedStateHandle(mapOf(AppDestination.ARG_SAKE_ID to EXISTING_SAKE_ID)),
                    sakeRepository = repository,
                    sakeImageRepository = imageRepository,
                    masterDataRepository = FakeMasterDataRepository(),
                )
            advanceUntilIdle()

            viewModel.onImageSelected(PICKED_IMAGE_URI)
            viewModel.save()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            val saved = repository.savedInputs.single()
            assertTrue(state.isSaved)
            assertEquals(null, state.error)
            assertEquals(IMPORTED_IMAGE_URI, saved.imageUri)
            assertEquals(listOf(PICKED_IMAGE_URI), imageRepository.importedSources)
            assertEquals(listOf(EXISTING_IMAGE_URI), imageRepository.deletedUris)
        }

    @Test
    fun save_withDeleteCleanupFailure_stillMarksSaved() =
        runTest {
            val repository =
                RecordingSakeRepository(
                    initial =
                        listOf(
                            Sake(
                                id = EXISTING_SAKE_ID,
                                name = "既存銘柄",
                                grade = SakeGrade.JUNMAI,
                                imageUri = EXISTING_IMAGE_URI,
                            ),
                        ),
                )
            val imageRepository =
                RecordingSakeImageRepository(
                    deleteFailures = setOf(EXISTING_IMAGE_URI),
                )
            val viewModel =
                SakeEditViewModel(
                    savedStateHandle = SavedStateHandle(mapOf(AppDestination.ARG_SAKE_ID to EXISTING_SAKE_ID)),
                    sakeRepository = repository,
                    sakeImageRepository = imageRepository,
                    masterDataRepository = FakeMasterDataRepository(),
                )
            advanceUntilIdle()

            viewModel.removeImage()
            viewModel.save()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            val saved = repository.savedInputs.single()
            assertTrue(state.isSaved)
            assertEquals(null, state.error)
            assertEquals(null, saved.imageUri)
            assertEquals(listOf(EXISTING_IMAGE_URI), imageRepository.deletedUris)
        }
}
