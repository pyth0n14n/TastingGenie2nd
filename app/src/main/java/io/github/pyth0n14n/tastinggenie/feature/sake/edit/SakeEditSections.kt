package io.github.pyth0n14n.tastinggenie.feature.sake.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.pyth0n14n.tastinggenie.R

private val SectionSpacing = 12.dp
private val FieldSpacing = 12.dp
private val TwoColumnMinWidth = 320.dp

@Composable
internal fun SakeEditImageSection(
    state: SakeEditUiState,
    callbacks: SakeEditCallbacks,
    onDeleteImageRequest: (String) -> Unit,
) {
    SakeEditSection(title = stringResource(R.string.label_sake_image)) {
        SakeImageField(
            imageUris = state.imagePreviewUris,
            isSaving = state.isSaving,
            onPickImage = callbacks.onPickImageRequest,
            onCaptureImage = callbacks.onCaptureImageRequest,
            onDeleteImageRequest = onDeleteImageRequest,
        )
    }
}

@Composable
internal fun SakeEditSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SectionSpacing),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth(),
        )
        content()
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
internal fun SakeEditResponsiveFieldGrid(content: SakeEditFieldGridScope.() -> Unit) {
    val scope = SakeEditFieldGridScope().apply(content)
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val columns = if (maxWidth >= TwoColumnMinWidth) 2 else 1
        val fieldWidth = fieldWidth(maxWidth = maxWidth, columns = columns)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(FieldSpacing),
            verticalArrangement = Arrangement.spacedBy(FieldSpacing),
            maxItemsInEachRow = columns,
        ) {
            scope.fields.forEach { field ->
                val modifier =
                    if (field.fullWidth) {
                        Modifier.fillMaxWidth()
                    } else {
                        Modifier.width(fieldWidth)
                    }
                Box(modifier = modifier) {
                    field()
                }
            }
        }
    }
}

private fun fieldWidth(
    maxWidth: Dp,
    columns: Int,
): Dp =
    if (columns == 1) {
        maxWidth
    } else {
        (maxWidth - FieldSpacing) / 2
    }

internal class SakeEditFieldGridScope {
    val fields = mutableListOf<SakeEditFieldGridItem>()

    fun field(content: @Composable () -> Unit) {
        fields += SakeEditFieldGridItem(fullWidth = false, content = content)
    }

    fun fullWidthField(content: @Composable () -> Unit) {
        fields += SakeEditFieldGridItem(fullWidth = true, content = content)
    }
}

internal data class SakeEditFieldGridItem(
    val fullWidth: Boolean,
    val content: @Composable () -> Unit,
) {
    @Composable
    operator fun invoke() = content()
}
