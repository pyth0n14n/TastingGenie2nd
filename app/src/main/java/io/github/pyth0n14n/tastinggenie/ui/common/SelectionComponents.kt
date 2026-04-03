package io.github.pyth0n14n.tastinggenie.ui.common

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.pyth0n14n.tastinggenie.R

private const val GROUP_MENU_MAX_HEIGHT = 320

data class DropdownOptionGroup(
    val label: String,
    val options: List<DropdownOption>,
)

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(text = message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(R.string.action_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.action_cancel))
            }
        },
    )
}

@Composable
fun SegmentedSingleChoiceField(
    label: String,
    options: List<DropdownOption>,
    selectedValue: String?,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = option.value == selectedValue,
                    onClick = { onSelected(option.value) },
                    shape =
                        androidx.compose.material3.SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = options.size,
                        ),
                    label = { Text(text = option.label) },
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun DatePickerField(
    label: String,
    value: String,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
    initialSelectedDateMillis: Long? = null,
) {
    var isDialogOpen by remember { mutableStateOf(false) }
    val displayedValue =
        if (value.isBlank()) {
            stringResource(R.string.label_unselected)
        } else {
            value
        }

    OutlinedTextField(
        value = displayedValue,
        onValueChange = {},
        modifier = modifier.datePickerTrigger { isDialogOpen = true },
        label = { Text(text = label) },
        readOnly = true,
        singleLine = true,
    )

    if (isDialogOpen) {
        val datePickerState =
            androidx.compose.material3.rememberDatePickerState(
                initialSelectedDateMillis = initialSelectedDateMillis,
            )
        DatePickerDialog(
            onDismissRequest = { isDialogOpen = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let(onDateSelected)
                        isDialogOpen = false
                    },
                    enabled = datePickerState.selectedDateMillis != null,
                ) {
                    Text(text = stringResource(R.string.action_select))
                }
            },
            dismissButton = {
                TextButton(onClick = { isDialogOpen = false }) {
                    Text(text = stringResource(R.string.action_cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private fun Modifier.datePickerTrigger(onOpen: () -> Unit): Modifier =
    fillMaxWidth()
        .pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(pass = androidx.compose.ui.input.pointer.PointerEventPass.Initial)
                val up =
                    waitForUpOrCancellation(
                        pass = androidx.compose.ui.input.pointer.PointerEventPass.Initial,
                    )
                if (up != null) {
                    onOpen()
                }
            }
        }.semantics {
            role = androidx.compose.ui.semantics.Role.Button
            onClick {
                onOpen()
                true
            }
        }

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun GroupedMultiSelectDropdown(
    label: String,
    groups: List<DropdownOptionGroup>,
    selectedValues: Collection<String>,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    var expandedGroups by remember(groups) { mutableStateOf(emptySet<String>()) }
    val summary = selectedSummary(groups = groups, selectedValues = selectedValues)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
            if (!expanded) {
                expandedGroups = emptySet()
            }
        },
        modifier = modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = summary,
            onValueChange = {},
            modifier =
                Modifier
                    .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
            label = { Text(text = label) },
            readOnly = true,
            singleLine = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                expandedGroups = emptySet()
            },
            modifier = Modifier.heightIn(max = GROUP_MENU_MAX_HEIGHT.dp),
        ) {
            GroupedDropdownMenuContent(
                groups = groups,
                selectedValues = selectedValues,
                expandedGroups = expandedGroups,
                onGroupToggle = { label ->
                    expandedGroups =
                        if (expandedGroups.contains(label)) {
                            expandedGroups - label
                        } else {
                            expandedGroups + label
                        }
                },
                onToggle = onToggle,
            )
        }
    }
}

@Composable
private fun GroupedDropdownMenuContent(
    groups: List<DropdownOptionGroup>,
    selectedValues: Collection<String>,
    expandedGroups: Set<String>,
    onGroupToggle: (String) -> Unit,
    onToggle: (String) -> Unit,
) {
    groups.forEachIndexed { index, group ->
        if (index > 0) {
            HorizontalDivider()
        }
        DropdownMenuItem(
            text = {
                Text(
                    text =
                        groupLabel(
                            group = group,
                            selectedValues = selectedValues,
                            expanded = expandedGroups.contains(group.label),
                        ),
                    style = MaterialTheme.typography.titleSmall,
                )
            },
            onClick = { onGroupToggle(group.label) },
        )
        if (expandedGroups.contains(group.label)) {
            group.options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        val prefix = if (selectedValues.contains(option.value)) "[x] " else "[ ] "
                        Text(text = "  $prefix${option.label}")
                    },
                    onClick = { onToggle(option.value) },
                )
            }
        }
    }
}

private fun groupLabel(
    group: DropdownOptionGroup,
    selectedValues: Collection<String>,
    expanded: Boolean,
): String {
    val selectedCount = group.options.count { option -> selectedValues.contains(option.value) }
    val prefix = if (expanded) "[-]" else "[+]"
    val countSuffix =
        if (selectedCount == 0) {
            ""
        } else {
            " ($selectedCount)"
        }
    return "$prefix ${group.label}$countSuffix"
}

@Composable
private fun selectedSummary(
    groups: List<DropdownOptionGroup>,
    selectedValues: Collection<String>,
): String {
    val labelMap = groups.flatMap { group -> group.options }.associate { option -> option.value to option.label }
    val selectedLabels = selectedValues.mapNotNull { value -> labelMap[value] }
    return when {
        selectedLabels.isEmpty() -> stringResource(R.string.label_none)
        selectedLabels.size <= 2 -> selectedLabels.joinToString()
        else -> stringResource(R.string.message_review_count, selectedLabels.size)
    }
}
