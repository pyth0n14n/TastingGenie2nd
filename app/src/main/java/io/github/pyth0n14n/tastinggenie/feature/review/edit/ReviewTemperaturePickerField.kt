@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.pyth0n14n.tastinggenie.feature.review.edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Temperature
import io.github.pyth0n14n.tastinggenie.feature.review.guideTemperatureLabel
import io.github.pyth0n14n.tastinggenie.feature.review.temperatureAccentColor
import io.github.pyth0n14n.tastinggenie.ui.common.DropdownOption

private val TemperatureCardHorizontalPadding = 16.dp
private val TemperatureCardHeight = 64.dp
private val SheetHorizontalPadding = 20.dp
private val SheetBottomPadding = 24.dp
private val TemperatureRowHeight = 48.dp
private val TemperatureListTopPadding = 4.dp

@Composable
fun TemperaturePickerField(
    label: String,
    options: List<DropdownOption>,
    selectedValue: String?,
    onValueChanged: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isSheetOpen by remember { mutableStateOf(false) }
    val selectedTemperature = selectedValue.toTemperatureOrNull()
    val selectedLabel = options.firstOrNull { option -> option.value == selectedValue }?.label

    TemperaturePickerTrigger(
        ui =
            TemperaturePickerTriggerUi(
                label = label,
                selectedLabel = selectedLabel,
                selectedTemperature = selectedTemperature,
                isClearEnabled = selectedValue != null,
            ),
        onClear = { onValueChanged(null) },
        onOpen = { isSheetOpen = true },
        modifier = modifier,
    )

    if (isSheetOpen) {
        TemperaturePickerSheet(
            options = options,
            selectedValue = selectedValue,
            onSelected = { next ->
                onValueChanged(next)
                isSheetOpen = false
            },
            onDismiss = { isSheetOpen = false },
        )
    }
}

@Composable
private fun TemperaturePickerTrigger(
    ui: TemperaturePickerTriggerUi,
    onClear: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        TemperaturePickerHeader(
            label = ui.label,
            isClearEnabled = ui.isClearEnabled,
            onClear = onClear,
        )
        TemperaturePickerCard(
            selectedLabel = ui.selectedLabel,
            selectedTemperature = ui.selectedTemperature,
            onOpen = onOpen,
        )
    }
}

private data class TemperaturePickerTriggerUi(
    val label: String,
    val selectedLabel: String?,
    val selectedTemperature: Temperature?,
    val isClearEnabled: Boolean,
)

@Composable
private fun TemperaturePickerHeader(
    label: String,
    isClearEnabled: Boolean,
    onClear: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
        )
        TextButton(
            onClick = onClear,
            enabled = isClearEnabled,
        ) {
            Text(text = stringResource(R.string.action_clear))
        }
    }
}

@Composable
private fun TemperaturePickerCard(
    selectedLabel: String?,
    selectedTemperature: Temperature?,
    onOpen: () -> Unit,
) {
    OutlinedCard(
        onClick = onOpen,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(TemperatureCardHeight),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(TemperatureCardHeight)
                    .padding(horizontal = TemperatureCardHorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Thermostat,
                contentDescription = null,
                tint = selectedTemperature?.temperatureAccentColor() ?: MaterialTheme.colorScheme.outline,
            )
            TemperaturePickerCardText(
                selectedLabel = selectedLabel,
                selectedTemperature = selectedTemperature,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TemperaturePickerCardText(
    selectedLabel: String?,
    selectedTemperature: Temperature?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = selectedLabel ?: stringResource(R.string.label_unselected),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
        )
        selectedTemperature?.let { temperature ->
            Text(
                text = temperature.guideTemperatureLabel(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TemperaturePickerSheet(
    options: List<DropdownOption>,
    selectedValue: String?,
    onSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SheetHorizontalPadding)
                    .padding(bottom = SheetBottomPadding),
        ) {
            Text(
                text = stringResource(R.string.title_temperature_picker),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = stringResource(R.string.message_temperature_picker),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
            )
            TemperatureOptionList(
                options = options,
                selectedValue = selectedValue,
                onSelected = onSelected,
            )
        }
    }
}

@Composable
private fun TemperatureOptionList(
    options: List<DropdownOption>,
    selectedValue: String?,
    onSelected: (String) -> Unit,
) {
    OutlinedCard(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = TemperatureListTopPadding),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, option ->
                option.value.toTemperatureOrNull()?.let { temperature ->
                    TemperatureOptionRow(
                        label = option.label,
                        temperature = temperature,
                        selected = option.value == selectedValue,
                        onClick = { onSelected(option.value) },
                    )
                    if (index < options.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun TemperatureOptionRow(
    label: String,
    temperature: Temperature,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val accentColor = temperature.temperatureAccentColor()
    Surface(
        color =
            if (selected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerLowest
            },
        modifier =
            Modifier
                .fillMaxWidth()
                .height(TemperatureRowHeight)
                .clickable(onClick = onClick),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Thermostat,
                contentDescription = null,
                tint = accentColor,
            )
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = temperature.guideTemperatureLabel(),
                style = MaterialTheme.typography.bodyLarge,
                color = accentColor,
            )
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors =
                    RadioButtonDefaults.colors(
                        selectedColor = accentColor,
                    ),
            )
        }
    }
}

private fun String?.toTemperatureOrNull(): Temperature? =
    this?.let { value -> Temperature.entries.firstOrNull { temperature -> temperature.name == value } }
