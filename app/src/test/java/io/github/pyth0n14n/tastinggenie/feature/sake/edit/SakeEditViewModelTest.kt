package io.github.pyth0n14n.tastinggenie.feature.sake.edit

import androidx.lifecycle.SavedStateHandle
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.MasterDataBundle
import io.github.pyth0n14n.tastinggenie.domain.model.MasterOption
import io.github.pyth0n14n.tastinggenie.domain.model.Sake
import io.github.pyth0n14n.tastinggenie.domain.model.SakeDeleteResult
import io.github.pyth0n14n.tastinggenie.domain.model.SakeId
import io.github.pyth0n14n.tastinggenie.domain.model.SakeInput
import io.github.pyth0n14n.tastinggenie.domain.model.SakeListSummary
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Prefecture
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeClassification
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeGrade
import io.github.pyth0n14n.tastinggenie.domain.repository.MasterDataRepository
import io.github.pyth0n14n.tastinggenie.domain.repository.SakeImageRepository
import io.github.pyth0n14n.tastinggenie.domain.repository.SakeRepository
import io.github.pyth0n14n.tastinggenie.navigation.AppDestination
import io.github.pyth0n14n.tastinggenie.testutil.MainDispatcherRule
import io.github.pyth0n14n.tastinggenie.ui.common.FieldValidationError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
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
        private const val GRADE_OPTION_COUNT = 4
        private const val EXTENDED_OPTION_COUNT = 3
        private const val TEST_SAKE_DEGREE = 3.5F
        private const val TEST_ACIDITY = 1.4F
        private const val TEST_KOJI_POLISH = 50
        private const val TEST_KAKE_POLISH = 55
        private const val TEST_ALCOHOL = 16
        private const val PICKED_IMAGE_URI = "content://picked/image/1"
        private const val EXISTING_IMAGE_URI = "file:///images/sakes/existing.jpg"
        private const val IMPORTED_IMAGE_URI = "file:///images/sakes/imported.jpg"
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
                    sakeImageRepository = RecordingSakeImageRepository(),
                    masterDataRepository = masterRepository,
                )
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals(GRADE_OPTION_COUNT, state.gradeOptions.size)
            assertEquals(EXTENDED_OPTION_COUNT, state.classificationOptions.size)
            assertEquals(EXTENDED_OPTION_COUNT, state.prefectureOptions.size)
            assertEquals(null, state.sakeId)
        }

    @Test
    fun save_withInvalidInput_setsValidationError() =
        runTest {
            val viewModel =
                SakeEditViewModel(
                    savedStateHandle = SavedStateHandle(),
                    sakeRepository = RecordingSakeRepository(),
                    sakeImageRepository = RecordingSakeImageRepository(),
                    masterDataRepository = FakeMasterDataRepository(),
                )
            advanceUntilIdle()
            viewModel.onTextChanged(SakeTextField.NAME, "")
            viewModel.save()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(null, state.error)
            assertEquals(FieldValidationError.REQUIRED, state.validationErrors[SakeValidationField.NAME])
            assertEquals(
                FieldValidationError.REQUIRED_SELECTION,
                state.validationErrors[SakeValidationField.GRADE],
            )
        }

    @Test
    fun save_withValidInput_callsUpsertAndMarksSaved() =
        runTest {
            val repository = RecordingSakeRepository()
            val viewModel =
                SakeEditViewModel(
                    savedStateHandle = SavedStateHandle(),
                    sakeRepository = repository,
                    sakeImageRepository = RecordingSakeImageRepository(),
                    masterDataRepository = FakeMasterDataRepository(),
                )
            advanceUntilIdle()
            viewModel.onTextChanged(SakeTextField.NAME, "保存テスト")
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
    fun save_withExtendedInput_persistsClassificationMakerAndPrefecture() =
        runTest {
            val repository = RecordingSakeRepository()
            val viewModel =
                SakeEditViewModel(
                    savedStateHandle = SavedStateHandle(),
                    sakeRepository = repository,
                    sakeImageRepository = RecordingSakeImageRepository(),
                    masterDataRepository = FakeMasterDataRepository(),
                )
            advanceUntilIdle()

            viewModel.onTextChanged(SakeTextField.NAME, "保存テスト")
            viewModel.onGradeSelected(SakeGrade.JUNMAI.name)
            viewModel.onClassificationToggled(SakeClassification.KIMOTO.name)
            viewModel.onClassificationToggled(SakeClassification.OTHER.name)
            viewModel.onTextChanged(SakeTextField.TYPE_OTHER, "限定品")
            viewModel.onTextChanged(SakeTextField.MAKER, "蔵元A")
            viewModel.onPrefectureSelected(Prefecture.NAGANO.name)
            viewModel.save()
            advanceUntilIdle()

            val saved = repository.savedInputs.single()
            assertEquals(listOf(SakeClassification.KIMOTO, SakeClassification.OTHER), saved.type)
            assertEquals("限定品", saved.typeOther)
            assertEquals("蔵元A", saved.maker)
            assertEquals(Prefecture.NAGANO, saved.prefecture)
        }

    @Test
    fun save_withPr4Fields_persistsNumericAndTextMetadata() =
        runTest {
            val repository = RecordingSakeRepository()
            val viewModel =
                SakeEditViewModel(
                    savedStateHandle = SavedStateHandle(),
                    sakeRepository = repository,
                    sakeImageRepository = RecordingSakeImageRepository(),
                    masterDataRepository = FakeMasterDataRepository(),
                )
            advanceUntilIdle()

            viewModel.onTextChanged(SakeTextField.NAME, "保存テスト")
            viewModel.onGradeSelected(SakeGrade.JUNMAI.name)
            viewModel.onTextChanged(SakeTextField.SAKE_DEGREE, "+3.5")
            viewModel.onTextChanged(SakeTextField.ACIDITY, "1.4")
            viewModel.onTextChanged(SakeTextField.KOJI_MAI, "山田錦")
            viewModel.onTextChanged(SakeTextField.KOJI_POLISH, "50")
            viewModel.onTextChanged(SakeTextField.KAKE_MAI, "五百万石")
            viewModel.onTextChanged(SakeTextField.KAKE_POLISH, "55")
            viewModel.onTextChanged(SakeTextField.ALCOHOL, "16")
            viewModel.onTextChanged(SakeTextField.YEAST, "協会9号")
            viewModel.onTextChanged(SakeTextField.WATER, "伏流水")
            viewModel.save()
            advanceUntilIdle()

            val saved = repository.savedInputs.single()
            assertEquals(TEST_SAKE_DEGREE, saved.sakeDegree)
            assertEquals(TEST_ACIDITY, saved.acidity)
            assertEquals("山田錦", saved.kojiMai)
            assertEquals(TEST_KOJI_POLISH, saved.kojiPolish)
            assertEquals("五百万石", saved.kakeMai)
            assertEquals(TEST_KAKE_POLISH, saved.kakePolish)
            assertEquals(TEST_ALCOHOL, saved.alcohol)
            assertEquals("協会9号", saved.yeast)
            assertEquals("伏流水", saved.water)
        }

    @Test
    fun save_withGradeOther_persistsFreeText() =
        runTest {
            val repository = RecordingSakeRepository()
            val viewModel =
                SakeEditViewModel(
                    savedStateHandle = SavedStateHandle(),
                    sakeRepository = repository,
                    sakeImageRepository = RecordingSakeImageRepository(),
                    masterDataRepository = FakeMasterDataRepository(),
                )
            advanceUntilIdle()

            viewModel.onTextChanged(SakeTextField.NAME, "保存テスト")
            viewModel.onGradeSelected(SakeGrade.OTHER.name)
            viewModel.onTextChanged(SakeTextField.GRADE_OTHER, "普通酒")
            viewModel.save()
            advanceUntilIdle()

            val saved = repository.savedInputs.single()
            assertEquals(SakeGrade.OTHER, saved.grade)
            assertEquals("普通酒", saved.gradeOther)
        }

    @Test
    fun save_withFutsushu_persistsNamedGradeWithoutFreeText() =
        runTest {
            val repository = RecordingSakeRepository()
            val viewModel =
                SakeEditViewModel(
                    savedStateHandle = SavedStateHandle(),
                    sakeRepository = repository,
                    sakeImageRepository = RecordingSakeImageRepository(),
                    masterDataRepository = FakeMasterDataRepository(),
                )
            advanceUntilIdle()

            viewModel.onTextChanged(SakeTextField.NAME, "保存テスト")
            viewModel.onGradeSelected(SakeGrade.FUTSUSHU.name)
            viewModel.save()
            advanceUntilIdle()

            val saved = repository.savedInputs.single()
            assertEquals(SakeGrade.FUTSUSHU, saved.grade)
            assertEquals(null, saved.gradeOther)
        }

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

    @Test
    fun save_withInvalidNumericField_setsValidationError() =
        runTest {
            val repository = RecordingSakeRepository()
            val viewModel =
                SakeEditViewModel(
                    savedStateHandle = SavedStateHandle(),
                    sakeRepository = repository,
                    sakeImageRepository = RecordingSakeImageRepository(),
                    masterDataRepository = FakeMasterDataRepository(),
                )
            advanceUntilIdle()

            viewModel.onTextChanged(SakeTextField.NAME, "保存テスト")
            viewModel.onGradeSelected(SakeGrade.JUNMAI.name)
            viewModel.onTextChanged(SakeTextField.ALCOHOL, "16%")
            viewModel.save()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(FieldValidationError.INVALID_NUMBER, state.validationErrors[SakeValidationField.ALCOHOL])
            assertTrue(repository.savedInputs.isEmpty())
        }

    @Test
    fun save_withOutOfRangePolishRatio_setsValidationError() =
        runTest {
            val repository = RecordingSakeRepository()
            val viewModel =
                SakeEditViewModel(
                    savedStateHandle = SavedStateHandle(),
                    sakeRepository = repository,
                    sakeImageRepository = RecordingSakeImageRepository(),
                    masterDataRepository = FakeMasterDataRepository(),
                )
            advanceUntilIdle()

            viewModel.onTextChanged(SakeTextField.NAME, "保存テスト")
            viewModel.onGradeSelected(SakeGrade.JUNMAI.name)
            viewModel.onTextChanged(SakeTextField.KOJI_POLISH, "101")
            viewModel.save()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(
                FieldValidationError.INVALID_PERCENTAGE,
                state.validationErrors[SakeValidationField.KOJI_POLISH],
            )
            assertTrue(repository.savedInputs.isEmpty())
        }

    @Test
    fun save_withNonFiniteFloatField_setsValidationError() =
        runTest {
            val repository = RecordingSakeRepository()
            val viewModel =
                SakeEditViewModel(
                    savedStateHandle = SavedStateHandle(),
                    sakeRepository = repository,
                    sakeImageRepository = RecordingSakeImageRepository(),
                    masterDataRepository = FakeMasterDataRepository(),
                )
            advanceUntilIdle()

            viewModel.onTextChanged(SakeTextField.NAME, "保存テスト")
            viewModel.onGradeSelected(SakeGrade.JUNMAI.name)
            viewModel.onTextChanged(SakeTextField.SAKE_DEGREE, "NaN")
            viewModel.onTextChanged(SakeTextField.ACIDITY, "1e50")
            viewModel.save()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(
                FieldValidationError.INVALID_NUMBER,
                state.validationErrors[SakeValidationField.SAKE_DEGREE],
            )
            assertEquals(
                FieldValidationError.INVALID_NUMBER,
                state.validationErrors[SakeValidationField.ACIDITY],
            )
            assertTrue(repository.savedInputs.isEmpty())
        }

    @Test
    fun onGradeSelected_withUnexpectedValue_setsUiErrorWithoutCrashing() =
        runTest {
            val viewModel =
                SakeEditViewModel(
                    savedStateHandle = SavedStateHandle(),
                    sakeRepository = RecordingSakeRepository(),
                    sakeImageRepository = RecordingSakeImageRepository(),
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
    fun onClassificationToggled_withUnexpectedValue_setsUiErrorWithoutCrashing() =
        runTest {
            val viewModel =
                SakeEditViewModel(
                    savedStateHandle = SavedStateHandle(),
                    sakeRepository = RecordingSakeRepository(),
                    sakeImageRepository = RecordingSakeImageRepository(),
                    masterDataRepository = FakeMasterDataRepository(),
                )
            advanceUntilIdle()

            viewModel.onClassificationToggled("BROKEN_VALUE")

            val state = viewModel.uiState.value
            assertTrue(state.classifications.isEmpty())
            assertEquals(R.string.error_invalid_sake_selection, state.error?.messageResId)
            assertEquals("BROKEN_VALUE", state.error?.causeKey)
        }

    @Test
    fun changingGradeAwayFromOther_clearsFreeTextWhenClassificationOtherIsAbsent() =
        runTest {
            val viewModel =
                SakeEditViewModel(
                    savedStateHandle = SavedStateHandle(),
                    sakeRepository = RecordingSakeRepository(),
                    sakeImageRepository = RecordingSakeImageRepository(),
                    masterDataRepository = FakeMasterDataRepository(),
                )
            advanceUntilIdle()

            viewModel.onGradeSelected(SakeGrade.OTHER.name)
            viewModel.onTextChanged(SakeTextField.GRADE_OTHER, "普通酒")
            viewModel.onGradeSelected(SakeGrade.JUNMAI.name)

            val state = viewModel.uiState.value
            assertEquals(SakeGrade.JUNMAI, state.grade)
            assertEquals("", state.gradeOther)
        }

    @Test
    fun togglingClassificationOther_doesNotAffectGradeOther() =
        runTest {
            val viewModel =
                SakeEditViewModel(
                    savedStateHandle = SavedStateHandle(),
                    sakeRepository = RecordingSakeRepository(),
                    sakeImageRepository = RecordingSakeImageRepository(),
                    masterDataRepository = FakeMasterDataRepository(),
                )
            advanceUntilIdle()

            viewModel.onGradeSelected(SakeGrade.OTHER.name)
            viewModel.onTextChanged(SakeTextField.GRADE_OTHER, "普通酒")
            viewModel.onClassificationToggled(SakeClassification.OTHER.name)
            viewModel.onTextChanged(SakeTextField.TYPE_OTHER, "普通酒")
            viewModel.onClassificationToggled(SakeClassification.OTHER.name)

            val state = viewModel.uiState.value
            assertEquals(SakeGrade.OTHER, state.grade)
            assertEquals("普通酒", state.gradeOther)
            assertTrue(state.classifications.isEmpty())
            assertEquals("", state.typeOther)
        }

    @Test
    fun deselectingOtherClassification_clearsFreeText() =
        runTest {
            val viewModel =
                SakeEditViewModel(
                    savedStateHandle = SavedStateHandle(),
                    sakeRepository = RecordingSakeRepository(),
                    sakeImageRepository = RecordingSakeImageRepository(),
                    masterDataRepository = FakeMasterDataRepository(),
                )
            advanceUntilIdle()

            viewModel.onClassificationToggled(SakeClassification.OTHER.name)
            viewModel.onTextChanged(SakeTextField.TYPE_OTHER, "限定品")
            viewModel.onClassificationToggled(SakeClassification.OTHER.name)

            val state = viewModel.uiState.value
            assertTrue(state.classifications.isEmpty())
            assertEquals("", state.typeOther)
        }

    @Test
    fun onPrefectureSelected_withUnexpectedValue_setsUiErrorWithoutCrashing() =
        runTest {
            val viewModel =
                SakeEditViewModel(
                    savedStateHandle = SavedStateHandle(),
                    sakeRepository = RecordingSakeRepository(),
                    sakeImageRepository = RecordingSakeImageRepository(),
                    masterDataRepository = FakeMasterDataRepository(),
                )
            advanceUntilIdle()

            viewModel.onPrefectureSelected("BROKEN_VALUE")

            val state = viewModel.uiState.value
            assertEquals(null, state.prefecture)
            assertEquals(R.string.error_invalid_sake_selection, state.error?.messageResId)
            assertEquals("BROKEN_VALUE", state.error?.causeKey)
        }

    @Test
    fun save_afterUnexpectedGradeValue_doesNotPersistStaleGrade() =
        runTest {
            val repository =
                RecordingSakeRepository(
                    initial =
                        listOf(
                            Sake(
                                id = EXISTING_SAKE_ID,
                                name = "既存銘柄",
                                grade = SakeGrade.OTHER,
                                imageUris = listOf(EXISTING_IMAGE_URI),
                                gradeOther = "普通酒",
                                type = listOf(SakeClassification.KIMOTO, SakeClassification.OTHER),
                                typeOther = "限定品",
                                maker = "既存酒造",
                                prefecture = Prefecture.NAGANO,
                                alcohol = TEST_ALCOHOL,
                                kojiMai = "山田錦",
                                kojiPolish = TEST_KOJI_POLISH,
                                kakeMai = "五百万石",
                                kakePolish = TEST_KAKE_POLISH,
                                sakeDegree = TEST_SAKE_DEGREE,
                                acidity = TEST_ACIDITY,
                                yeast = "協会9号",
                                water = "伏流水",
                            ),
                        ),
                )
            val viewModel =
                SakeEditViewModel(
                    savedStateHandle = SavedStateHandle(mapOf(AppDestination.ARG_SAKE_ID to EXISTING_SAKE_ID)),
                    sakeRepository = repository,
                    sakeImageRepository = RecordingSakeImageRepository(),
                    masterDataRepository = FakeMasterDataRepository(),
                )
            advanceUntilIdle()

            viewModel.onGradeSelected("BROKEN_VALUE")
            viewModel.save()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(null, state.grade)
            assertEquals(null, state.error)
            assertEquals(
                FieldValidationError.REQUIRED_SELECTION,
                state.validationErrors[SakeValidationField.GRADE],
            )
            assertTrue(repository.savedInputs.isEmpty())
        }

    @Test
    fun loadInitial_editModeWithMissingSake_setsLoadErrorAndBlocksSave() =
        runTest {
            val repository = RecordingSakeRepository()
            val viewModel =
                SakeEditViewModel(
                    savedStateHandle = SavedStateHandle(mapOf(AppDestination.ARG_SAKE_ID to EXISTING_SAKE_ID)),
                    sakeRepository = repository,
                    sakeImageRepository = RecordingSakeImageRepository(),
                    masterDataRepository = FakeMasterDataRepository(),
                )
            advanceUntilIdle()

            viewModel.onTextChanged(SakeTextField.NAME, "should not save")
            viewModel.onGradeSelected(SakeGrade.JUNMAI.name)
            viewModel.save()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertTrue(state.isEditTargetMissing)
            assertEquals(R.string.error_load_sake, state.error?.messageResId)
            assertEquals(EXISTING_SAKE_ID.toString(), state.error?.causeKey)
            assertTrue(repository.savedInputs.isEmpty())
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
                                grade = SakeGrade.OTHER,
                                imageUris = listOf(EXISTING_IMAGE_URI),
                                gradeOther = "普通酒",
                                type = listOf(SakeClassification.KIMOTO, SakeClassification.OTHER),
                                typeOther = "限定品",
                                maker = "既存酒造",
                                prefecture = Prefecture.NAGANO,
                                alcohol = TEST_ALCOHOL,
                                kojiMai = "山田錦",
                                kojiPolish = TEST_KOJI_POLISH,
                                kakeMai = "五百万石",
                                kakePolish = TEST_KAKE_POLISH,
                                sakeDegree = TEST_SAKE_DEGREE,
                                acidity = TEST_ACIDITY,
                                yeast = "協会9号",
                                water = "伏流水",
                            ),
                        ),
                )
            val viewModel =
                SakeEditViewModel(
                    savedStateHandle = SavedStateHandle(mapOf(AppDestination.ARG_SAKE_ID to EXISTING_SAKE_ID)),
                    sakeRepository = repository,
                    sakeImageRepository = RecordingSakeImageRepository(),
                    masterDataRepository = FakeMasterDataRepository(),
                )
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals(EXISTING_SAKE_ID, state.sakeId)
            assertEquals("既存銘柄", state.name)
            assertEquals(SakeGrade.OTHER, state.grade)
            assertEquals(listOf(EXISTING_IMAGE_URI), state.imagePreviewUris)
            assertEquals(listOf(EXISTING_IMAGE_URI), state.persistedImageUris)
            assertEquals("普通酒", state.gradeOther)
            assertEquals(listOf(SakeClassification.KIMOTO, SakeClassification.OTHER), state.classifications)
            assertEquals("限定品", state.typeOther)
            assertEquals("既存酒造", state.maker)
            assertEquals(Prefecture.NAGANO, state.prefecture)
            assertEquals("3.5", state.sakeDegree)
            assertEquals("1.4", state.acidity)
            assertEquals("山田錦", state.kojiMai)
            assertEquals("50", state.kojiPolish)
            assertEquals("五百万石", state.kakeMai)
            assertEquals("55", state.kakePolish)
            assertEquals("16", state.alcohol)
            assertEquals("協会9号", state.yeast)
            assertEquals("伏流水", state.water)
        }
}

