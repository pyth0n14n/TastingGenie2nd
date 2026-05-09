package io.github.pyth0n14n.tastinggenie.feature.sake.edit

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.pyth0n14n.tastinggenie.R

@Composable
internal fun SakeDetailHelpDialog(onDismiss: () -> Unit) {
    val entries = sakeDetailHelpEntries()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.title_sake_detail_help)) },
        text = {
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
            ) {
                items(entries, key = { entry -> entry.term }) { entry ->
                    ListItem(
                        headlineContent = {
                            Text(
                                text = entry.term,
                                style = MaterialTheme.typography.titleSmall,
                            )
                        },
                        supportingContent = {
                            Text(
                                text = entry.message,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        },
                    )
                    HorizontalDivider()
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.action_close_message))
            }
        },
    )
}

@Composable
private fun sakeDetailHelpEntries(): List<SakeDetailHelpEntry> =
    listOf(
        SakeDetailHelpEntry(
            term = stringResource(R.string.sake_detail_help_koji_mai_term),
            message = stringResource(R.string.sake_detail_help_koji_mai_message),
        ),
        SakeDetailHelpEntry(
            term = stringResource(R.string.sake_detail_help_kake_mai_term),
            message = stringResource(R.string.sake_detail_help_kake_mai_message),
        ),
        SakeDetailHelpEntry(
            term = stringResource(R.string.sake_detail_help_polish_term),
            message = stringResource(R.string.sake_detail_help_polish_message),
        ),
        SakeDetailHelpEntry(
            term = stringResource(R.string.sake_detail_help_sake_degree_term),
            message = stringResource(R.string.sake_detail_help_sake_degree_message),
        ),
        SakeDetailHelpEntry(
            term = stringResource(R.string.sake_detail_help_acidity_term),
            message = stringResource(R.string.sake_detail_help_acidity_message),
        ),
        SakeDetailHelpEntry(
            term = stringResource(R.string.sake_detail_help_amino_term),
            message = stringResource(R.string.sake_detail_help_amino_message),
        ),
        SakeDetailHelpEntry(
            term = stringResource(R.string.sake_detail_help_alcohol_term),
            message = stringResource(R.string.sake_detail_help_alcohol_message),
        ),
        SakeDetailHelpEntry(
            term = stringResource(R.string.sake_detail_help_yeast_term),
            message = stringResource(R.string.sake_detail_help_yeast_message),
        ),
        SakeDetailHelpEntry(
            term = stringResource(R.string.sake_detail_help_water_term),
            message = stringResource(R.string.sake_detail_help_water_message),
        ),
    )

private data class SakeDetailHelpEntry(
    val term: String,
    val message: String,
)
