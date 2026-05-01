package io.github.pyth0n14n.tastinggenie.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.github.pyth0n14n.tastinggenie.R

@Composable
fun DiscardDraftDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    ConfirmationDialog(
        title = stringResource(R.string.title_discard_draft),
        message = stringResource(R.string.message_discard_draft),
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}
