package io.github.pyth0n14n.tastinggenie.feature.review.food

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.MasterOption
import io.github.pyth0n14n.tastinggenie.domain.model.SakeFoodReviewInput
import io.github.pyth0n14n.tastinggenie.domain.model.UiError
import io.github.pyth0n14n.tastinggenie.domain.model.enums.FoodCompatibility
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Temperature
import io.github.pyth0n14n.tastinggenie.domain.repository.MasterDataRepository
import io.github.pyth0n14n.tastinggenie.domain.repository.SakeFoodReviewRepository
import io.github.pyth0n14n.tastinggenie.domain.repository.SakeRepository
import io.github.pyth0n14n.tastinggenie.feature.review.edit.defaultReviewDateText
import io.github.pyth0n14n.tastinggenie.feature.review.edit.toLocalDateOrNull
import io.github.pyth0n14n.tastinggenie.feature.review.edit.toReviewDateText
import io.github.pyth0n14n.tastinggenie.navigation.AppDestination
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SakeFoodReviewEditViewModel
    @Inject
    constructor(
        private val savedStateHandle: SavedStateHandle,
        private val sakeRepository: SakeRepository,
        private val foodReviewRepository: SakeFoodReviewRepository,
        private val masterDataRepository: MasterDataRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(SakeFoodReviewEditUiState())
        val uiState: StateFlow<SakeFoodReviewEditUiState> = _uiState.asStateFlow()

        init {
            load()
        }

        @Suppress("LongMethod")
        private fun load() {
            viewModelScope.launch {
                val sakeId = savedStateHandle.get<Long>(AppDestination.ARG_SAKE_ID) ?: AppDestination.NO_ID
                val foodReviewId = savedStateHandle.get<Long>(AppDestination.ARG_FOOD_REVIEW_ID)
                if (sakeId == AppDestination.NO_ID) {
                    _uiState.update {
                        it.copy(isLoading = false, isSakeMissing = true, error = UiError(R.string.error_load_sake))
                    }
                    return@launch
                }
                runCatching {
                    val sake = sakeRepository.getSake(sakeId)
                    val review = foodReviewId?.let { foodReviewRepository.getFoodReview(it) }
                    val temperatures = masterDataRepository.getMasterData().temperatures
                    when {
                        sake == null ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    sakeId = sakeId,
                                    isSakeMissing = true,
                                    error = UiError(R.string.error_load_sake),
                                    temperatureOptions = temperatures,
                                )
                            }

                        foodReviewId != null && (review == null || review.sakeId != sakeId) ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    sakeId = sakeId,
                                    sakeName = sake.name,
                                    isEditTargetMissing = true,
                                    error = UiError(R.string.error_load_food_review),
                                    temperatureOptions = temperatures,
                                )
                            }

                        else ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    sakeId = sakeId,
                                    reviewId = review?.id,
                                    sakeName = sake.name,
                                    date = review?.date?.toString() ?: defaultReviewDateText(),
                                    bar = review?.bar.orEmpty(),
                                    dish = review?.dish.orEmpty(),
                                    foodCompatibility = review?.foodCompatibility,
                                    temperature = review?.temperature,
                                    freeComment = review?.freeComment.orEmpty(),
                                    temperatureOptions = temperatures,
                                    error = null,
                                )
                            }
                    }
                }.onFailure { throwable ->
                    if (throwable is CancellationException) throw throwable
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            sakeId = sakeId,
                            error = UiError(R.string.error_load_food_review, throwable.message),
                        )
                    }
                }
            }
        }

        fun onDateSelected(epochMillis: Long) {
            _uiState.update { it.copy(date = epochMillis.toReviewDateText(), error = null) }
        }

        fun onBarChanged(value: String) {
            _uiState.update { it.copy(bar = value, error = null) }
        }

        fun onDishChanged(value: String) {
            _uiState.update { it.copy(dish = value, error = null) }
        }

        fun onCompatibilityChanged(value: String?) {
            _uiState.update {
                it.copy(
                    foodCompatibility =
                        value?.let { raw ->
                            FoodCompatibility.entries.firstOrNull { entry -> entry.name == raw }
                        },
                    error = null,
                )
            }
        }

        fun onTemperatureChanged(value: String?) {
            _uiState.update {
                it.copy(
                    temperature = value?.let { raw -> Temperature.entries.firstOrNull { entry -> entry.name == raw } },
                    error = null,
                )
            }
        }

        fun onCommentChanged(value: String) {
            _uiState.update { it.copy(freeComment = value, error = null) }
        }

        @Suppress("TooGenericExceptionCaught")
        fun save() {
            val snapshot = _uiState.value
            val sakeId = snapshot.sakeId
            val date = snapshot.date.toLocalDateOrNull()
            if (sakeId == null || date == null) {
                _uiState.update { it.copy(error = UiError(R.string.error_invalid_review_input)) }
                return
            }
            viewModelScope.launch {
                _uiState.update { it.copy(isSaving = true, error = null) }
                try {
                    foodReviewRepository.upsertFoodReview(
                        SakeFoodReviewInput(
                            id = snapshot.reviewId,
                            sakeId = sakeId,
                            date = date,
                            bar = snapshot.bar.trimmedOrNull(),
                            dish = snapshot.dish.trimmedOrNull(),
                            foodCompatibility = snapshot.foodCompatibility,
                            temperature = snapshot.temperature,
                            freeComment = snapshot.freeComment.trimmedOrNull(),
                        ),
                    )
                    _uiState.update { it.copy(isSaving = false, isSaved = true) }
                } catch (throwable: CancellationException) {
                    throw throwable
                } catch (throwable: Exception) {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = UiError(R.string.error_save_food_review, throwable.message),
                        )
                    }
                }
            }
        }

        fun consumeSaved() {
            _uiState.update { it.copy(isSaved = false) }
        }
    }

data class SakeFoodReviewEditUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val isSakeMissing: Boolean = false,
    val isEditTargetMissing: Boolean = false,
    val error: UiError? = null,
    val sakeId: Long? = null,
    val reviewId: Long? = null,
    val sakeName: String = "",
    val date: String = defaultReviewDateText(),
    val bar: String = "",
    val dish: String = "",
    val foodCompatibility: FoodCompatibility? = null,
    val temperature: Temperature? = null,
    val freeComment: String = "",
    val temperatureOptions: List<MasterOption> = emptyList(),
) {
    val isInputLocked: Boolean
        get() = isSakeMissing || isEditTargetMissing
}

private fun String.trimmedOrNull(): String? = trim().takeIf { it.isNotEmpty() }