class RecordingSakeRepository(
    initial: List<Sake> = emptyList(),
    private val upsertFailure: Throwable? = null,
) : SakeRepository {
    private val stream = MutableStateFlow(initial)
    val savedInputs = mutableListOf<SakeInput>()

    override fun observeSakes(): Flow<List<Sake>> = stream

    override fun observeSakeListSummaries(): Flow<List<SakeListSummary>> =
        stream.map { list -> list.map { SakeListSummary(it) } }

    override suspend fun getSake(id: SakeId): Sake? = stream.value.firstOrNull { it.id == id }

    override suspend fun upsertSake(input: SakeInput): SakeId {
        savedInputs.add(input)
        upsertFailure?.let { throw it }
        val id = input.id ?: ((stream.value.maxOfOrNull { it.id } ?: 0L) + 1L)
        val mapped =
            Sake(
                id = id,
                name = input.name,
                grade = input.grade,
                isPinned = input.isPinned,
                imageUris = input.imageUris,
                gradeOther = input.gradeOther,
                type = input.type,
                typeOther = input.typeOther,
                maker = input.maker,
                prefecture = input.prefecture,
                alcohol = input.alcohol,
                kojiMai = input.kojiMai,
                kojiPolish = input.kojiPolish,
                kakeMai = input.kakeMai,
                kakePolish = input.kakePolish,
                sakeDegree = input.sakeDegree,
                acidity = input.acidity,
                yeast = input.yeast,
                water = input.water,
            )
        val mutable = stream.value.toMutableList().apply { removeAll { it.id == id } }
        mutable.add(mapped)
        stream.value = mutable
        return id
    }

    override suspend fun setPinned(
        id: SakeId,
        isPinned: Boolean,
    ) {
        stream.value =
            stream.value.map { sake ->
                if (sake.id == id) {
                    sake.copy(isPinned = isPinned)
                } else {
                    sake
                }
            }
    }

    override suspend fun deleteSake(id: SakeId): SakeDeleteResult {
        val removed = stream.value.any { sake -> sake.id == id }
        if (removed) {
            stream.value = stream.value.filterNot { sake -> sake.id == id }
        }
        return SakeDeleteResult(isDeleted = removed)
    }
}

