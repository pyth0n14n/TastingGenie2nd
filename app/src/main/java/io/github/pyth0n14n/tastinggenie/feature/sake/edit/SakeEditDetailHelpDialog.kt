package io.github.pyth0n14n.tastinggenie.feature.sake.edit

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.pyth0n14n.tastinggenie.R

private val HelpIconSize = 32.dp
private val LeftSwipeDismissDistance = 96.dp

@Composable
internal fun SakeHelpLabel(
    label: String,
    helpMessage: SakeDetailHelpMessage,
) {
    var visibleHelp by remember { mutableStateOf<SakeDetailHelpMessage?>(null) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = label)
        IconButton(
            onClick = { visibleHelp = helpMessage },
            modifier = Modifier.size(HelpIconSize),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                contentDescription = stringResource(R.string.cd_sake_field_help, label),
            )
        }
    }
    visibleHelp?.let { help ->
        SakeDetailHelpDialog(
            helpMessage = help,
            onDismiss = { visibleHelp = null },
        )
    }
}

@Composable
private fun SakeDetailHelpDialog(
    helpMessage: SakeDetailHelpMessage,
    onDismiss: () -> Unit,
) {
    val dismissDistancePx = with(LocalDensity.current) { LeftSwipeDismissDistance.toPx() }
    var horizontalDrag by remember { mutableFloatStateOf(0f) }
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier =
            Modifier.pointerInput(onDismiss, dismissDistancePx) {
                detectHorizontalDragGestures(
                    onDragStart = { horizontalDrag = 0f },
                    onHorizontalDrag = { _, dragAmount ->
                        horizontalDrag += dragAmount
                        if (horizontalDrag <= -dismissDistancePx) {
                            onDismiss()
                        }
                    },
                    onDragEnd = { horizontalDrag = 0f },
                    onDragCancel = { horizontalDrag = 0f },
                )
            },
        title = { Text(text = stringResource(helpMessage.titleRes)) },
        text = {
            Text(
                text = stringResource(helpMessage.messageRes),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.action_ok))
            }
        },
    )
}

internal data class SakeDetailHelpMessage(
    val titleRes: Int,
    val messageRes: Int,
)

internal fun SakeTextField.toSakeDetailHelpMessage(): SakeDetailHelpMessage? =
    when (this) {
        SakeTextField.KOJI_MAI ->
            SakeDetailHelpMessage(
                R.string.sake_detail_help_koji_mai_term,
                R.string.sake_detail_help_koji_mai_message,
            )
        SakeTextField.KAKE_MAI ->
            SakeDetailHelpMessage(
                R.string.sake_detail_help_kake_mai_term,
                R.string.sake_detail_help_kake_mai_message,
            )
        SakeTextField.KOJI_POLISH,
        SakeTextField.KAKE_POLISH,
        ->
            SakeDetailHelpMessage(
                R.string.sake_detail_help_polish_term,
                R.string.sake_detail_help_polish_message,
            )
        SakeTextField.SAKE_DEGREE ->
            SakeDetailHelpMessage(
                R.string.sake_detail_help_sake_degree_term,
                R.string.sake_detail_help_sake_degree_message,
            )
        SakeTextField.ACIDITY ->
            SakeDetailHelpMessage(
                R.string.sake_detail_help_acidity_term,
                R.string.sake_detail_help_acidity_message,
            )
        SakeTextField.AMINO ->
            SakeDetailHelpMessage(
                R.string.sake_detail_help_amino_term,
                R.string.sake_detail_help_amino_message,
            )
        SakeTextField.ALCOHOL ->
            SakeDetailHelpMessage(
                R.string.sake_detail_help_alcohol_term,
                R.string.sake_detail_help_alcohol_message,
            )
        SakeTextField.YEAST ->
            SakeDetailHelpMessage(
                R.string.sake_detail_help_yeast_term,
                R.string.sake_detail_help_yeast_message,
            )
        SakeTextField.WATER ->
            SakeDetailHelpMessage(
                R.string.sake_detail_help_water_term,
                R.string.sake_detail_help_water_message,
            )
        else -> null
    }
