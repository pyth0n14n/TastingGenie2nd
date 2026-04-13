package io.github.pyth0n14n.tastinggenie.feature.sake.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.github.pyth0n14n.tastinggenie.R

private const val IMAGE_FIELD_HEIGHT = 180
private const val IMAGE_PREVIEW_WIDTH_FRACTION = 0.85f

@Composable
fun SakeImageField(
    imageUris: List<String>,
    isSaving: Boolean,
    onPickImage: () -> Unit,
    onCaptureImage: () -> Unit,
    onDeleteImageRequest: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.label_sake_image),
            style = MaterialTheme.typography.bodyLarge,
        )
        SakeImagePreview(
            imageUris = imageUris,
            onDeleteImageRequest = onDeleteImageRequest,
        )
        SakeImageActions(
            hasImage = imageUris.isNotEmpty(),
            isSaving = isSaving,
            onPickImage = onPickImage,
            onCaptureImage = onCaptureImage,
        )
    }
}

@Composable
private fun SakeImagePreview(
    imageUris: List<String>,
    onDeleteImageRequest: (String) -> Unit,
) {
    if (imageUris.isEmpty()) {
        Text(
            text = stringResource(R.string.message_no_image),
            style = MaterialTheme.typography.bodyMedium,
        )
    } else {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items = imageUris, key = { imageUri -> imageUri }) { imageUri ->
                Surface(shadowElevation = 1.dp) {
                    Box(modifier = Modifier.fillParentMaxWidth(IMAGE_PREVIEW_WIDTH_FRACTION)) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = stringResource(R.string.content_sake_image),
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(IMAGE_FIELD_HEIGHT.dp),
                            contentScale = ContentScale.Fit,
                        )
                        TextButton(
                            onClick = { onDeleteImageRequest(imageUri) },
                            modifier = Modifier.padding(8.dp),
                        ) {
                            Text(stringResource(R.string.action_delete_sake_image))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SakeImageActions(
    hasImage: Boolean,
    isSaving: Boolean,
    onPickImage: () -> Unit,
    onCaptureImage: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedButton(
            onClick = onPickImage,
            enabled = !isSaving,
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text =
                    if (!hasImage) {
                        stringResource(R.string.action_select_sake_image)
                    } else {
                        stringResource(R.string.action_add_sake_image)
                    },
            )
        }
        OutlinedButton(
            onClick = onCaptureImage,
            enabled = !isSaving,
            modifier = Modifier.weight(1f),
        ) {
            Text(stringResource(R.string.action_capture_sake_image))
        }
    }
}
