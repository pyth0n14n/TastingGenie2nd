package io.github.pyth0n14n.tastinggenie.feature.review.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.ReviewItemId
import io.github.pyth0n14n.tastinggenie.ui.common.DropdownOption

private const val CLEAR_BUTTON_MIN_WIDTH = 72
private val ClearButtonHeight = 32.dp
private val ChoiceButtonHeight = 32.dp
private val ChoiceButtonCorner = 12.dp

@Composable
fun ReviewEditChoiceField(
    ui: ReviewEditChoiceFieldUi,
    onValueChanged: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ReviewEditLabelInputSpacing),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ReviewHelpLabel(
                label = ui.label,
                itemId = ui.helpItemId,
                showHelpHints = ui.showHelpHints,
                modifier = Modifier.weight(1f),
            )
            TextButton(
                onClick = { onValueChanged(null) },
                enabled = ui.selectedValue != null,
                modifier =
                    Modifier
                        .width(CLEAR_BUTTON_MIN_WIDTH.dp)
                        .height(ClearButtonHeight),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
            ) {
                Text(
                    text = stringResource(R.string.action_clear),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        ConnectedChoiceButtons(
            options = ui.options,
            selectedValue = ui.selectedValue,
            onSelected = onValueChanged,
        )
    }
}

data class ReviewEditChoiceFieldUi(
    val label: String,
    val options: List<DropdownOption>,
    val selectedValue: String?,
    val showHelpHints: Boolean,
    val helpItemId: ReviewItemId? = null,
)

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
