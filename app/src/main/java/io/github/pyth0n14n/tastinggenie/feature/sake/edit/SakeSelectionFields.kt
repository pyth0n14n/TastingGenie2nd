@file:Suppress("LongMethod", "LongParameterList", "TooManyFunctions")

package io.github.pyth0n14n.tastinggenie.feature.sake.edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.ui.common.DropdownOption
import io.github.pyth0n14n.tastinggenie.ui.common.DropdownOptionGroup

private const val SUMMARY_LABEL_LIMIT = 3
private val SheetHorizontalPadding = 16.dp
private val SheetBottomPadding = 16.dp
private val SelectFieldTrailingWidth = 48.dp
private val SelectFieldClearAndTrailingWidth = 96.dp

@Composable
internal fun SakePrefectureSelectField(
    label: String,
    groups: List<DropdownOptionGroup>,
    selectedValue: String?,
    onSelected: (String?) -> Unit,
) {
    var isPickerVisible by remember { mutableStateOf(false) }
    SakeSelectTextField(
        label = label,
        valueText = groups.groupedOptionLabelOf(selectedValue) ?: stringResource(R.string.label_unselected),
        isClearEnabled = selectedValue != null,
        onOpen = { isPickerVisible = true },
        onClear = { onSelected(null) },
    )
    if (isPickerVisible) {
        SakePrefectureBottomSheet(
            title = label,
            groups = groups,
            selectedValue = selectedValue,
            onDismiss = { isPickerVisible = false },
            onSelected = { value ->
                onSelected(value)
                isPickerVisible = false
            },
        )
    }
}

@Composable
internal fun SakeClassificationSelectField(
    label: String,
    groups: List<DropdownOptionGroup>,
    selectedValues: List<String>,
    onSelectionChanged: (List<String>) -> Unit,
) {
    var isPickerVisible by remember { mutableStateOf(false) }
    SakeSelectTextField(
        label = label,
        valueText = classificationSummary(groups = groups, selectedValues = selectedValues),
        isClearEnabled = selectedValues.isNotEmpty(),
        onOpen = { isPickerVisible = true },
        onClear = { onSelectionChanged(emptyList()) },
    )
    if (isPickerVisible) {
        SakeClassificationBottomSheet(
            title = label,
            groups = groups,
            selectedValues = selectedValues,
            onDismiss = { isPickerVisible = false },
            onSave = { values ->
                onSelectionChanged(values)
                isPickerVisible = false
            },
        )
    }
}

