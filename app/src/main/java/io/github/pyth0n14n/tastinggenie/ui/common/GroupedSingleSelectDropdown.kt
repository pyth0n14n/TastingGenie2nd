package io.github.pyth0n14n.tastinggenie.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuBoxScope
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import io.github.pyth0n14n.tastinggenie.R

private const val GROUP_MENU_MAX_HEIGHT = 320

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun GroupedSingleSelectDropdown(
    label: String,
    groups: List<DropdownOptionGroup>,
    selectedValue: String?,
    onSelected: (String?) -> Unit,
    fieldState: FormFieldState = FormFieldState(),
) {
    var expanded by remember { mutableStateOf(false) }
    var expandedGroups by remember(groups) { mutableStateOf(emptySet<String>()) }
    val summary = selectedSingleLabel(groups = groups, selectedValue = selectedValue)
    val menuState = GroupedSingleSelectMenuState(expanded, selectedValue, expandedGroups)
    val callbacks =
        GroupedSingleSelectCallbacks(
            onDismiss = {
                expanded = false
                expandedGroups = emptySet()
            },
            onGroupToggle = { groupLabel ->
                expandedGroups =
                    if (expandedGroups.contains(groupLabel)) {
                        expandedGroups - groupLabel
                    } else {
                        expandedGroups + groupLabel
                    }
            },
            onClearSelection = {
                onSelected(null)
                expanded = false
                expandedGroups = emptySet()
            },
            onOptionSelected = { value ->
                onSelected(value)
                expanded = false
                expandedGroups = emptySet()
            },
        )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
            if (!expanded) {
                expandedGroups = emptySet()
            }
        },
        modifier = Modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = summary,
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
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
        )
        GroupedSingleSelectMenu(groups = groups, state = menuState, callbacks = callbacks)
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ExposedDropdownMenuBoxScope.GroupedSingleSelectMenu(
    groups: List<DropdownOptionGroup>,
    state: GroupedSingleSelectMenuState,
    callbacks: GroupedSingleSelectCallbacks,
) {
    ExposedDropdownMenu(
        expanded = state.expanded,
        onDismissRequest = callbacks.onDismiss,
        modifier = Modifier.heightIn(max = GROUP_MENU_MAX_HEIGHT.dp),
    ) {
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.label_unselected)) },
            onClick = callbacks.onClearSelection,
        )
        HorizontalDivider()
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
                                selectedValue = state.selectedValue,
                                expanded = state.expandedGroups.contains(group.label),
                            ),
                        style = MaterialTheme.typography.titleSmall,
                    )
                },
                onClick = { callbacks.onGroupToggle(group.label) },
            )
            if (state.expandedGroups.contains(group.label)) {
                group.options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            val prefix = if (state.selectedValue == option.value) "(*) " else "( ) "
                            Text(text = "  $prefix${option.label}")
                        },
                        onClick = { callbacks.onOptionSelected(option.value) },
                    )
                }
            }
        }
    }
}

private fun groupLabel(
    group: DropdownOptionGroup,
    selectedValue: String?,
    expanded: Boolean,
): String {
    val selectedCount = group.options.count { option -> selectedValue == option.value }
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
private fun selectedSingleLabel(
    groups: List<DropdownOptionGroup>,
    selectedValue: String?,
): String {
    if (selectedValue == null) {
        return stringResource(R.string.label_unselected)
    }
    val labelMap =
        groups.flatMap { group -> group.options }.associate { option ->
            option.value to option.label
        }
    return labelMap[selectedValue] ?: stringResource(R.string.label_unselected)
}

private data class GroupedSingleSelectMenuState(
    val expanded: Boolean,
    val selectedValue: String?,
    val expandedGroups: Set<String>,
)

private data class GroupedSingleSelectCallbacks(
    val onDismiss: () -> Unit,
    val onGroupToggle: (String) -> Unit,
    val onClearSelection: () -> Unit,
    val onOptionSelected: (String) -> Unit,
)
