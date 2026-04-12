package io.github.pyth0n14n.tastinggenie.feature.sake.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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

private const val CARD_PADDING = 12
private const val CARD_INNER_SPACING = 6
private const val CARD_IMAGE_RATIO_WIDTH = 4f
private const val CARD_IMAGE_RATIO_HEIGHT = 3f
private const val CARD_PLACEHOLDER_ALPHA = 0.45f
private const val FAVORITE_ICON_SIZE = 20
private const val FAVORITE_BUTTON_SIZE = 32
private const val STAR_ICON_SIZE = 16

data class SakeListCardLabels(
    val grade: String,
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
    OutlinedCard(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { itemActions.onOpenSake(sake.id) },
        border = CardDefaults.outlinedCardBorder(),
        elevation = CardDefaults.outlinedCardElevation(defaultElevation = 3.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(CARD_INNER_SPACING.dp),
        ) {
            if (showImagePreview) {
                SakeCardImage(imageUri = sake.imageUri)
            }
            Column(
                modifier = Modifier.padding(CARD_PADDING.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                SakeCardHeader(sake = sake, itemActions = itemActions)
                SakeCardMetaAndActions(
                    labels = labels,
                    sake = sake,
                    itemActions = itemActions,
                )
                SakeCardLatestReview(labels = labels)
            }
        }
    }
}

@Composable
private fun SakeCardHeader(
    sake: Sake,
    itemActions: SakeListItemActions,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = sake.name,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        IconButton(
            onClick = { itemActions.onTogglePinned(sake.id, !sake.isPinned) },
            modifier = Modifier.size(FAVORITE_BUTTON_SIZE.dp),
        ) {
            Icon(
                imageVector = if (sake.isPinned) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription =
                    stringResource(
                        if (sake.isPinned) {
                            R.string.content_unfavorite_sake
                        } else {
                            R.string.content_favorite_sake
                        },
                    ),
                modifier = Modifier.size(FAVORITE_ICON_SIZE.dp),
                tint =
                    if (sake.isPinned) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
            )
        }
    }
}

@Composable
private fun SakeCardMetaAndActions(
    labels: SakeListCardLabels,
    sake: Sake,
    itemActions: SakeListItemActions,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = labels.grade,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        SakeCardActions(
            sakeId = sake.id,
            itemActions = itemActions,
        )
    }
}

@Composable
private fun SakeCardActions(
    sakeId: Long,
    itemActions: SakeListItemActions,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = { itemActions.onEditSake(sakeId) }) {
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = stringResource(R.string.content_edit_sake),
            )
        }
        IconButton(onClick = { itemActions.onDeleteSake(sakeId) }) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = stringResource(R.string.content_delete_sake),
            )
        }
    }
}

@Composable
private fun SakeCardLatestReview(labels: SakeListCardLabels) {
    val selectedIndex = labels.latestOverallReview?.ordinal
    val contentDescription =
        labels.latestOverallReviewLabel?.let { label ->
            stringResource(R.string.content_latest_overall_review, label)
        } ?: stringResource(R.string.content_latest_overall_review_none)
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .semantics { this.contentDescription = contentDescription },
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OverallReview.entries.forEachIndexed { index, _ ->
            val isSelected = selectedIndex != null && index <= selectedIndex
            Icon(
                imageVector = if (isSelected) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = null,
                modifier = Modifier.size(STAR_ICON_SIZE.dp),
                tint =
                    if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    },
            )
        }
    }
}

@Composable
private fun SakeCardImage(imageUri: String?) {
    val shape = CardDefaults.outlinedShape
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .aspectRatio(CARD_IMAGE_RATIO_WIDTH / CARD_IMAGE_RATIO_HEIGHT)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (imageUri.isNullOrBlank()) {
            Text(
                text = stringResource(R.string.message_no_image),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = CARD_PLACEHOLDER_ALPHA),
                modifier = Modifier.padding(CARD_PADDING.dp),
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
