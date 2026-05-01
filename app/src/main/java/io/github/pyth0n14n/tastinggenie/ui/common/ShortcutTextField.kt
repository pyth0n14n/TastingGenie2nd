package io.github.pyth0n14n.tastinggenie.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
@Suppress("LongParameterList")
fun ShortcutTextField(
    label: String,
    value: String,
    shortcuts: List<DropdownOption>,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = true,
    fieldState: FormFieldState = FormFieldState(),
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        LabeledTextField(
            label = label,
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            fieldState = fieldState,
        )
        if (shortcuts.isEmpty()) {
            return@Column
        }
        ShortcutChips(
            shortcuts = shortcuts,
            selectedValue = value,
            onSelected = onValueChange,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
fun ShortcutChips(
    shortcuts: List<DropdownOption>,
    selectedValue: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items = shortcuts, key = { option -> option.value }) { option ->
            FilterChip(
                selected = option.value == selectedValue,
                onClick = { onSelected(option.value) },
                label = {
                    Text(
                        text = option.label,
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
            )
        }
    }
}
