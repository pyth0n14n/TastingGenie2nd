package io.github.pyth0n14n.tastinggenie.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.pyth0n14n.tastinggenie.R
import kotlin.math.roundToInt

private const val TWO_OPTIONS = 2
private const val CLEAR_BUTTON_MIN_WIDTH = 72
private const val SLIDER_VALUE_START = 0f

@Composable
fun DiscreteSliderField(
    label: String,
    options: List<DropdownOption>,
    selectedValue: String?,
    onValueChanged: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedIndex = options.indexOfFirst { option -> option.value == selectedValue }.takeIf { it >= 0 } ?: 0
    val maxIndex = options.lastIndex.coerceAtLeast(0)
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
        Slider(
            value = selectedIndex.toFloat(),
            onValueChange = { next ->
                val nextIndex = next.roundToInt().coerceIn(0, maxIndex)
                onValueChanged(options[nextIndex].value)
            },
            valueRange = SLIDER_VALUE_START..maxIndex.toFloat(),
            steps = (options.size - 2).coerceAtLeast(0),
            modifier = Modifier.fillMaxWidth(),
            colors =
                SliderDefaults.colors(
                    thumbColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant,
                ),
        )
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
        androidx.compose.foundation.layout.Row {
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
    androidx.compose.foundation.layout.Row(modifier = Modifier.fillMaxWidth()) {
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
