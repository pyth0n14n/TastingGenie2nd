package io.github.pyth0n14n.tastinggenie.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.pyth0n14n.tastinggenie.R
import kotlin.math.roundToInt

private const val TWO_OPTIONS = 2
private const val THREE_OPTIONS = 3
private const val MIDPOINT_DIVISOR = 2
private const val CLEAR_BUTTON_MIN_WIDTH = 72

@Composable
fun DiscreteSliderField(
    label: String,
    options: List<DropdownOption>,
    selectedValue: String?,
    onValueChanged: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    require(options.size >= TWO_OPTIONS) { "DiscreteSliderField requires at least 2 options" }
    val selectedIndex = options.indexOfFirst { option -> option.value == selectedValue }.takeIf { it >= 0 }
    val fallbackIndex = defaultSliderIndex(options.size)
    val isSelected = selectedValue != null
    var sliderValue by remember(selectedValue, options) {
        mutableFloatStateOf((selectedIndex ?: fallbackIndex).toFloat())
    }

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
        Slider(
            value = sliderValue,
            onValueChange = { next ->
                val nextIndex = next.roundToInt().coerceIn(0, options.lastIndex)
                sliderValue = nextIndex.toFloat()
                onValueChanged(options[nextIndex].value)
            },
            onValueChangeFinished = null,
            valueRange = 0f..options.lastIndex.toFloat(),
            steps = options.size - TWO_OPTIONS,
            colors = sliderColors(isSelected = isSelected),
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = options.first().label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall,
                color = ratingEndpointColor(isSelected = isSelected),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = options.last().label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.End,
                color = ratingEndpointColor(isSelected = isSelected),
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

private fun defaultSliderIndex(optionCount: Int): Int =
    when (optionCount) {
        TWO_OPTIONS -> 0
        THREE_OPTIONS -> 1
        else -> optionCount / MIDPOINT_DIVISOR
    }

@Composable
private fun selectedLabel(
    options: List<DropdownOption>,
    selectedValue: String?,
): String =
    options.firstOrNull { option -> option.value == selectedValue }?.label ?: stringResource(R.string.label_unselected)

@Composable
private fun sliderColors(isSelected: Boolean) =
    if (isSelected) {
        SliderDefaults.colors()
    } else {
        SliderDefaults.colors(
            thumbColor = MaterialTheme.colorScheme.outline,
            activeTrackColor = MaterialTheme.colorScheme.outline,
            inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant,
            activeTickColor = MaterialTheme.colorScheme.surface,
            inactiveTickColor = MaterialTheme.colorScheme.outline,
        )
    }

@Composable
private fun ratingValueColor(isSelected: Boolean): Color =
    if (isSelected) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.outline
    }

@Composable
private fun ratingEndpointColor(isSelected: Boolean): Color =
    if (isSelected) {
        MaterialTheme.colorScheme.onSurfaceVariant
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
