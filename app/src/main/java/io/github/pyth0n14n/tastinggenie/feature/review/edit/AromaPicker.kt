@file:Suppress("LongMethod", "LongParameterList", "TooManyFunctions")

package io.github.pyth0n14n.tastinggenie.feature.review.edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.AromaComponent
import io.github.pyth0n14n.tastinggenie.domain.model.AromaLabelByValue
import io.github.pyth0n14n.tastinggenie.domain.model.AromaMasterEntries
import io.github.pyth0n14n.tastinggenie.domain.model.AromaMasterEntry
import io.github.pyth0n14n.tastinggenie.domain.model.AromaTaste
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Aroma

private const val SUMMARY_LABEL_LIMIT = 3
private const val CLEAR_BUTTON_MIN_WIDTH = 72
private val AromaCardHeight = 64.dp
private val AromaCardHorizontalPadding = 16.dp
private val SheetHorizontalPadding = 16.dp
private val SheetBottomPadding = 16.dp

@Composable
fun AromaPickerField(
    label: String,
    title: String,
    selectedValues: List<Aroma>,
    fallbackLabels: Map<String, String>,
    onSave: (List<Aroma>) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isPickerVisible by remember { mutableStateOf(false) }
    val summary =
        selectedValues
            .take(SUMMARY_LABEL_LIMIT)
            .joinToString("、") { aroma -> aroma.displayLabel(fallbackLabels) }
            .ifBlank { "未選択" }
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        AromaPickerFieldHeader(
            label = label,
            isClearEnabled = selectedValues.isNotEmpty(),
            onClear = { onSave(emptyList()) },
        )
        OutlinedCard(
            onClick = { isPickerVisible = true },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(AromaCardHeight),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(AromaCardHeight)
                        .padding(horizontal = AromaCardHorizontalPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = summary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "${selectedValues.distinct().size}件選択済み",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
    if (isPickerVisible) {
        AromaPickerBottomSheet(
            title = title,
            initialSelection = selectedValues,
            fallbackLabels = fallbackLabels,
            onDismiss = { isPickerVisible = false },
            onSave = { next ->
                isPickerVisible = false
                onSave(next)
            },
        )
    }
}

@Composable
private fun AromaPickerFieldHeader(
    label: String,
    isClearEnabled: Boolean,
    onClear: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
        )
        TextButton(
            onClick = onClear,
            enabled = isClearEnabled,
            modifier = Modifier.width(CLEAR_BUTTON_MIN_WIDTH.dp),
        ) {
            Text(
                text = stringResource(R.string.action_clear),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AromaPickerBottomSheet(
    title: String,
    initialSelection: List<Aroma>,
    fallbackLabels: Map<String, String>,
    onDismiss: () -> Unit,
    onSave: (List<Aroma>) -> Unit,
) {
    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            confirmValueChange = { nextValue -> nextValue != SheetValue.Hidden },
        )
    val viewModel = remember(initialSelection) { AromaPickerViewModel(initialSelection = initialSelection) }
    ModalBottomSheet(
        onDismissRequest = {},
        sheetState = sheetState,
        modifier =
            Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        shape = RectangleShape,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null,
    ) {
        AromaPickerSheetContent(
            title = title,
            viewModel = viewModel,
            fallbackLabels = fallbackLabels,
            onDismiss = onDismiss,
            onSave = { onSave(viewModel.selected.toSavedAromaList()) },
        )
    }
}

@Composable
private fun AromaPickerSheetContent(
    title: String,
    viewModel: AromaPickerViewModel,
    fallbackLabels: Map<String, String>,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    val uiModel = viewModel.uiModel
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
            AromaPickerHeader(
                title = title,
                selectedCount = viewModel.selected.size,
                onDismiss = onDismiss,
            )
            AromaFilterBar(
                query = viewModel.query,
                selected = viewModel.selected.toSavedAromaList(),
                fallbackLabels = fallbackLabels,
                onQueryChanged = viewModel::updateQuery,
                onSelectedRemoved = viewModel::toggleSelection,
            )
            HorizontalDivider()
            AromaAccordionList(
                uiModel = uiModel,
                selected = viewModel.selected,
                expandedTasteKeys = viewModel.expandedTasteKeys,
                expandedCategoryKeys = viewModel.expandedCategoryKeys,
                forceExpanded = viewModel.query.isNotBlank(),
                onTasteToggled = viewModel::toggleTasteExpanded,
                onCategoryToggled = viewModel::toggleCategoryExpanded,
                onSelectionToggled = viewModel::toggleSelection,
                modifier = Modifier.weight(1f),
            )
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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("完了")
                }
            }
        }
    }
}

