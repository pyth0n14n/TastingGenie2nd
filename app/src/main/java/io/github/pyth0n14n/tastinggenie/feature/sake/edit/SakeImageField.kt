package io.github.pyth0n14n.tastinggenie.feature.sake.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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

@Composable
fun SakeImageField(
    imageUri: String?,
    isSaving: Boolean,
    onPickImage: () -> Unit,
    onDeleteImageRequest: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.label_sake_image),
            style = MaterialTheme.typography.bodyLarge,
        )
        if (imageUri.isNullOrBlank()) {
            Text(
                text = stringResource(R.string.message_no_image),
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            AsyncImage(
                model = imageUri,
                contentDescription = stringResource(R.string.content_sake_image),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(IMAGE_FIELD_HEIGHT.dp),
                contentScale = ContentScale.Fit,
            )
        }
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
                        if (imageUri.isNullOrBlank()) {
                            stringResource(R.string.action_select_sake_image)
                        } else {
                            stringResource(R.string.action_replace_sake_image)
                        },
                )
            }
            if (!imageUri.isNullOrBlank()) {
                TextButton(
                    onClick = onDeleteImageRequest,
                    enabled = !isSaving,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.action_delete_sake_image))
                }
            }
        }
    }
}
