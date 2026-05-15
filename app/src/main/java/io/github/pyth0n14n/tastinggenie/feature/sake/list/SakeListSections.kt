package io.github.pyth0n14n.tastinggenie.feature.sake.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.pyth0n14n.tastinggenie.domain.model.Sake
import io.github.pyth0n14n.tastinggenie.domain.model.SakeListSummary

private const val SECTION_HEADER_VERTICAL_PADDING = 8

@Composable
internal fun SakeListSection(
    title: String,
    items: List<SakeListSummary>,
    state: SakeListUiState,
    itemActions: SakeListItemActions,
) {
    if (items.isEmpty()) {
        return
    }
    var expanded by rememberSaveable(title) { mutableStateOf(true) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(vertical = SECTION_HEADER_VERTICAL_PADDING.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier =
                    Modifier
                        .weight(1f)
                        .clickable { expanded = !expanded },
            )
            IconButton(onClick = { expanded = !expanded }) {
                Icon(
                    imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = null,
                )
            }
        }
        if (expanded) {
            items.forEach { item ->
                SakeListCard(
                    sake = item.sake,
                    labels = item.toCardLabels(state),
                    itemActions = itemActions,
                )
            }
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
