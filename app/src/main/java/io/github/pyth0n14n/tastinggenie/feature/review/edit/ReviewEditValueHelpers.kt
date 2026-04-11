package io.github.pyth0n14n.tastinggenie.feature.review.edit

import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.UiError
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Aroma
import java.time.LocalDate
import java.time.format.DateTimeParseException

private const val MIN_VISCOSITY = 1
private const val MAX_VISCOSITY = 5

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

internal fun ReviewEditUiState.withViscosity(value: String): ReviewEditUiState {
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

internal fun <T> ReviewEditUiState.copySelection(
    value: String,
    selected: T?,
    assign: (ReviewEditUiState, T?) -> ReviewEditUiState,
): ReviewEditUiState =
    if (selected == null) {
        assign(this, null).copy(error = invalidSelectionError(value))
    } else {
        assign(this, selected).copy(error = null)
    }

internal fun invalidSelectionError(value: String): UiError =
    UiError(
        messageResId = R.string.error_invalid_review_selection,
        causeKey = value,
    )

internal fun List<Aroma>.toggle(target: Aroma): List<Aroma> =
    if (contains(target)) {
        filterNot { it == target }
    } else {
        this + target
    }
