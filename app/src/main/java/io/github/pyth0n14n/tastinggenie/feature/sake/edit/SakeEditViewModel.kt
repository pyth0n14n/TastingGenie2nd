package io.github.pyth0n14n.tastinggenie.feature.sake.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.AppSettings
import io.github.pyth0n14n.tastinggenie.domain.model.MasterDataBundle
import io.github.pyth0n14n.tastinggenie.domain.model.Sake
import io.github.pyth0n14n.tastinggenie.domain.model.UiError
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Prefecture
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeClassification
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeGrade
import io.github.pyth0n14n.tastinggenie.domain.repository.MasterDataRepository
import io.github.pyth0n14n.tastinggenie.domain.repository.SakeImageRepository
import io.github.pyth0n14n.tastinggenie.domain.repository.SakeRepository
import io.github.pyth0n14n.tastinggenie.domain.repository.SettingsRepository
import io.github.pyth0n14n.tastinggenie.navigation.AppDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
@Suppress("TooManyFunctions")
class SakeEditViewModel
    @Inject
    constructor(
        private val savedStateHandle: SavedStateHandle,
        private val sakeRepository: SakeRepository,
        private val sakeImageRepository: SakeImageRepository,
        private val masterDataRepository: MasterDataRepository,
        private val settingsRepository: SettingsRepository = NoOpSettingsRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(SakeEditUiState())
        val uiState: StateFlow<SakeEditUiState> = _uiState.asStateFlow()

        init {
            loadInitial()
        }

        fun onTextChanged(
            field: SakeTextField,
            value: String,
        ) {
            updateEditableState { current ->
                current
                    .withTextFieldValue(field = field, value = value)
                    .clearValidationError(field.toValidationField())
            }
        }

        fun onImageSelected(imageUri: String) {
            updateEditableState { current ->
                if (imageUri in current.imagePreviewUris) {
                    return@updateEditableState current.copy(error = null)
                }
                current.copy(
                    imagePreviewUris = current.imagePreviewUris + imageUri,
                    pendingImageSourceUris = current.pendingImageSourceUris + imageUri,
                    error = null,
                )
            }
        }

        fun removeImage(imageUri: String) {
            val shouldCleanupPendingSource = uiState.value.pendingImageSourceUris.contains(imageUri)
            updateEditableState { current ->
                current.copy(
                    imagePreviewUris = current.imagePreviewUris - imageUri,
                    pendingImageSourceUris = current.pendingImageSourceUris - imageUri,
                    error = null,
                )
            }
            if (shouldCleanupPendingSource) {
                deleteImageSilently(imageUri)
            }
        }

        fun onGradeSelected(value: String) {
            val selectedGrade = SakeGrade.entries.firstOrNull { grade -> grade.name == value }
            _uiState.update { current ->
                if (current.isEditTargetMissing) {
                    return@update current
                }
                if (selectedGrade == null) {
                    current.copy(
                        grade = null,
                        error =
                            UiError(
                                messageResId = R.string.error_invalid_sake_grade,
                                causeKey = value,
                            ),
                    )
                } else {
                    current
                        .copy(
                            grade = selectedGrade,
                            gradeOther = if (selectedGrade == SakeGrade.OTHER) current.gradeOther else "",
                            error = null,
                        ).clearValidationError(SakeValidationField.GRADE)
                }
            }
        }

        fun onClassificationToggled(value: String) {
            val selectedClassification =
                SakeClassification.entries.firstOrNull { classification ->
                    classification.name == value
                }
            _uiState.update { current ->
                if (current.isEditTargetMissing) {
                    return@update current
                }
                if (selectedClassification == null) {
                    current.copy(
                        error =
                            UiError(
                                messageResId = R.string.error_invalid_sake_selection,
                                causeKey = value,
                            ),
                    )
                } else {
                    val nextSelections =
                        if (current.classifications.contains(selectedClassification)) {
                            current.classifications - selectedClassification
                        } else {
                            (current.classifications + selectedClassification)
                                .sortedBy { classification ->
                                    current.classificationOptions.indexOfFirst { option ->
                                        option.value == classification.name
                                    }
                                }
                        }
                    current.copy(
                        classifications = nextSelections,
                        typeOther =
                            if (nextSelections.contains(SakeClassification.OTHER)) {
                                current.typeOther
                            } else {
                                ""
                            },
                        error = null,
                    )
                }
            }
        }

        fun onPrefectureSelected(value: String?) {
            val selectedPrefecture =
                when (value) {
                    null -> null
                    else -> Prefecture.entries.firstOrNull { prefecture -> prefecture.name == value }
                }
            _uiState.update { current ->
                if (current.isEditTargetMissing) {
                    return@update current
                }
                if (value != null && selectedPrefecture == null) {
                    current.copy(
                        error =
                            UiError(
                                messageResId = R.string.error_invalid_sake_selection,
                                causeKey = value,
                            ),
                    )
                } else {
                    current.copy(
                        prefecture = selectedPrefecture,
                        error = null,
                    )
                }
            }
        }

        fun save() {
            val snapshot = uiState.value
            if (snapshot.isEditTargetMissing) {
                return
            }
            val input = snapshot.toValidatedInput()
            if (input == null) {
                _uiState.update { state ->
                    val validationErrors = state.validationErrorsForSave()
                    state.copy(
                        error =
                            if (validationErrors.isEmpty()) {
                                UiError(messageResId = R.string.error_invalid_sake_input)
                            } else {
                                null
                            },
                        validationErrors = validationErrors,
                        validationFailureCount = state.validationFailureCount + 1,
                    )
                }
                return
            }
            viewModelScope.launch {
                _uiState.update { it.copy(isSaving = true, error = null, validationErrors = emptyMap()) }
                runCatching {
                    val settings = settingsRepository.getCurrentSettings()
                    saveSake(
                        snapshot = snapshot,
                        input = input,
                        sakeRepository = sakeRepository,
                        sakeImageRepository = sakeImageRepository,
                        autoDeleteUnusedImages = settings.autoDeleteUnusedImages,
                    )
                }.onSuccess {
                    _uiState.update { it.copy(isSaving = false, isSaved = true) }
                }.onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error =
                                UiError(
                                    messageResId = R.string.error_save_sake,
                                    causeKey = throwable.message,
                                ),
                        )
                    }
                }
            }
        }

        fun consumeSaved() {
            _uiState.update { it.copy(isSaved = false) }
        }

        private fun loadInitial() {
            viewModelScope.launch {
                val sakeIdArg =
                    savedStateHandle.get<Long>(AppDestination.ARG_SAKE_ID) ?: AppDestination.NO_ID
                val sakeId = sakeIdArg.takeIf { it != AppDestination.NO_ID }
                // 編集モードでは既存データを読み込み、新規モードでは空フォームを返す。
                runCatching {
                    val master = masterDataRepository.getMasterData()
                    val existing = sakeId?.let { sakeRepository.getSake(it) }
                    master to existing
                }.onSuccess { (master, existing) ->
                    if (sakeId != null && existing == null) {
                        _uiState.update { state ->
                            state.withMissingEditTarget(master = master, sakeId = sakeId)
                        }
                        return@onSuccess
                    }
                    _uiState.update { state ->
                        state.withLoadedData(master = master, existing = existing)
                    }
                }.onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error =
                                UiError(
                                    messageResId = R.string.error_load_sake,
                                    causeKey = throwable.message,
                                ),
                        )
                    }
                }
            }
        }

        private fun updateEditableState(transform: (SakeEditUiState) -> SakeEditUiState) {
            _uiState.update { current ->
                if (current.isEditTargetMissing) {
                    current
                } else {
                    transform(current)
                }
            }
        }

        override fun onCleared() {
            cleanupPendingImageSourcesOnClear(
                sourceUris = _uiState.value.pendingImageSourceUris,
                sakeImageRepository = sakeImageRepository,
            )
            super.onCleared()
        }

        private fun deleteImageSilently(imageUri: String) {
            viewModelScope.launch {
                runCatching { sakeImageRepository.deleteImage(imageUri) }
            }
        }
    }

