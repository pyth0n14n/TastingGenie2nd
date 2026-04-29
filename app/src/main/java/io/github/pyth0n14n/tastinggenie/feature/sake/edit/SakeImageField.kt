package io.github.pyth0n14n.tastinggenie.feature.sake.edit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.github.pyth0n14n.tastinggenie.R

private const val IMAGE_CARD_HEIGHT = 102
private const val IMAGE_CARD_WIDTH = 120

@Composable
@Suppress("UNUSED_PARAMETER")
fun SakeImageField(
    imageUris: List<String>,
    isSaving: Boolean,
    onPickImage: () -> Unit,
    onCaptureImage: () -> Unit,
    onDeleteImageRequest: (String) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        item(key = "add_image") {
            SakeImageAddCard(
                isSaving = isSaving,
                onPickImage = onPickImage,
            )
        }
        items(
            items = imageUris,
            key = { imageUri -> imageUri },
        ) { imageUri ->
            SakeImagePreviewCard(
                imageUri = imageUri,
                onDeleteImageRequest = onDeleteImageRequest,
            )
        }
    }
}

@Composable
private fun SakeImageAddCard(
    isSaving: Boolean,
    onPickImage: () -> Unit,
) {
    val borderColor = MaterialTheme.colorScheme.outlineVariant
    Box(
        modifier =
            Modifier
                .width(IMAGE_CARD_WIDTH.dp)
                .height(IMAGE_CARD_HEIGHT.dp)
                .drawBehind {
                    val strokeWidth = 1.dp.toPx()
                    drawRoundRect(
                        color = borderColor,
                        cornerRadius = CornerRadius(4.dp.toPx()),
                        style =
                            Stroke(
                                width = strokeWidth,
                                pathEffect =
                                    PathEffect.dashPathEffect(
                                        intervals = floatArrayOf(3.dp.toPx(), 3.dp.toPx()),
                                    ),
                            ),
                    )
                }.clickable(enabled = !isSaving, onClick = onPickImage),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        ) {
            Icon(
                imageVector = Icons.Outlined.AddAPhoto,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = stringResource(R.string.action_add_sake_image),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.message_tap_to_select_image),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun SakeImagePreviewCard(
    imageUri: String,
    onDeleteImageRequest: (String) -> Unit,
) {
    Column(
        modifier = Modifier.width(IMAGE_CARD_WIDTH.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(
            modifier =
                Modifier
                    .width(IMAGE_CARD_WIDTH.dp)
                    .height(IMAGE_CARD_HEIGHT.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Box {
                AsyncImage(
                    model = imageUri,
                    contentDescription = stringResource(R.string.content_sake_image),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
        }
        TextButton(
            onClick = { onDeleteImageRequest(imageUri) },
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
            modifier = Modifier.height(32.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = stringResource(R.string.action_delete_sake_image_short),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        }
    }
}
