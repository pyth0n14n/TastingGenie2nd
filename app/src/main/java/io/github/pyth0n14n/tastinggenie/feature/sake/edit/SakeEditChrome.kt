package io.github.pyth0n14n.tastinggenie.feature.sake.edit

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.ui.common.ConfirmationDialog

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SakeEditTopBar(onBack: () -> Unit) {
    TopAppBar(
        title = { Text(stringResource(R.string.screen_sake_edit)) },
        navigationIcon = {
            TextButton(onClick = onBack) {
                Text(stringResource(R.string.action_back))
            }
        },
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
