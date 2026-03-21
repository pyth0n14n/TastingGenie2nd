package io.github.pyth0n14n.tastinggenie.feature.settings

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.ui.common.LoadingContent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.charset.StandardCharsets

private const val SCREEN_PADDING = 16
private const val ITEM_SPACING = 12
private const val EXPORT_FILE_NAME = "tastinggenie-backup.json"

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current.applicationContext
    val exportLauncher =
        rememberLauncherForActivityResult(CreateDocument("application/json")) { uri ->
            if (uri == null) {
                return@rememberLauncherForActivityResult
            }
            viewModel.exportBackup { rawJson ->
                runRouteTransferCatching { writeBackupJson(context, uri, rawJson) }
            }
        }
    val importLauncher =
        rememberLauncherForActivityResult(OpenDocument()) { uri ->
            if (uri == null) {
                return@rememberLauncherForActivityResult
            }
            viewModel.importBackup {
                runRouteTransferCatching { readBackupJson(context, uri) }
            }
        }
    SettingsScreen(
        state = state,
        onBack = onBack,
        actions =
            SettingsScreenActions(
                onToggleHelpHints = viewModel::toggleHelpHints,
                onToggleImagePreview = viewModel::toggleImagePreview,
                onExportJson = { if (!state.isProcessingTransfer) exportLauncher.launch(EXPORT_FILE_NAME) },
                onImportJson = { if (!state.isProcessingTransfer) importLauncher.launch(arrayOf("application/json")) },
                onDismissMessage = viewModel::clearMessage,
            ),
    )
}

private suspend fun <T> runRouteTransferCatching(block: suspend () -> T): Result<T> =
    runCatching { block() }.onFailure { throwable ->
        if (throwable is CancellationException) {
            throw throwable
        }
    }

private suspend fun writeBackupJson(
    context: Context,
    uri: Uri,
    rawJson: String,
) = withContext(Dispatchers.IO) {
    // Export success depends on both JSON generation and URI write completion.
    val outputStream =
        checkNotNull(context.contentResolver.openOutputStream(uri, "wt")) {
            "Failed to open output stream for export"
        }
    outputStream.writer(StandardCharsets.UTF_8).use { writer ->
        writer.write(rawJson)
    }
}

private suspend fun readBackupJson(
    context: Context,
    uri: Uri,
): String =
    withContext(Dispatchers.IO) {
        val inputStream =
            checkNotNull(context.contentResolver.openInputStream(uri)) {
                "Failed to open input stream for import"
            }
        inputStream.reader(StandardCharsets.UTF_8).use { reader ->
            reader.readText()
        }
    }

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SettingsScreen(
    state: SettingsUiState,
    onBack: () -> Unit,
    actions: SettingsScreenActions,
) {
    if (state.isLoading) {
        LoadingContent()
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.screen_settings)) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(stringResource(R.string.action_back))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(SCREEN_PADDING.dp),
            verticalArrangement = Arrangement.spacedBy(ITEM_SPACING.dp),
        ) {
            SettingSwitchRow(
                label = stringResource(R.string.setting_help_hint),
                checked = state.settings.showHelpHints,
                onCheckedChange = actions.onToggleHelpHints,
                enabled = !state.isProcessingTransfer,
            )
            SettingSwitchRow(
                label = stringResource(R.string.setting_image_preview),
                checked = state.settings.showImagePreview,
                onCheckedChange = actions.onToggleImagePreview,
                enabled = !state.isProcessingTransfer,
            )
            TransferActions(
                isProcessingTransfer = state.isProcessingTransfer,
                messageResId = state.messageResId,
                onExportJson = actions.onExportJson,
                onImportJson = actions.onImportJson,
                onDismissMessage = actions.onDismissMessage,
            )
            state.error?.let { error ->
                Text(
                    text = stringResource(error.messageResId),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

data class SettingsScreenActions(
    val onToggleHelpHints: (Boolean) -> Unit,
    val onToggleImagePreview: (Boolean) -> Unit,
    val onExportJson: () -> Unit,
    val onImportJson: () -> Unit,
    val onDismissMessage: () -> Unit,
)

@Composable
private fun TransferActions(
    isProcessingTransfer: Boolean,
    messageResId: Int?,
    onExportJson: () -> Unit,
    onImportJson: () -> Unit,
    onDismissMessage: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ITEM_SPACING.dp),
    ) {
        Button(
            onClick = onExportJson,
            enabled = !isProcessingTransfer,
        ) {
            Text(stringResource(R.string.action_export_json))
        }
        Button(
            onClick = onImportJson,
            enabled = !isProcessingTransfer,
        ) {
            Text(stringResource(R.string.action_import_json))
        }
    }
    if (isProcessingTransfer) {
        Text(text = stringResource(R.string.message_processing_transfer))
    }
    messageResId?.let { feedbackResId ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = stringResource(feedbackResId))
            TextButton(onClick = onDismissMessage) {
                Text(stringResource(R.string.action_close_message))
            }
        }
    }
}

@Composable
private fun SettingSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
        )
    }
}
