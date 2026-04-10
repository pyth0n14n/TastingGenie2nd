package io.github.pyth0n14n.tastinggenie.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.pyth0n14n.tastinggenie.R

@Composable
fun LabeledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = true,
    fieldState: FormFieldState = FormFieldState(),
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = formFieldLabel(label = label, required = fieldState.required)) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = singleLine,
        isError = fieldState.isError,
        supportingText = supportingTextContent(fieldState.errorText),
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SimpleDropdown(
    label: String,
    selectedLabel: String,
    options: List<DropdownOption>,
    onSelected: (String) -> Unit,
    fieldState: FormFieldState = FormFieldState(),
) {
    var expanded by remember { mutableStateOf(false) }
    val displayedLabel =
        if (selectedLabel.isBlank()) {
            stringResource(R.string.label_unselected)
        } else {
            selectedLabel
        }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = displayedLabel,
            onValueChange = {},
            modifier =
                Modifier
                    .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
            label = { Text(text = formFieldLabel(label = label, required = fieldState.required)) },
            readOnly = true,
            singleLine = true,
            isError = fieldState.isError,
            supportingText = supportingTextContent(fieldState.errorText),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onSelected(option.value)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
fun RequiredFieldHint() {
    Text(
        text = stringResource(R.string.message_required_field_hint),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
