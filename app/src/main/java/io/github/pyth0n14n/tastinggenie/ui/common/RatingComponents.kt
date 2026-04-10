package io.github.pyth0n14n.tastinggenie.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.pyth0n14n.tastinggenie.R

private const val TWO_OPTIONS = 2
private const val CLEAR_BUTTON_MIN_WIDTH = 72
private const val STEP_CHOICE_SPACING = 8
private const val STEP_CHOICE_MIN_HEIGHT = 44
private const val STEP_CHOICE_MIN_WIDTH = 72
private const val STEP_CHOICE_CORNER_RADIUS = 12
private const val STEP_LABEL_MAX_LINES = 2

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun DiscreteSliderField(
    label: String,
    options: List<DropdownOption>,
    selectedValue: String?,
    onValueChanged: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isSelected = selectedValue != null

    Column(modifier = modifier.fillMaxWidth()) {
        RatingFieldHeader(
            label = label,
            isClearEnabled = selectedValue != null,
            onClear = { onValueChanged(null) },
        )
        Text(
            text = selectedLabel(options = options, selectedValue = selectedValue),
            style = MaterialTheme.typography.bodyMedium,
            color = ratingValueColor(isSelected = isSelected),
        )
        if (options.size < TWO_OPTIONS) {
            return@Column
        }
        StepChoiceGroup(
            options = options,
            selectedValue = selectedValue,
            hasSelection = isSelected,
            onValueChanged = onValueChanged,
        )
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun StepChoiceGroup(
    options: List<DropdownOption>,
    selectedValue: String?,
    hasSelection: Boolean,
    onValueChanged: (String?) -> Unit,
) {
    FlowRow(
        modifier =
            Modifier
                .fillMaxWidth()
                .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(STEP_CHOICE_SPACING.dp),
        verticalArrangement = Arrangement.spacedBy(STEP_CHOICE_SPACING.dp),
    ) {
        options.forEach { option ->
            StepChoice(
                option = option,
                isSelected = option.value == selectedValue,
                hasSelection = hasSelection,
                onClick = { onValueChanged(option.value) },
            )
        }
    }
}

@Composable
private fun StepChoice(
    option: DropdownOption,
    isSelected: Boolean,
    hasSelection: Boolean,
    onClick: () -> Unit,
) {
    val stepColors = stepChoiceColors(isSelected = isSelected, hasSelection = hasSelection)
    Surface(
        modifier =
            Modifier
                .widthIn(min = STEP_CHOICE_MIN_WIDTH.dp)
                .heightIn(min = STEP_CHOICE_MIN_HEIGHT.dp)
                .selectable(
                    selected = isSelected,
                    onClick = onClick,
                    role = Role.RadioButton,
                ).semantics(mergeDescendants = true) {},
        shape = RoundedCornerShape(STEP_CHOICE_CORNER_RADIUS.dp),
        color = stepColors.container,
        contentColor = stepColors.content,
        border = BorderStroke(width = 1.dp, color = stepColors.border),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = option.label,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                maxLines = STEP_LABEL_MAX_LINES,
            )
        }
    }
}

@Composable
fun StarRatingField(
    label: String,
    options: List<DropdownOption>,
    selectedValue: String?,
    onValueChanged: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedIndex = options.indexOfFirst { option -> option.value == selectedValue }
    val isSelected = selectedValue != null

    Column(modifier = modifier.fillMaxWidth()) {
        RatingFieldHeader(
            label = label,
            isClearEnabled = selectedValue != null,
            onClear = { onValueChanged(null) },
        )
        Row {
            options.forEachIndexed { index, option ->
                IconButton(onClick = { onValueChanged(option.value) }) {
                    Icon(
                        imageVector = if (index <= selectedIndex) Icons.Filled.Star else Icons.Outlined.StarOutline,
                        contentDescription =
                            stringResource(
                                R.string.content_star_rating_option,
                                label,
                                index + 1,
                            ),
                        tint =
                            if (index <= selectedIndex) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                starOutlineColor(isSelected = isSelected)
                            },
                    )
                }
            }
        }
        Text(
            text = selectedLabel(options = options, selectedValue = selectedValue),
            style = MaterialTheme.typography.bodyMedium,
            color = ratingValueColor(isSelected = isSelected),
        )
    }
}

@Composable
private fun RatingFieldHeader(
    label: String,
    isClearEnabled: Boolean,
    onClear: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
        )
        TextButton(
            onClick = onClear,
            enabled = isClearEnabled,
            modifier = Modifier.width(CLEAR_BUTTON_MIN_WIDTH.dp),
        ) {
            Text(text = stringResource(R.string.action_clear))
        }
    }
}

@Composable
private fun selectedLabel(
    options: List<DropdownOption>,
    selectedValue: String?,
): String =
    options.firstOrNull { option -> option.value == selectedValue }?.label ?: stringResource(R.string.label_unselected)

@Composable
private fun ratingValueColor(isSelected: Boolean): Color =
    if (isSelected) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.outline
    }

@Composable
private fun starOutlineColor(isSelected: Boolean): Color =
    if (isSelected) {
        MaterialTheme.colorScheme.outline
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

private data class StepChoiceColors(
    val container: Color,
    val content: Color,
    val border: Color,
)

@Composable
private fun stepChoiceColors(
    isSelected: Boolean,
    hasSelection: Boolean,
): StepChoiceColors =
    if (isSelected) {
        StepChoiceColors(
            container = MaterialTheme.colorScheme.primaryContainer,
            content = MaterialTheme.colorScheme.onPrimaryContainer,
            border = MaterialTheme.colorScheme.primary,
        )
    } else if (hasSelection) {
        StepChoiceColors(
            container = MaterialTheme.colorScheme.surfaceVariant,
            content = MaterialTheme.colorScheme.onSurfaceVariant,
            border = MaterialTheme.colorScheme.outlineVariant,
        )
    } else {
        StepChoiceColors(
            container = MaterialTheme.colorScheme.surface,
            content = MaterialTheme.colorScheme.onSurfaceVariant,
            border = MaterialTheme.colorScheme.outline,
        )
    }
