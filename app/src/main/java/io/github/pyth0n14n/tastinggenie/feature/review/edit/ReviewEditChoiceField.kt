package io.github.pyth0n14n.tastinggenie.feature.review.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.ui.common.DropdownOption

private const val CLEAR_BUTTON_MIN_WIDTH = 72
private val ChoiceButtonHeight = 32.dp
private val ChoiceButtonCorner = 12.dp

@Composable
fun ReviewEditChoiceField(
    label: String,
    options: List<DropdownOption>,
    selectedValue: String?,
    onValueChanged: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
            )
            TextButton(
                onClick = { onValueChanged(null) },
                enabled = selectedValue != null,
                modifier = Modifier.width(CLEAR_BUTTON_MIN_WIDTH.dp),
            ) {
                Text(text = stringResource(R.string.action_clear))
            }
        }
        ConnectedChoiceButtons(
            options = options,
            selectedValue = selectedValue,
            onSelected = onValueChanged,
        )
    }
}

@Composable
private fun ConnectedChoiceButtons(
    options: List<DropdownOption>,
    selectedValue: String?,
    onSelected: (String?) -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, option ->
            val selected = option.value == selectedValue
            Surface(
                onClick = { onSelected(option.value) },
                modifier =
                    Modifier
                        .weight(1f)
                        .height(ChoiceButtonHeight),
                shape = choiceShape(index = index, count = options.size),
                color =
                    if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    },
                contentColor =
                    if (selected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    },
            ) {
                Text(
                    text = option.label,
                    modifier = Modifier.padding(horizontal = 2.dp, vertical = 7.dp),
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private fun choiceShape(
    index: Int,
    count: Int,
) = RoundedCornerShape(
    topStart = if (index == 0) ChoiceButtonCorner else 0.dp,
    bottomStart = if (index == 0) ChoiceButtonCorner else 0.dp,
    topEnd = if (index == count - 1) ChoiceButtonCorner else 0.dp,
    bottomEnd = if (index == count - 1) ChoiceButtonCorner else 0.dp,
)