@Composable
private fun AromaPickerHeader(
    title: String,
    selectedCount: Int,
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
            Icon(imageVector = Icons.Filled.Close, contentDescription = "閉じる")
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            Text(
                text = "${selectedCount}件選択済み",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AromaFilterBar(
    query: String,
    selected: List<Aroma>,
    fallbackLabels: Map<String, String>,
    onQueryChanged: (String) -> Unit,
    onSelectedRemoved: (Aroma) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SearchBar(
            query = query,
            onQueryChange = onQueryChanged,
            onSearch = onQueryChanged,
            active = false,
            onActiveChange = {},
            modifier = Modifier.fillMaxWidth(),
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
        SelectedAromaChips(
            selected = selected,
            fallbackLabels = fallbackLabels,
            onSelectedRemoved = onSelectedRemoved,
        )
    }
}

@Composable
private fun SelectedAromaChips(
    selected: List<Aroma>,
    fallbackLabels: Map<String, String>,
    onSelectedRemoved: (Aroma) -> Unit,
) {
    if (selected.isEmpty()) {
        AssistChip(
            onClick = {},
            label = { Text("未選択") },
            enabled = false,
        )
        return
    }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(selected, key = { aroma -> aroma.name }) { aroma ->
            InputChip(
                selected = true,
                onClick = { onSelectedRemoved(aroma) },
                label = { Text(aroma.displayLabel(fallbackLabels)) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "${aroma.displayLabel(fallbackLabels)}を解除",
                    )
                },
            )
        }
    }
}

