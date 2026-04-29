package io.github.pyth0n14n.tastinggenie.feature.sake.edit

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.ui.common.ConfirmationDialog
import io.github.pyth0n14n.tastinggenie.ui.common.TastingTopAppBar

@Composable
fun SakeEditTopBar(onBack: () -> Unit) {
    TastingTopAppBar(
        title = stringResource(R.string.screen_sake_edit),
        onBack = onBack,
    )
}

@Composable
fun DeleteSakeImageDialog(
    isVisible: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (!isVisible) {
        return
    }
    ConfirmationDialog(
        title = stringResource(R.string.title_delete_sake_image),
        message = stringResource(R.string.message_confirm_delete_sake_image),
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}
