@file:OptIn(ExperimentalMaterial3Api::class)
@file:Suppress("MagicNumber", "TooManyFunctions")

package io.github.pyth0n14n.tastinggenie.feature.review.edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.WaterDrop
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeColor
import io.github.pyth0n14n.tastinggenie.ui.common.DropdownOption

private val ColorCardHorizontalPadding = 16.dp
private val ColorCardHeight = 64.dp
private val SheetHorizontalPadding = 20.dp
private val SheetBottomPadding = 24.dp
private val ColorRowHeight = 52.dp
private val ColorListTopPadding = 4.dp
private val ColorIconSize = 28.dp
private val ColorIconInnerSize = 22.dp

@Composable
fun ColorPickerField(
    label: String,
    options: List<DropdownOption>,
    selectedValue: String?,
    onValueChanged: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isSheetOpen by remember { mutableStateOf(false) }
    val selectedColor = selectedValue.toSakeColorOrNull()
    val selectedLabel = options.firstOrNull { option -> option.value == selectedValue }?.label

    ColorPickerTrigger(
        ui =
            ColorPickerTriggerUi(
                label = label,
                selectedLabel = selectedLabel,
                selectedColor = selectedColor,
                isClearEnabled = selectedValue != null,
            ),
        onClear = { onValueChanged(null) },
        onOpen = { isSheetOpen = true },
        modifier = modifier,
    )

    if (isSheetOpen) {
        ColorPickerSheet(
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
private fun ColorPickerTrigger(
    ui: ColorPickerTriggerUi,
    onClear: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        ColorPickerHeader(
            label = ui.label,
            isClearEnabled = ui.isClearEnabled,
            onClear = onClear,
        )
        ColorPickerCard(
            selectedLabel = ui.selectedLabel,
            selectedColor = ui.selectedColor,
            onOpen = onOpen,
        )
    }
}

private data class ColorPickerTriggerUi(
    val label: String,
    val selectedLabel: String?,
    val selectedColor: SakeColor?,
    val isClearEnabled: Boolean,
)

@Composable
private fun ColorPickerHeader(
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
private fun ColorPickerCard(
    selectedLabel: String?,
    selectedColor: SakeColor?,
    onOpen: () -> Unit,
) {
    OutlinedCard(
        onClick = onOpen,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(ColorCardHeight),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(ColorCardHeight)
                    .padding(horizontal = ColorCardHorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ColorWaterDropIcon(color = selectedColor)
            Text(
                text = selectedLabel ?: stringResource(R.string.label_unselected),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
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
private fun ColorPickerSheet(
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
                text = stringResource(R.string.title_color_picker),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = stringResource(R.string.message_color_picker),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
            )
            ColorOptionList(
                options = options,
                selectedValue = selectedValue,
                onSelected = onSelected,
            )
        }
    }
}

@Composable
private fun ColorOptionList(
    options: List<DropdownOption>,
    selectedValue: String?,
    onSelected: (String) -> Unit,
) {
    OutlinedCard(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = ColorListTopPadding),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            val colorOptions = options.mapNotNull { option -> option.toColorOptionOrNull() }
            colorOptions.forEachIndexed { index, option ->
                ColorOptionRow(
                    option = option,
                    selected = option.value == selectedValue,
                    onClick = { onSelected(option.value) },
                )
                if (index < colorOptions.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

@Composable
private fun ColorOptionRow(
    option: ColorOptionUi,
    selected: Boolean,
    onClick: () -> Unit,
) {
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
                .height(ColorRowHeight)
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
            ColorWaterDropIcon(color = option.color)
            Text(
                text = option.label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
            )
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors =
                    RadioButtonDefaults.colors(
                        selectedColor = option.color.swatch().outline,
                    ),
            )
        }
    }
}

@Composable
private fun ColorWaterDropIcon(color: SakeColor?) {
    val swatch = color?.swatch()
    if (swatch == null) {
        Icon(
            imageVector = Icons.Filled.WaterDrop,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(ColorIconSize),
        )
        return
    }
    Box(
        modifier = Modifier.size(ColorIconSize),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.WaterDrop,
            contentDescription = null,
            tint = swatch.outline,
            modifier = Modifier.fillMaxSize(),
        )
        Icon(
            imageVector = Icons.Filled.WaterDrop,
            contentDescription = null,
            tint = swatch.fill,
            modifier = Modifier.size(ColorIconInnerSize),
        )
    }
}

private data class ColorOptionUi(
    val label: String,
    val value: String,
    val color: SakeColor,
)

private data class SakeColorSwatch(
    val fill: Color,
    val outline: Color,
)

private fun DropdownOption.toColorOptionOrNull(): ColorOptionUi? =
    value.toSakeColorOrNull()?.let { color ->
        ColorOptionUi(
            label = label,
            value = value,
            color = color,
        )
    }

private fun String?.toSakeColorOrNull(): SakeColor? =
    this?.let { value -> SakeColor.entries.firstOrNull { color -> color.name == value } }

private fun SakeColor.swatch(): SakeColorSwatch =
    when (this) {
        SakeColor.CLEAR -> SakeColorSwatch(fill = Color(0xFFF5F3EE), outline = Color(0xFFB8B8B8))
        SakeColor.PALE_YELLOW -> SakeColorSwatch(fill = Color(0xFFE7D9A0), outline = Color(0xFFA89054))
        SakeColor.YELLOW -> SakeColorSwatch(fill = Color(0xFFD8B85C), outline = Color(0xFF8D6E2A))
        SakeColor.DARK_YELLOW -> SakeColorSwatch(fill = Color(0xFFC99034), outline = Color(0xFF7A5720))
        SakeColor.BROWN -> SakeColorSwatch(fill = Color(0xFF9B6A3A), outline = Color(0xFF5F3E1E))
        SakeColor.DARK_BROWN -> SakeColorSwatch(fill = Color(0xFF6E4739), outline = Color(0xFF412720))
        SakeColor.GREEN -> SakeColorSwatch(fill = Color(0xFFC5D06E), outline = Color(0xFF6F7B33))
        SakeColor.OTHER -> SakeColorSwatch(fill = Color(0xFFD68C94), outline = Color(0xFF8A4E57))
        SakeColor.AOZAE,
        SakeColor.ALMOST_CLEAR,
        SakeColor.LIGHT_YELLOW,
        SakeColor.YAMABUKI,
        SakeColor.AMBER,
        SakeColor.ORANGE,
        SakeColor.WHITE,
        SakeColor.CLOUDY,
        -> SakeColorSwatch(fill = Color(0xFFD8B85C), outline = Color(0xFF8D6E2A))
    }
