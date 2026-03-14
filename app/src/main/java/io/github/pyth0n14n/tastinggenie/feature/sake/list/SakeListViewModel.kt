package io.github.pyth0n14n.tastinggenie.feature.sake.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.UiError
import io.github.pyth0n14n.tastinggenie.domain.repository.MasterDataRepository
import io.github.pyth0n14n.tastinggenie.domain.repository.SakeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SakeListViewModel
    @Inject
    constructor(
        private val sakeRepository: SakeRepository,
        private val masterDataRepository: MasterDataRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(SakeListUiState())
        val uiState: StateFlow<SakeListUiState> = _uiState.asStateFlow()

        init {
            loadInitial()
        }

        private fun loadInitial() {
            viewModelScope.launch {
                val gradeLabels =
                    runCatching {
                        masterDataRepository
                            .getMasterData()
                            .sakeGrades
                            .associate { option -> option.value to option.label }
                    }.getOrElse { throwable ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error =
                                    UiError(
                                        messageResId = R.string.error_load_sakes,
                                        causeKey = throwable.message,
                                    ),
                            )
                        }
                        return@launch
                    }

                // DB監視結果をそのままUiStateへ反映し、一覧再表示を自動化する。
                sakeRepository
                    .observeSakes()
                    .catch { throwable ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error =
                                    UiError(
                                        messageResId = R.string.error_load_sakes,
                                        causeKey = throwable.message,
                                    ),
                            )
                        }
                    }.collect { list ->
                        _uiState.update { current ->
                            current.copy(
                                isLoading = false,
                                error = null,
                                sakes = list,
                                gradeLabels = gradeLabels,
                            )
                        }
                    }
            }
        }
    }
