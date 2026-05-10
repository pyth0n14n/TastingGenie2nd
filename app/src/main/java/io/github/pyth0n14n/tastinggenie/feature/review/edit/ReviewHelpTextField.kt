package io.github.pyth0n14n.tastinggenie.feature.review.edit

import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.pyth0n14n.tastinggenie.domain.model.ReviewItemId
import io.github.pyth0n14n.tastinggenie.ui.common.FormFieldState
import io.github.pyth0n14n.tastinggenie.ui.common.supportingTextContent

@Composable
internal fun ReviewHelpTextField(
    ui: ReviewHelpTextFieldUi,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = ui.value,
        onValueChange = onValueChange,
        label = {
            ReviewHelpLabel(
                label = ui.label,
                itemId = ui.helpItemId,
                showHelpHints = ui.showHelpHints,
            )
        },
        modifier = modifier,
        singleLine = ui.singleLine,
        isError = ui.fieldState.isError,
        keyboardOptions = ui.fieldState.keyboardOptions,
        supportingText = supportingTextContent(ui.fieldState.errorText),
        prefix = ui.fieldState.prefixText?.let { prefixText -> { androidx.compose.material3.Text(prefixText) } },
        suffix = ui.fieldState.suffixText?.let { suffixText -> { androidx.compose.material3.Text(suffixText) } },
    )
}

internal data class ReviewHelpTextFieldUi(
    val label: String,
    val value: String,
    val showHelpHints: Boolean,
    val helpItemId: ReviewItemId? = null,
    val singleLine: Boolean = true,
    val fieldState: FormFieldState = FormFieldState(),
)
