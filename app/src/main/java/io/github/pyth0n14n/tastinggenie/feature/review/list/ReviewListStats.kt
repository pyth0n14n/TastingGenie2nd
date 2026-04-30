package io.github.pyth0n14n.tastinggenie.feature.review.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.pyth0n14n.tastinggenie.R

private val StatsVerticalPadding = 14.dp
private val StatsDividerHeight = 56.dp

@Composable
internal fun ReviewStatsPanel(state: ReviewListUiState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = MaterialTheme.shapes.extraSmall,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = StatsVerticalPadding),
        ) {
            ReviewStatCell(
                label = stringResource(R.string.label_average_review),
                value = state.averageOverallReviewText,
                leading = {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                    )
                },
                modifier = Modifier.weight(1f),
            )
            StatsDivider()
            ReviewStatCell(
                label = stringResource(R.string.label_review_count),
                value = state.reviewCount.toString(),
                suffix = stringResource(R.string.suffix_review_count),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ReviewStatCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    leading: @Composable (() -> Unit)? = null,
    suffix: String? = null,
) {
    Column(
        modifier = modifier.padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            leading?.invoke()
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            suffix?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun StatsDivider() {
    val dividerColor = MaterialTheme.colorScheme.outlineVariant
    Box(
        modifier =
            Modifier
                .width(1.dp)
                .height(StatsDividerHeight)
                .drawBehind {
                    drawLine(
                        color = dividerColor,
                        start = Offset(x = 0f, y = 0f),
                        end = Offset(x = 0f, y = size.height),
                        strokeWidth = 1.dp.toPx(),
                    )
                },
    )
}