class RecordingSakeImageRepository(
    private val importedUri: String = "file:///images/sakes/imported.jpg",
    private val deleteFailures: Set<String> = emptySet(),
) : SakeImageRepository {
    val importedSources = mutableListOf<String>()
    val deletedUris = mutableListOf<String>()
    var cleanupCalls = 0

    override suspend fun importImage(sourceUri: String): String {
        importedSources += sourceUri
        return importedUri
    }

    override suspend fun deleteImage(imageUri: String?) {
        imageUri?.let { uri ->
            if (uri.startsWith("file:///images/sakes/") || uri.startsWith("file:///cache/images/sakes/capture/")) {
                deletedUris += uri
                if (uri in deleteFailures) {
                    error("delete failed for $uri")
                }
            }
        }
    }

    override suspend fun cleanupUnusedImages(): Int {
        cleanupCalls += 1
        val failure = deleteFailures.firstOrNull()
        if (failure != null) {
            error("delete failed for $failure")
        }
        return 0
    }
}

class FakeMasterDataRepository : MasterDataRepository {
    override suspend fun getMasterData(): MasterDataBundle =
        MasterDataBundle(
            sakeGrades =
                listOf(
                    MasterOption(value = SakeGrade.JUNMAI.name, label = "純米"),
                    MasterOption(value = SakeGrade.GINJO.name, label = "吟醸"),
                    MasterOption(value = SakeGrade.FUTSUSHU.name, label = "普通酒"),
                    MasterOption(value = SakeGrade.OTHER.name, label = "その他"),
                ),
            classifications =
                listOf(
                    MasterOption(value = SakeClassification.KIMOTO.name, label = "生酛"),
                    MasterOption(value = SakeClassification.OTHER.name, label = "その他"),
                    MasterOption(value = SakeClassification.NAMA.name, label = "生酒"),
                ),
            temperatures = emptyList(),
            colors = emptyList(),
            prefectures =
                listOf(
                    MasterOption(value = Prefecture.HOKKAIDO.name, label = "北海道"),
                    MasterOption(value = Prefecture.NAGANO.name, label = "長野県"),
                    MasterOption(value = Prefecture.TOKYO.name, label = "東京都"),
                ),
            intensityLevels = emptyList(),
            tasteLevels = emptyList(),
            overallReviews = emptyList(),
            aromaCategories = emptyList(),
        )
}
