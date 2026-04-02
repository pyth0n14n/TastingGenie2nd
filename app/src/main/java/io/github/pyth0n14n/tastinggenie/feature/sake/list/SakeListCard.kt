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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.Sake

private const val CARD_PADDING = 12
private const val CARD_INNER_SPACING = 8
private const val CARD_IMAGE_RATIO_WIDTH = 4f
private const val CARD_IMAGE_RATIO_HEIGHT = 3f
private const val CARD_PLACEHOLDER_ALPHA = 0.45f

@Composable
fun SakeListCard(
    sake: Sake,
    gradeLabel: String,
    showImagePreview: Boolean,
    itemActions: SakeListItemActions,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { itemActions.onOpenSake(sake.id) },
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
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
                Text(
                    text = sake.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = gradeLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    modifier = Modifier.align(Alignment.End),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { itemActions.onDeleteSake(sake.id) }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.content_delete_sake),
                        )
                    }
                    TextButton(onClick = { itemActions.onEditSake(sake.id) }) {
                        Text(text = stringResource(R.string.action_edit))
                    }
                }
            }
        }
    }
}

@Composable
private fun SakeCardImage(imageUri: String?) {
    val shape = CardDefaults.elevatedShape
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
