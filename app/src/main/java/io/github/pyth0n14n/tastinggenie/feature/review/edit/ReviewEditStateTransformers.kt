package io.github.pyth0n14n.tastinggenie.feature.review.edit

import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.UiError
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Aroma
import io.github.pyth0n14n.tastinggenie.domain.model.enums.IntensityLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.OverallReview
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeColor
import io.github.pyth0n14n.tastinggenie.domain.model.enums.TasteLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Temperature
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.format.DateTimeParseException

const val INVALID_NUMBER = Int.MIN_VALUE
private const val MIN_VISCOSITY = 1
private const val MAX_VISCOSITY = 3

fun MutableStateFlow<ReviewEditUiState>.updateEditable(transform: (ReviewEditUiState) -> ReviewEditUiState) {
    update { current ->
        if (current.isInputLocked) {
            current
        } else {
            transform(current)
        }
    }
}

fun ReviewEditUiState.withText(
    field: ReviewTextField,
    value: String,
): ReviewEditUiState =
    when (field) {
        ReviewTextField.DATE -> copy(date = value, error = null)
        ReviewTextField.BAR -> copy(bar = value, error = null)
        ReviewTextField.PRICE -> copy(price = value, error = null)
        ReviewTextField.VOLUME -> copy(volume = value, error = null)
        ReviewTextField.SCENE -> copy(scene = value, error = null)
        ReviewTextField.DISH -> copy(dish = value, error = null)
        ReviewTextField.COMMENT -> copy(comment = value, error = null)
    }

fun ReviewEditUiState.withSelection(
    field: ReviewSelectionField,
    value: String,
): ReviewEditUiState =
    when (field) {
        ReviewSelectionField.TEMPERATURE ->
            copySelection(value, Temperature.entries.firstOrNull { it.name == value }) { state, selected ->
                state.copy(temperature = selected)
            }
        ReviewSelectionField.COLOR ->
            copySelection(value, SakeColor.entries.firstOrNull { it.name == value }) { state, selected ->
                state.copy(color = selected)
            }
        ReviewSelectionField.INTENSITY ->
            copySelection(value, IntensityLevel.entries.firstOrNull { it.name == value }) { state, selected ->
                state.copy(intensity = selected)
            }
        ReviewSelectionField.SWEET ->
            copySelection(value, TasteLevel.entries.firstOrNull { it.name == value }) { state, selected ->
                state.copy(sweet = selected)
            }
        ReviewSelectionField.SOUR ->
            copySelection(value, TasteLevel.entries.firstOrNull { it.name == value }) { state, selected ->
                state.copy(sour = selected)
            }
        ReviewSelectionField.BITTER ->
            copySelection(value, TasteLevel.entries.firstOrNull { it.name == value }) { state, selected ->
                state.copy(bitter = selected)
            }
        ReviewSelectionField.UMAMI ->
            copySelection(value, TasteLevel.entries.firstOrNull { it.name == value }) { state, selected ->
                state.copy(umami = selected)
            }
        ReviewSelectionField.SHARP ->
            copySelection(value, TasteLevel.entries.firstOrNull { it.name == value }) { state, selected ->
                state.copy(sharp = selected)
            }
        ReviewSelectionField.OVERALL_REVIEW ->
            copySelection(
                value,
                OverallReview.entries.firstOrNull { it.name == value },
            ) { state, selected ->
                state.copy(review = selected)
            }
        ReviewSelectionField.VISCOSITY -> withViscosity(value)
    }

fun ReviewEditUiState.withAromaToggled(
    field: ReviewAromaField,
    value: String,
): ReviewEditUiState {
    val aroma =
        Aroma.entries.firstOrNull { it.name == value }
            ?: return copy(error = invalidSelectionError(value))
    return when (field) {
        ReviewAromaField.TOP -> copy(scentTop = scentTop.toggle(aroma), error = null)
        ReviewAromaField.BASE -> copy(scentBase = scentBase.toggle(aroma), error = null)
        ReviewAromaField.MOUTH -> copy(scentMouth = scentMouth.toggle(aroma), error = null)
    }
}

fun String.toLocalDateOrNull(): LocalDate? =
    runCatching {
        trim().takeIf { it.isNotEmpty() }?.let(LocalDate::parse)
    }.getOrElse { throwable ->
        if (throwable is DateTimeParseException) {
            null
        } else {
            throw throwable
        }
    }

fun String.toOptionalInt(): Int? {
    val trimmed = trim()
    if (trimmed.isEmpty()) {
        return null
    }
    return trimmed.toIntOrNull() ?: INVALID_NUMBER
}

private fun ReviewEditUiState.withViscosity(value: String): ReviewEditUiState {
    val selected = value.toIntOrNull()?.takeIf { it in MIN_VISCOSITY..MAX_VISCOSITY }
    return if (selected == null) {
        copy(
            viscosity = null,
            error = invalidSelectionError(value),
        )
    } else {
        copy(viscosity = selected, error = null)
    }
}

private fun <T> ReviewEditUiState.copySelection(
    value: String,
    selected: T?,
    assign: (ReviewEditUiState, T?) -> ReviewEditUiState,
): ReviewEditUiState =
    if (value.isBlank()) {
        assign(this, null).copy(error = null)
    } else if (selected == null) {
        assign(this, null).copy(error = invalidSelectionError(value))
    } else {
        assign(this, selected).copy(error = null)
    }

private fun invalidSelectionError(value: String): UiError =
    UiError(
        messageResId = R.string.error_invalid_review_selection,
        causeKey = value,
    )

private fun List<Aroma>.toggle(target: Aroma): List<Aroma> =
    if (contains(target)) {
        filterNot { it == target }
    } else {
        this + target
    }
