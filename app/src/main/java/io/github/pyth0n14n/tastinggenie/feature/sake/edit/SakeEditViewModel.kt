package io.github.pyth0n14n.tastinggenie.feature.sake.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.SakeInput
import io.github.pyth0n14n.tastinggenie.domain.model.UiError
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Prefecture
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeClassification
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeGrade
import io.github.pyth0n14n.tastinggenie.domain.repository.MasterDataRepository
import io.github.pyth0n14n.tastinggenie.domain.repository.SakeRepository
import io.github.pyth0n14n.tastinggenie.navigation.AppDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SakeEditViewModel
    @Inject
    constructor(
        private val savedStateHandle: SavedStateHandle,
        private val sakeRepository: SakeRepository,
        private val masterDataRepository: MasterDataRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(SakeEditUiState())
        val uiState: StateFlow<SakeEditUiState> = _uiState.asStateFlow()

        init {
            loadInitial()
        }

        fun onNameChanged(value: String) {
            _uiState.update { current ->
                if (current.isEditTargetMissing) {
                    current
                } else {
                    current.copy(name = value)
                }
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
                    current.copy(
                        grade = selectedGrade,
                        gradeOther = if (selectedGrade == SakeGrade.OTHER) current.gradeOther else "",
                        error = null,
                    )
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

        fun onGradeOtherChanged(value: String) {
            _uiState.update { current ->
                if (current.isEditTargetMissing) {
                    current
                } else {
                    current.copy(gradeOther = value)
                }
            }
        }

        fun onTypeOtherChanged(value: String) {
            _uiState.update { current ->
                if (current.isEditTargetMissing) {
                    current
                } else {
                    current.copy(typeOther = value)
                }
            }
        }

        fun onMakerChanged(value: String) {
            _uiState.update { current ->
                if (current.isEditTargetMissing) {
                    current
                } else {
                    current.copy(maker = value)
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
            val grade = snapshot.grade
            // PR-3 で必須にしている入力のみ先に弾く。
            if (snapshot.name.isBlank() || grade == null) {
                _uiState.update { it.copy(error = UiError(messageResId = R.string.error_invalid_sake_input)) }
                return
            }
            viewModelScope.launch {
                _uiState.update { it.copy(isSaving = true, error = null) }
                runCatching {
                    sakeRepository.upsertSake(
                        SakeInput(
                            id = snapshot.sakeId,
                            name = snapshot.name.trim(),
                            grade = grade,
                            gradeOther =
                                snapshot.gradeOther
                                    .normalizedOrNull()
                                    ?.takeIf { snapshot.grade == SakeGrade.OTHER },
                            type = snapshot.classifications,
                            typeOther =
                                snapshot.typeOther
                                    .normalizedOrNull()
                                    ?.takeIf { snapshot.classifications.contains(SakeClassification.OTHER) },
                            maker = snapshot.maker.normalizedOrNull(),
                            prefecture = snapshot.prefecture,
                        ),
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
                        _uiState.update {
                            it.copy(
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
                        }
                        return@onSuccess
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isEditTargetMissing = false,
                            gradeOptions = master.sakeGrades,
                            classificationOptions = master.classifications,
                            prefectureOptions = master.prefectures,
                            sakeId = existing?.id,
                            name = existing?.name.orEmpty(),
                            grade = existing?.grade,
                            gradeOther = existing?.gradeOther.orEmpty(),
                            classifications = existing?.type.orEmpty(),
                            typeOther = existing?.typeOther.orEmpty(),
                            maker = existing?.maker.orEmpty(),
                            prefecture = existing?.prefecture,
                        )
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
    }

private fun String.normalizedOrNull(): String? = trim().takeIf { value -> value.isNotEmpty() }