private fun SakeEditUiState.withTextFieldValue(
    field: SakeTextField,
    value: String,
): SakeEditUiState =
    when (field) {
        SakeTextField.NAME,
        SakeTextField.GRADE_OTHER,
        SakeTextField.TYPE_OTHER,
        SakeTextField.MAKER,
        SakeTextField.CITY,
        -> withBasicTextFieldValue(field = field, value = value)

        SakeTextField.SAKE_DEGREE,
        SakeTextField.ACIDITY,
        SakeTextField.AMINO,
        SakeTextField.ALCOHOL,
        -> withNumericTextFieldValue(field = field, value = value)

        SakeTextField.KOJI_MAI,
        SakeTextField.KOJI_POLISH,
        SakeTextField.KAKE_MAI,
        SakeTextField.KAKE_POLISH,
        SakeTextField.YEAST,
        SakeTextField.WATER,
        -> withSourceTextFieldValue(field = field, value = value)
    }

private fun SakeEditUiState.withBasicTextFieldValue(
    field: SakeTextField,
    value: String,
): SakeEditUiState =
    when (field) {
        SakeTextField.NAME -> copy(name = value)
        SakeTextField.GRADE_OTHER -> copy(gradeOther = value)
        SakeTextField.TYPE_OTHER -> copy(typeOther = value)
        SakeTextField.MAKER -> copy(maker = value)
        SakeTextField.CITY -> copy(city = value)
        else -> this
    }

