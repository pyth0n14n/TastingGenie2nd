package io.github.pyth0n14n.tastinggenie.feature.sake.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.Sake
import io.github.pyth0n14n.tastinggenie.domain.model.enums.OverallReview
import io.github.pyth0n14n.tastinggenie.ui.common.OverflowAction
import io.github.pyth0n14n.tastinggenie.ui.common.OverflowActionsMenu
import io.github.pyth0n14n.tastinggenie.ui.theme.TastingSakeChipContainer
import io.github.pyth0n14n.tastinggenie.ui.theme.TastingSakeChipOutline
import io.github.pyth0n14n.tastinggenie.ui.theme.TastingTypeChipContainer
import io.github.pyth0n14n.tastinggenie.ui.theme.TastingTypeChipOutline

private val ThumbnailSize = 56.dp
private val ChipShape = RoundedCornerShape(4.dp)

data class SakeListCardLabels(
    val grade: String,
    val classifications: List<String> = emptyList(),
    val prefecture: String? = null,
    val latestOverallReview: OverallReview? = null,
    val latestOverallReviewLabel: String? = null,
)

@Composable
fun SakeListCard(
    sake: Sake,
    labels: SakeListCardLabels,
    showImagePreview: Boolean,
    itemActions: SakeListItemActions,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { itemActions.onOpenSake(sake.id) },
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (showImagePreview) {
                    SakeListThumbnail(imageUri = sake.primaryImageUri)
                }
                SakeListItemBody(
                    sake = sake,
                    labels = labels,
                    modifier = Modifier.weight(1f),
                )
                SakeListTrailing(
                    sake = sake,
                    labels = labels,
                    itemActions = itemActions,
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Composable
private fun SakeListItemBody(
    sake: Sake,
    labels: SakeListCardLabels,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = sake.name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        SakeListChips(labels = labels)
        sake.maker?.takeIf { it.isNotBlank() }?.let { maker ->
            SakeListSupportingText(text = maker)
        }
        labels.prefecture?.let { prefecture ->
            SakeListSupportingText(text = prefecture)
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun SakeListChips(labels: SakeListCardLabels) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        SakeAssistChip(
            text = labels.grade,
            containerColor = TastingSakeChipContainer,
            outlineColor = TastingSakeChipOutline,
        )
        labels.classifications.forEach { classification ->
            SakeAssistChip(
                text = classification,
                containerColor = TastingTypeChipContainer,
                outlineColor = TastingTypeChipOutline,
            )
        }
    }
}

@Composable
private fun SakeAssistChip(
    text: String,
    containerColor: androidx.compose.ui.graphics.Color,
    outlineColor: androidx.compose.ui.graphics.Color,
) {
    Surface(
        shape = ChipShape,
        color = containerColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, outlineColor),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SakeListSupportingText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun SakeListTrailing(
    sake: Sake,
    labels: SakeListCardLabels,
    itemActions: SakeListItemActions,
) {
    Column(
        modifier =
            Modifier
                .width(64.dp)
                .fillMaxHeight(),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        SakeListRating(labels = labels)
        OverflowActionsMenu(
            actions =
                listOf(
                    OverflowAction(
                        labelRes =
                            if (sake.isPinned) {
                                R.string.action_unpin_sake
                            } else {
                                R.string.action_pin_sake
                            },
                        onClick = { itemActions.onTogglePinned(sake.id, !sake.isPinned) },
                    ),
                    OverflowAction(
                        labelRes = R.string.action_edit,
                        onClick = { itemActions.onEditSake(sake.id) },
                    ),
                    OverflowAction(
                        labelRes = R.string.action_delete,
                        onClick = { itemActions.onDeleteSake(sake.id) },
                    ),
                ),
        )
    }
}

@Composable
private fun SakeListRating(labels: SakeListCardLabels) {
    val ratingText = labels.latestOverallReview?.let { review -> "${review.ordinal + 1}.00" }
    val contentDescription =
        labels.latestOverallReviewLabel?.let { label ->
            stringResource(R.string.content_latest_overall_review, label)
        } ?: stringResource(R.string.content_latest_overall_review_none)
    Row(
        modifier = Modifier.semantics { this.contentDescription = contentDescription },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = ratingText ?: "-",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SakeListThumbnail(imageUri: String?) {
    Box(
        modifier =
            Modifier
                .size(ThumbnailSize)
                .clip(RoundedCornerShape(0.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center,
    ) {
        if (imageUri.isNullOrBlank()) {
            Text(
                text = stringResource(R.string.label_sake_image),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(4.dp),
            )
        } else {
            AsyncImage(
                model = imageUri,
                contentDescription = stringResource(R.string.content_sake_image),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}
