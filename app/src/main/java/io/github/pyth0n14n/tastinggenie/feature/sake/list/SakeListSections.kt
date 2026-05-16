package io.github.pyth0n14n.tastinggenie.feature.sake.list

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.pyth0n14n.tastinggenie.domain.model.Sake
import io.github.pyth0n14n.tastinggenie.domain.model.SakeListSummary

private const val SECTION_HEADER_VERTICAL_PADDING = 8

internal fun LazyListScope.sakeListSection(
    section: SakeListSectionState,
    state: SakeListUiState,
    itemActions: SakeListItemActions,
) {
    if (section.items.isEmpty()) {
        return
    }
    item(key = "${section.keyPrefix}-header", contentType = "section-header") {
        SakeListSectionHeader(
            title = stringResource(section.titleRes),
            expanded = section.expanded,
            onToggleExpanded = section.onToggleExpanded,
        )
    }
    if (section.expanded) {
        items(
            items = section.items,
            key = { item -> "${section.keyPrefix}-${item.sake.id}" },
            contentType = { "sake-card" },
        ) { item ->
            SakeListCard(
                sake = item.sake,
                labels = item.toCardLabels(state),
                itemActions = itemActions,
            )
        }
    }
}

internal data class SakeListSectionState(
    val keyPrefix: String,
    @param:StringRes val titleRes: Int,
    val items: List<SakeListSummary>,
    val expanded: Boolean,
    val onToggleExpanded: () -> Unit,
)

@Composable
private fun SakeListSectionHeader(
    title: String,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleExpanded)
                .padding(vertical = SECTION_HEADER_VERTICAL_PADDING.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onToggleExpanded) {
            Icon(
                imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                contentDescription = null,
            )
        }
    }
}

private fun SakeListSummary.toCardLabels(state: SakeListUiState): SakeListCardLabels =
    SakeListCardLabels(
        grade = state.gradeLabels[sake.grade.name] ?: sake.grade.name,
        classifications =
            sake.type.map { classification ->
                state.classificationLabels[classification.name] ?: classification.name
            },
        prefecture =
            sakePrefectureAndCityLabel(
                sake = sake,
                prefectureLabels = state.prefectureLabels,
            ),
        averageOverallReview = averageOverallReview,
    )

private fun sakePrefectureAndCityLabel(
    sake: Sake,
    prefectureLabels: Map<String, String>,
): String? {
    val prefectureLabel =
        sake.prefecture?.name?.let { key ->
            prefectureLabels[key] ?: key
        }
    val city = sake.city?.trim().takeIf { value -> !value.isNullOrEmpty() }
    return listOfNotNull(prefectureLabel, city).takeIf { labels -> labels.isNotEmpty() }?.joinToString(" ")
}
