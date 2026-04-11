package io.github.pyth0n14n.tastinggenie.feature.review

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.enums.ComplexityLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.IntensityLevel

private const val GRID_SIZE = 5
private val GridCellShape = RoundedCornerShape(8.dp)

@Composable
fun ReviewFlavorProfileField(
    intensity: IntensityLevel?,
    complexity: ComplexityLevel?,
    onSelectionChanged: ((FlavorProfileSelection) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val selectedCell = selectedFlavorProfileCell(intensity = intensity, complexity = complexity)
    val selectedType = deriveFlavorProfileType(intensity = intensity, complexity = complexity)

    Column(modifier = modifier.fillMaxWidth()) {
        FlavorProfileHeader(selectedType = selectedType)
        FlavorProfileTopLegend()
        FlavorProfileGrid(
            selectedCell = selectedCell,
            onSelectionChanged = onSelectionChanged,
        )
        FlavorProfileBottomLegend()
    }
}

@Composable
private fun FlavorProfileHeader(selectedType: FlavorProfileType?) {
    Text(
        text = reviewFlavorProfileText(R.string.label_flavor_profile),
        style = MaterialTheme.typography.bodyLarge,
    )
    Text(
        text = selectedType?.toLabel() ?: reviewFlavorProfileText(R.string.label_unselected),
        style = MaterialTheme.typography.bodyMedium,
        color =
            if (selectedType == null) {
                MaterialTheme.colorScheme.outline
            } else {
                MaterialTheme.colorScheme.onSurface
            },
    )
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun FlavorProfileTopLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = FlavorProfileType.JUNSHU.toLabel(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
        )
        Text(
            text = FlavorProfileType.JUKUSHU.toLabel(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
        )
    }
    Text(
        text = reviewFlavorProfileText(R.string.label_flavor_profile_complexity_complex),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.outline,
    )
}

@Composable
private fun FlavorProfileGrid(
    selectedCell: FlavorProfileCell?,
    onSelectionChanged: ((FlavorProfileSelection) -> Unit)?,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .selectableGroup(),
    ) {
        flavorProfileComplexityLevels.forEachIndexed { yIndex, _ ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                repeat(GRID_SIZE) { xIndex ->
                    val selection = flavorProfileSelectionAt(xIndex = xIndex, yIndex = yIndex) ?: return@repeat
                    FlavorProfileCellBox(
                        selection = selection,
                        isSelected = selectedCell == FlavorProfileCell(xIndex = xIndex, yIndex = yIndex),
                        onClick = onSelectionChanged,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            if (yIndex != flavorProfileComplexityLevels.lastIndex) {
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun FlavorProfileBottomLegend() {
    Text(
        text = reviewFlavorProfileText(R.string.label_flavor_profile_complexity_simple),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.outline,
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = FlavorProfileType.SOUSHU.toLabel(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
        )
        Text(
            text = reviewFlavorProfileText(R.string.label_flavor_profile_intensity_axis),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
        )
        Text(
            text = FlavorProfileType.KUNSHU.toLabel(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
        )
    }
}

@Composable
private fun FlavorProfileCellBox(
    selection: FlavorProfileSelection,
    isSelected: Boolean,
    onClick: ((FlavorProfileSelection) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val cellDescription =
        stringResource(
            R.string.content_flavor_profile_cell,
            selection.intensity.toLabel(),
            selection.complexity.toLabel(),
        )
    val cellModifier =
        modifier
            .aspectRatio(1f)
            .clip(GridCellShape)
            .border(
                width = 1.dp,
                color =
                    if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    },
                shape = GridCellShape,
            ).background(
                color =
                    if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
            ).semantics {
                selected = isSelected
                contentDescription = cellDescription
            }.let { base ->
                if (onClick == null) {
                    base
                } else {
                    base.selectable(
                        selected = isSelected,
                        onClick = { onClick(selection) },
                        role = Role.RadioButton,
                    )
                }
            }

    Box(
        modifier = cellModifier,
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            Box(
                modifier =
                    Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(MaterialTheme.colorScheme.primary),
            )
        }
    }
}

@Composable
private fun FlavorProfileType.toLabel(): String =
    when (this) {
        FlavorProfileType.SOUSHU -> reviewFlavorProfileText(R.string.label_flavor_profile_soushu)
        FlavorProfileType.KUNSHU -> reviewFlavorProfileText(R.string.label_flavor_profile_kunshu)
        FlavorProfileType.JUNSHU -> reviewFlavorProfileText(R.string.label_flavor_profile_junshu)
        FlavorProfileType.JUKUSHU -> reviewFlavorProfileText(R.string.label_flavor_profile_jukushu)
    }

@Composable
private fun IntensityLevel.toLabel(): String =
    when (this) {
        IntensityLevel.VERY_WEAK -> reviewFlavorProfileText(R.string.label_intensity_very_weak)
        IntensityLevel.WEAK -> reviewFlavorProfileText(R.string.label_intensity_weak)
        IntensityLevel.MEDIUM -> reviewFlavorProfileText(R.string.label_intensity_medium)
        IntensityLevel.STRONG -> reviewFlavorProfileText(R.string.label_intensity_strong)
        IntensityLevel.VERY_STRONG -> reviewFlavorProfileText(R.string.label_intensity_very_strong)
    }

@Composable
private fun ComplexityLevel.toLabel(): String =
    when (this) {
        ComplexityLevel.SIMPLE -> reviewFlavorProfileText(R.string.label_complexity_simple)
        ComplexityLevel.SLIGHTLY_SIMPLE -> reviewFlavorProfileText(R.string.label_complexity_slightly_simple)
        ComplexityLevel.MEDIUM -> reviewFlavorProfileText(R.string.label_complexity_medium)
        ComplexityLevel.SLIGHTLY_COMPLEX -> reviewFlavorProfileText(R.string.label_complexity_slightly_complex)
        ComplexityLevel.COMPLEX -> reviewFlavorProfileText(R.string.label_complexity_complex)
    }

@Composable
private fun reviewFlavorProfileText(resId: Int): String = stringResource(resId)