private fun SakeEditUiState.withNumericTextFieldValue(
    field: SakeTextField,
    value: String,
): SakeEditUiState =
    when (field) {
        SakeTextField.SAKE_DEGREE -> copy(sakeDegree = value)
        SakeTextField.ACIDITY -> copy(acidity = value)
        SakeTextField.AMINO -> copy(amino = value)
        SakeTextField.ALCOHOL -> copy(alcohol = value)
        else -> this
    }

private fun SakeEditUiState.withSourceTextFieldValue(
    field: SakeTextField,
    value: String,
): SakeEditUiState =
    when (field) {
        SakeTextField.KOJI_MAI -> copy(kojiMai = value)
        SakeTextField.KOJI_POLISH -> copy(kojiPolish = value)
        SakeTextField.KAKE_MAI -> copy(kakeMai = value)
        SakeTextField.KAKE_POLISH -> copy(kakePolish = value)
        SakeTextField.YEAST -> copy(yeast = value)
        SakeTextField.WATER -> copy(water = value)
        else -> this
    }

private fun cleanupPendingImageSourcesOnClear(
    sourceUris: List<String>,
    sakeImageRepository: SakeImageRepository,
) {
    if (sourceUris.isEmpty()) {
        return
    }
    runBlocking {
        sourceUris.forEach { sourceUri ->
            runCatching { sakeImageRepository.deleteImage(sourceUri) }
        }
    }
}

private fun SakeEditUiState.withMissingEditTarget(
    master: MasterDataBundle,
    sakeId: Long,
): SakeEditUiState =
    copy(
        isLoading = false,
        isEditTargetMissing = true,
        gradeOptions = master.sakeGrades,
        classificationOptions = master.classifications,
        prefectureOptions = master.prefectures,
        error =
            UiError(
                messageResId = R.string.error_load_sake,
                causeKey = sakeId.toString(),
            ),
    )

private object NoOpSettingsRepository : SettingsRepository {
    override fun observeSettings() = MutableStateFlow(AppSettings())

    override suspend fun getCurrentSettings() = AppSettings()

    override suspend fun updateShowHelpHints(enabled: Boolean) = Unit

    override suspend fun updateShowImagePreview(enabled: Boolean) = Unit

    override suspend fun updateShowReviewSoundness(enabled: Boolean) = Unit

    override suspend fun updateAutoDeleteUnusedImages(enabled: Boolean) = Unit
}

private fun SakeEditUiState.withLoadedData(
    master: MasterDataBundle,
    existing: Sake?,
): SakeEditUiState =
    copy(
        isLoading = false,
        isEditTargetMissing = false,
        gradeOptions = master.sakeGrades,
        classificationOptions = master.classifications,
        prefectureOptions = master.prefectures,
        sakeId = existing?.id,
        isPinned = existing?.isPinned ?: false,
        name = existing?.name.orEmpty(),
        grade = existing?.grade,
        imagePreviewUris = existing?.imageUris.orEmpty(),
        persistedImageUris = existing?.imageUris.orEmpty(),
        pendingImageSourceUris = emptyList(),
        gradeOther = existing?.gradeOther.orEmpty(),
        classifications = existing?.type.orEmpty(),
        typeOther = existing?.typeOther.orEmpty(),
        maker = existing?.maker.orEmpty(),
        prefecture = existing?.prefecture,
        city = existing?.city.orEmpty(),
        sakeDegree = existing?.sakeDegree?.toString().orEmpty(),
        acidity = existing?.acidity?.toString().orEmpty(),
        amino = existing?.amino?.toString().orEmpty(),
        kojiMai = existing?.kojiMai.orEmpty(),
        kojiPolish = existing?.kojiPolish?.toString().orEmpty(),
        kakeMai = existing?.kakeMai.orEmpty(),
        kakePolish = existing?.kakePolish?.toString().orEmpty(),
        alcohol = existing?.alcohol?.toString().orEmpty(),
        yeast = existing?.yeast.orEmpty(),
        water = existing?.water.orEmpty(),
        validationErrors = emptyMap(),
    )
