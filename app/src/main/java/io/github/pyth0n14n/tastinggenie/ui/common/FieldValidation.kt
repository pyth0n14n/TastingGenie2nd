package io.github.pyth0n14n.tastinggenie.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.github.pyth0n14n.tastinggenie.R

enum class FieldValidationError {
    REQUIRED,
    REQUIRED_SELECTION,
    INVALID_NUMBER,
    INVALID_INTEGER_RANGE,
    INVALID_PERCENTAGE,
    INVALID_DATE,
}

data class FormFieldState(
    val required: Boolean = false,
    val errorText: String? = null,
) {
    val isError: Boolean
        get() = errorText != null
}

internal fun formFieldLabel(
    label: String,
    required: Boolean,
): String = if (required) "$label *" else label

internal fun supportingTextContent(text: String?): (@Composable () -> Unit)? =
    text?.let { message ->
        {
            androidx.compose.material3.Text(text = message)
        }
    }

@Composable
fun validationErrorText(
    label: String,
    error: FieldValidationError,
    minValue: Int? = null,
    maxValue: Int? = null,
): String =
    when (error) {
        FieldValidationError.REQUIRED -> stringResource(R.string.error_required_field, label)
        FieldValidationError.REQUIRED_SELECTION ->
            stringResource(R.string.error_required_selection_field, label)
        FieldValidationError.INVALID_NUMBER ->
            stringResource(R.string.error_invalid_number_field, label)
        FieldValidationError.INVALID_INTEGER_RANGE ->
            stringResource(
                R.string.error_invalid_integer_range_field,
                label,
                requireNotNull(minValue),
                requireNotNull(maxValue),
            )
        FieldValidationError.INVALID_PERCENTAGE ->
            stringResource(R.string.error_invalid_percentage_field, label)
        FieldValidationError.INVALID_DATE -> stringResource(R.string.error_invalid_date_field, label)
    }
