package io.github.pyth0n14n.tastinggenie.feature.review.edit

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.enums.FlavorProfileType
import io.github.pyth0n14n.tastinggenie.ui.theme.TastingGenie2ndAndroidTheme

private val SakeTypeGridShape = RoundedCornerShape(8.dp)
private val SakeTypeCellShape = RoundedCornerShape(6.dp)
private val SakeTypeGridHorizontalInset = 80.dp
private val SakeTypeMinGridSize = 96.dp

@Composable
internal fun SakeTypeQuadrantSelector(
    selectedType: FlavorProfileType?,
    onTypeSelected: (FlavorProfileType) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier =
            modifier
                .fillMaxWidth()
                .selectableGroup(),
    ) {
        val gridSize = (maxWidth - SakeTypeGridHorizontalInset * 2).coerceAtLeast(SakeTypeMinGridSize)
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            SakeTypeAxisLabel(text = stringResource(R.string.label_sake_type_aroma_high))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SakeTypeAxisLabel(text = stringResource(R.string.label_sake_type_taste_light))
                SakeTypeGrid(
                    selectedType = selectedType,
                    onTypeSelected = onTypeSelected,
                    modifier = Modifier.size(gridSize),
                )
                SakeTypeAxisLabel(text = stringResource(R.string.label_sake_type_taste_rich))
            }
            SakeTypeAxisLabel(text = stringResource(R.string.label_sake_type_aroma_low))
        }
    }
}

@Composable
private fun SakeTypeGrid(
    selectedType: FlavorProfileType?,
    onTypeSelected: (FlavorProfileType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(SakeTypeGridShape),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SakeTypeRow(
                leftType = FlavorProfileType.KUNSHU,
                rightType = FlavorProfileType.JUKUSHU,
                selectedType = selectedType,
                onTypeSelected = onTypeSelected,
                modifier = Modifier.weight(1f),
            )
            SakeTypeRow(
                leftType = FlavorProfileType.SOUSHU,
                rightType = FlavorProfileType.JUNSHU,
                selectedType = selectedType,
                onTypeSelected = onTypeSelected,
                modifier = Modifier.weight(1f),
            )
        }
        SakeTypeAxisOverlay(modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun SakeTypeRow(
    leftType: FlavorProfileType,
    rightType: FlavorProfileType,
    selectedType: FlavorProfileType?,
    onTypeSelected: (FlavorProfileType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth()) {
        SakeTypeCell(
            selected = selectedType == leftType,
            label = leftType.toSakeTypeLabel(),
            onClick = { onTypeSelected(leftType) },
            modifier = Modifier.weight(1f),
        )
        SakeTypeCell(
            selected = selectedType == rightType,
            label = rightType.toSakeTypeLabel(),
            onClick = { onTypeSelected(rightType) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
internal fun SakeTypeCell(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .padding(3.dp)
                .clip(SakeTypeCellShape)
                .background(if (selected) colorScheme.primaryContainer else colorScheme.surface)
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) colorScheme.primary else colorScheme.outlineVariant,
                    shape = SakeTypeCellShape,
                ).selectable(
                    selected = selected,
                    onClick = onClick,
                    role = Role.RadioButton,
                ).semantics {
                    this.selected = selected
                    contentDescription = label
                },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) colorScheme.onPrimaryContainer else colorScheme.onSurface,
        )
    }
}

@Composable
internal fun SakeTypeAxisOverlay(modifier: Modifier = Modifier) {
    val lineColor = MaterialTheme.colorScheme.outlineVariant
    Canvas(modifier = modifier) {
        val strokeWidth = 1.dp.toPx()
        drawLine(
            color = lineColor,
            start = Offset(size.width / 2f, 0f),
            end = Offset(size.width / 2f, size.height),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = lineColor,
            start = Offset(0f, size.height / 2f),
            end = Offset(size.width, size.height / 2f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun SakeTypeAxisLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun FlavorProfileType.toSakeTypeLabel(): String =
    when (this) {
        FlavorProfileType.KUNSHU -> stringResource(R.string.label_flavor_profile_kunshu)
        FlavorProfileType.JUKUSHU -> stringResource(R.string.label_flavor_profile_jukushu)
        FlavorProfileType.SOUSHU -> stringResource(R.string.label_flavor_profile_soushu)
        FlavorProfileType.JUNSHU -> stringResource(R.string.label_flavor_profile_junshu)
    }

@Preview(showBackground = true)
@Composable
@Suppress("UnusedPrivateMember")
private fun SakeTypeQuadrantSelectorPreview() {
    TastingGenie2ndAndroidTheme {
        SakeTypeQuadrantSelector(
            selectedType = FlavorProfileType.KUNSHU,
            onTypeSelected = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