@Composable
fun AromaAccordionList(
    uiModel: AromaUiModel,
    selected: Set<Aroma>,
    expandedTasteKeys: Set<AromaTaste>,
    expandedCategoryKeys: Set<String>,
    forceExpanded: Boolean,
    onTasteToggled: (AromaTaste) -> Unit,
    onCategoryToggled: (String) -> Unit,
    onSelectionToggled: (Aroma) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        if (uiModel.sections.isEmpty()) {
            item(key = "empty") {
                Text(
                    text = "条件に一致する香りがありません",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        uiModel.sections.forEach { tasteSection ->
            item(key = "taste_${tasteSection.taste.name}") {
                AccordionHeaderRow(
                    label = tasteSection.label,
                    count = tasteSection.selectedCount,
                    expanded = forceExpanded || tasteSection.taste in expandedTasteKeys,
                    onClick = { onTasteToggled(tasteSection.taste) },
                )
            }
            val isTasteExpanded = forceExpanded || tasteSection.taste in expandedTasteKeys
            if (isTasteExpanded) {
                tasteSection.categories.forEach { categorySection ->
                    val categoryKey = categorySection.key
                    item(key = "category_$categoryKey") {
                        AccordionHeaderRow(
                            label = categorySection.label,
                            count = categorySection.selectedCount,
                            expanded = forceExpanded || categoryKey in expandedCategoryKeys,
                            onClick = { onCategoryToggled(categoryKey) },
                            modifier = Modifier.padding(start = 16.dp),
                            supportingText = categorySection.componentLabels.joinToString(" / "),
                        )
                    }
                    val isCategoryExpanded = forceExpanded || categoryKey in expandedCategoryKeys
                    if (isCategoryExpanded) {
                        items(categorySection.items, key = { item -> item.aroma.name }) { item ->
                            AromaLeafRow(
                                item = item,
                                selected = item.aroma in selected,
                                onToggle = onSelectionToggled,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AccordionHeaderRow(
    label: String,
    count: Int,
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
) {
    ListItem(
        modifier = modifier.clickable(onClick = onClick),
        headlineContent = { Text(if (count > 0) "$label ($count)" else label) },
        supportingContent =
            supportingText
                ?.takeIf { it.isNotBlank() }
                ?.let { { Text(it, style = MaterialTheme.typography.bodySmall) } },
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
private fun AromaLeafRow(
    item: AromaExampleUiModel,
    selected: Boolean,
    onToggle: (Aroma) -> Unit,
) {
    ListItem(
        modifier =
            Modifier
                .padding(start = 32.dp)
                .clickable { onToggle(item.aroma) },
        headlineContent = { Text(item.label) },
        leadingContent = {
            Checkbox(
                checked = selected,
                onCheckedChange = { onToggle(item.aroma) },
            )
        },
    )
    HorizontalDivider()
}

class AromaPickerViewModel(
    initialSelection: List<Aroma>,
) {
    var query by mutableStateOf("")
        private set
    var tasteFilters by mutableStateOf<Set<AromaTaste>>(emptySet())
        private set
    var categoryFilters by mutableStateOf<Set<String>>(emptySet())
        private set
    var selected by mutableStateOf(initialSelection.toSet())
        private set
    var expandedTasteKeys by mutableStateOf<Set<AromaTaste>>(emptySet())
        private set
    var expandedCategoryKeys by mutableStateOf<Set<String>>(emptySet())
        private set

    val uiModel: AromaUiModel
        get() =
            buildAromaUiModel(
                query = query,
                tasteFilters = tasteFilters,
                categoryFilters = categoryFilters,
                selected = selected,
            )

    fun updateQuery(next: String) {
        query = next
    }

    fun toggleTasteFilter(taste: AromaTaste) {
        tasteFilters = tasteFilters.toggle(taste)
    }

    fun toggleSelection(aroma: Aroma) {
        selected = selected.toggle(aroma)
    }

    fun clearSelection() {
        selected = emptySet()
    }

    fun toggleTasteExpanded(taste: AromaTaste) {
        expandedTasteKeys = expandedTasteKeys.toggle(taste)
    }

    fun toggleCategoryExpanded(categoryKey: String) {
        expandedCategoryKeys = expandedCategoryKeys.toggle(categoryKey)
    }
}

data class AromaUiModel(
    val sections: List<AromaTasteSectionUiModel>,
)

data class AromaTasteSectionUiModel(
    val taste: AromaTaste,
    val label: String,
    val selectedCount: Int,
    val categories: List<AromaCategorySectionUiModel>,
)

data class AromaCategorySectionUiModel(
    val key: String,
    val label: String,
    val selectedCount: Int,
    val componentLabels: List<String>,
    val items: List<AromaExampleUiModel>,
)

data class AromaExampleUiModel(
    val aroma: Aroma,
    val label: String,
    val component: AromaComponent,
)

fun buildAromaUiModel(
    query: String,
    tasteFilters: Set<AromaTaste>,
    categoryFilters: Set<String>,
    selected: Set<Aroma>,
): AromaUiModel {
    val normalizedQuery = query.trim()
    val filtered =
        AromaMasterEntries
            .asSequence()
            .filter { entry -> normalizedQuery.isBlank() || entry.label.contains(normalizedQuery) }
            .filter { entry -> tasteFilters.isEmpty() || entry.taste in tasteFilters }
            .filter { entry -> categoryFilters.isEmpty() || entry.category in categoryFilters }
            .toList()
    return AromaUiModel(
        sections =
            AromaTaste.entries.mapNotNull { taste ->
                val tasteEntries = filtered.filter { it.taste == taste }
                if (tasteEntries.isEmpty()) {
                    null
                } else {
                    tasteEntries.toTasteSection(taste = taste, selected = selected)
                }
            },
    )
}

private fun List<AromaMasterEntry>.toTasteSection(
    taste: AromaTaste,
    selected: Set<Aroma>,
): AromaTasteSectionUiModel {
    val categories =
        distinctBy { it.category }.map { first ->
            val categoryEntries = filter { it.category == first.category }
            categoryEntries.toCategorySection(taste = taste, category = first.category, selected = selected)
        }
    return AromaTasteSectionUiModel(
        taste = taste,
        label = taste.label,
        selectedCount = categories.sumOf { it.selectedCount },
        categories = categories,
    )
}

private fun List<AromaMasterEntry>.toCategorySection(
    taste: AromaTaste,
    category: String,
    selected: Set<Aroma>,
): AromaCategorySectionUiModel {
    val distinctEntries = distinctBy { it.aroma }
    return AromaCategorySectionUiModel(
        key = categoryKey(taste = taste, category = category),
        label = category,
        selectedCount = distinctEntries.count { it.aroma in selected },
        componentLabels = distinctEntries.map { it.component.label }.distinct(),
        items =
            distinctEntries.map { entry ->
                AromaExampleUiModel(
                    aroma = entry.aroma,
                    label = entry.label,
                    component = entry.component,
                )
            },
    )
}

fun categoryKey(
    taste: AromaTaste,
    category: String,
): String = "${taste.name}:$category"

fun Set<Aroma>.toSavedAromaList(): List<Aroma> {
    val known = AromaMasterEntries.map { it.aroma }.filter { it in this }
    val legacy = filterNot { aroma -> AromaMasterEntries.any { it.aroma == aroma } }
    return known + legacy
}

private fun <T> Set<T>.toggle(value: T): Set<T> =
    if (value in this) {
        this - value
    } else {
        this + value
    }

private fun Aroma.displayLabel(fallbackLabels: Map<String, String>): String =
    AromaLabelByValue[name] ?: fallbackLabels[name] ?: name

fun List<io.github.pyth0n14n.tastinggenie.domain.model.AromaCategoryMaster>.toAromaLabelMap(): Map<String, String> =
    flatMap { category -> category.items }.associate { item -> item.value to item.label }