@Composable
private fun SakeSelectTextField(
    label: String,
    valueText: String,
    isClearEnabled: Boolean,
    onOpen: () -> Unit,
    onClear: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .semantics {
                    role = Role.Button
                    contentDescription = "$label: $valueText"
                },
    ) {
        OutlinedTextField(
            value = valueText,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            readOnly = true,
            singleLine = true,
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isClearEnabled) {
                        IconButton(onClick = onClear) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.action_clear),
                            )
                        }
                    }
                    IconButton(onClick = onOpen) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                        )
                    }
                }
            },
        )
        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .padding(
                        end =
                            if (isClearEnabled) {
                                SelectFieldClearAndTrailingWidth
                            } else {
                                SelectFieldTrailingWidth
                            },
                    ).clickable(role = Role.Button, onClick = onOpen),
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SakePrefectureBottomSheet(
    title: String,
    groups: List<DropdownOptionGroup>,
    selectedValue: String?,
    onDismiss: () -> Unit,
    onSelected: (String) -> Unit,
) {
    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            confirmValueChange = { nextValue -> nextValue != SheetValue.Hidden },
        )
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier =
            Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        shape = RectangleShape,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null,
    ) {
        var query by rememberSaveable { mutableStateOf("") }
        var expandedGroupKeys by remember(groups) { mutableStateOf(emptySet<String>()) }
        val filteredGroups = groups.filteredBy(query)
        val forceExpanded = query.isNotBlank()
        SakePickerSheetContent(
            title = title,
            selectedSummary = groups.groupedOptionLabelOf(selectedValue),
            query = query,
            onQueryChanged = { query = it },
            onDismiss = onDismiss,
            bottomBar = {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        ) {
            GroupedSingleSelectList(
                groups = filteredGroups,
                selectedValue = selectedValue,
                expandedGroupKeys = expandedGroupKeys,
                forceExpanded = forceExpanded,
                onGroupToggled = { key -> expandedGroupKeys = expandedGroupKeys.toggle(key) },
                onSelected = onSelected,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SakeClassificationBottomSheet(
    title: String,
    groups: List<DropdownOptionGroup>,
    selectedValues: List<String>,
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit,
) {
    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            confirmValueChange = { nextValue -> nextValue != SheetValue.Hidden },
        )
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier =
            Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        shape = RectangleShape,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null,
    ) {
        var query by rememberSaveable { mutableStateOf("") }
        var stagedValues by remember(selectedValues) { mutableStateOf(selectedValues.toSet()) }
        var expandedGroupKeys by remember(groups) { mutableStateOf(emptySet<String>()) }
        val filteredGroups = groups.filteredBy(query)
        val forceExpanded = query.isNotBlank()
        SakePickerSheetContent(
            title = title,
            selectedSummary = "${stagedValues.size}件選択済み",
            query = query,
            onQueryChanged = { query = it },
            onDismiss = onDismiss,
            bottomBar = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    TextButton(
                        onClick = { stagedValues = emptySet() },
                        modifier = Modifier.weight(1f),
                        enabled = stagedValues.isNotEmpty(),
                    ) {
                        Text(stringResource(R.string.action_clear))
                    }
                    Button(
                        onClick = { onSave(groups.orderedValues(stagedValues)) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("完了")
                    }
                }
            },
        ) {
            GroupedMultiSelectList(
                groups = filteredGroups,
                selectedValues = stagedValues,
                expandedGroupKeys = expandedGroupKeys,
                forceExpanded = forceExpanded,
                onGroupToggled = { key -> expandedGroupKeys = expandedGroupKeys.toggle(key) },
                onSelectionToggled = { value -> stagedValues = stagedValues.toggle(value) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SakePickerSheetContent(
    title: String,
    selectedSummary: String?,
    query: String,
    onQueryChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    bottomBar: @Composable () -> Unit,
    listContent: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxHeight(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
        ) {
            SakePickerHeader(title = title, selectedSummary = selectedSummary, onDismiss = onDismiss)
            SakePickerFilterBar(query = query, onQueryChanged = onQueryChanged)
            HorizontalDivider()
            listContent()
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(
                            start = SheetHorizontalPadding,
                            end = SheetHorizontalPadding,
                            top = 8.dp,
                            bottom = SheetBottomPadding,
                        ),
            ) {
                bottomBar()
            }
        }
    }
}

@Composable
private fun SakePickerHeader(
    title: String,
    selectedSummary: String?,
    onDismiss: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onDismiss) {
            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.action_close_message))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            selectedSummary?.let { summary ->
                Text(
                    text = summary,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SakePickerFilterBar(
    query: String,
    onQueryChanged: (String) -> Unit,
) {
    SearchBar(
        query = query,
        onQueryChange = onQueryChanged,
        onSearch = onQueryChanged,
        active = false,
        onActiveChange = {},
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text("検索") },
        leadingIcon = { Icon(imageVector = Icons.Outlined.Search, contentDescription = null) },
        trailingIcon =
            if (query.isNotBlank()) {
                {
                    IconButton(onClick = { onQueryChanged("") }) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "検索語をクリア")
                    }
                }
            } else {
                null
            },
    ) {
        Spacer(modifier = Modifier.height(0.dp))
    }
}

@Composable
private fun GroupedSingleSelectList(
    groups: List<DropdownOptionGroup>,
    selectedValue: String?,
    expandedGroupKeys: Set<String>,
    forceExpanded: Boolean,
    onGroupToggled: (String) -> Unit,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        groups.forEach { group ->
            val groupKey = group.label
            val selectedCount = group.options.count { option -> option.value == selectedValue }
            item(key = "group_$groupKey") {
                SakeAccordionHeaderRow(
                    label = group.label,
                    count = selectedCount,
                    expanded = forceExpanded || groupKey in expandedGroupKeys,
                    onClick = { onGroupToggled(groupKey) },
                )
            }
            if (forceExpanded || groupKey in expandedGroupKeys) {
                items(group.options, key = { option -> option.value }) { option ->
                    SakeRadioOptionRow(
                        label = option.label,
                        selected = option.value == selectedValue,
                        onClick = { onSelected(option.value) },
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupedMultiSelectList(
    groups: List<DropdownOptionGroup>,
    selectedValues: Set<String>,
    expandedGroupKeys: Set<String>,
    forceExpanded: Boolean,
    onGroupToggled: (String) -> Unit,
    onSelectionToggled: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        groups.forEach { group ->
            val groupKey = group.label
            val selectedCount = group.options.count { option -> option.value in selectedValues }
            item(key = "group_$groupKey") {
                SakeAccordionHeaderRow(
                    label = group.label,
                    count = selectedCount,
                    expanded = forceExpanded || groupKey in expandedGroupKeys,
                    onClick = { onGroupToggled(groupKey) },
                )
            }
            if (forceExpanded || groupKey in expandedGroupKeys) {
                items(group.options, key = { option -> option.value }) { option ->
                    val selected = option.value in selectedValues
                    ListItem(
                        modifier = Modifier.clickable { onSelectionToggled(option.value) },
                        headlineContent = { Text(option.label) },
                        leadingContent = {
                            Checkbox(
                                checked = selected,
                                onCheckedChange = { onSelectionToggled(option.value) },
                            )
                        },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun SakeAccordionHeaderRow(
    label: String,
    count: Int,
    expanded: Boolean,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(if (count > 0) "$label ($count)" else label) },
        trailingContent = {
            Icon(
                imageVector =
                    if (expanded) {
                        Icons.Filled.ExpandMore
                    } else {
                        Icons.AutoMirrored.Filled.KeyboardArrowRight
                    },
                contentDescription = null,
            )
        },
    )
    HorizontalDivider()
}

@Composable
private fun SakeRadioOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(label) },
        leadingContent = {
            RadioButton(
                selected = selected,
                onClick = onClick,
            )
        },
    )
    HorizontalDivider()
}

@Composable
private fun classificationSummary(
    groups: List<DropdownOptionGroup>,
    selectedValues: List<String>,
): String {
    if (selectedValues.isEmpty()) {
        return stringResource(R.string.label_unselected)
    }
    val labelsByValue = groups.flattenOptions().associate { option -> option.value to option.label }
    val labels = selectedValues.mapNotNull { value -> labelsByValue[value] }
    return if (labels.size <= SUMMARY_LABEL_LIMIT) {
        labels.joinToString("、")
    } else {
        labels.take(SUMMARY_LABEL_LIMIT).joinToString("、") + " +${labels.size - SUMMARY_LABEL_LIMIT}"
    }
}

private fun List<DropdownOptionGroup>.groupedOptionLabelOf(value: String?): String? =
    flattenOptions().firstOrNull { option -> option.value == value }?.label

private fun List<DropdownOptionGroup>.flattenOptions(): List<DropdownOption> = flatMap { group -> group.options }

private fun List<DropdownOptionGroup>.filteredBy(query: String): List<DropdownOptionGroup> {
    val normalizedQuery = query.trim()
    if (normalizedQuery.isBlank()) {
        return this
    }
    return mapNotNull { group ->
        val options =
            group.options.filter { option ->
                option.label.contains(normalizedQuery) || option.value.contains(normalizedQuery, ignoreCase = true)
            }
        if (options.isEmpty()) {
            null
        } else {
            group.copy(options = options)
        }
    }
}

private fun Set<String>.toggle(value: String): Set<String> =
    if (value in this) {
        this - value
    } else {
        this + value
    }

private fun List<DropdownOptionGroup>.orderedValues(values: Set<String>): List<String> =
    flattenOptions().map { option -> option.value }.filter { value -> value in values }
