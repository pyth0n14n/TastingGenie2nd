@file:Suppress("TooManyFunctions")

package io.github.pyth0n14n.tastinggenie.feature.settings

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.ui.common.LoadingContent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.charset.StandardCharsets

private const val SCREEN_PADDING = 16
private const val SECTION_SPACING = 10
private const val SETTINGS_ROW_MIN_HEIGHT = 56
private const val SETTINGS_CARD_CORNER = 8
private const val REQUIRED_VERSION_PARTS = 3
private const val EXPORT_FILE_NAME = "tastinggenie-backup.json"
private const val DEFAULT_APP_VERSION = "1.0"
private const val VERSION_PART_PADDING = "0"

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    onOpenGlossary: () -> Unit,
    viewModel: SettingsViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activityContext = LocalContext.current
    val context = activityContext.applicationContext
    DisposableEffect(activityContext, viewModel) {
        viewModel.setSettingsVisible(visible = true)
        onDispose {
            if (!activityContext.isChangingConfigurations()) {
                viewModel.setSettingsVisible(visible = false)
            }
        }
    }
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
                onToggleReviewSoundness = viewModel::toggleReviewSoundness,
                onToggleAutoDeleteUnusedImages = viewModel::toggleAutoDeleteUnusedImages,
                onCleanupUnusedImages = viewModel::cleanupUnusedImages,
                onExportJson = { if (!state.isProcessingTransfer) exportLauncher.launch(EXPORT_FILE_NAME) },
                onImportJson = { if (!state.isProcessingTransfer) importLauncher.launch(arrayOf("application/json")) },
                onOpenGlossary = onOpenGlossary,
                onDismissMessage = viewModel::clearTransferFeedback,
            ),
    )
}

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

private fun Context.isChangingConfigurations(): Boolean = findActivity()?.isChangingConfigurations == true

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
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarMessage = state.messageResId?.let { messageResId -> stringResource(messageResId) }
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message = message)
            actions.onDismissMessage()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.screen_settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
            )
        },
    ) { padding ->
        SettingsContent(
            state = state,
            actions = actions,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(SCREEN_PADDING.dp),
        )
    }
}

@Composable
@Suppress("LongMethod")
private fun SettingsContent(
    state: SettingsUiState,
    actions: SettingsScreenActions,
    modifier: Modifier = Modifier,
) {
    val versionText = currentAppVersionText()
    var isRestoreConfirmOpen by remember { mutableStateOf(false) }
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(SECTION_SPACING.dp),
    ) {
        item {
            SettingsSection(title = stringResource(R.string.settings_section_display_operation)) {
                SettingSwitchRow(
                    label = stringResource(R.string.setting_help_hint_short),
                    checked = state.settings.showHelpHints,
                    onCheckedChange = actions.onToggleHelpHints,
                    enabled = !state.isProcessingTransfer,
                )
            }
        }
        item {
            SettingsSection(title = stringResource(R.string.settings_section_sake_review)) {
                SettingSwitchRow(
                    label = stringResource(R.string.setting_review_soundness_hide),
                    description = stringResource(R.string.setting_review_soundness_hide_description),
                    checked = !state.settings.showReviewSoundness,
                    onCheckedChange = { hideSoundness ->
                        actions.onToggleReviewSoundness(!hideSoundness)
                    },
                    enabled = !state.isProcessingTransfer,
                )
                SettingsDivider()
                SettingSwitchRow(
                    label = stringResource(R.string.setting_auto_delete_unused_images_short),
                    description = stringResource(R.string.setting_auto_delete_unused_images_description),
                    checked = state.settings.autoDeleteUnusedImages,
                    onCheckedChange = actions.onToggleAutoDeleteUnusedImages,
                    enabled = !state.isProcessingTransfer,
                )
            }
        }
        item {
            Button(
                onClick = actions.onCleanupUnusedImages,
                enabled = !state.isProcessingTransfer,
            ) {
                Text(stringResource(R.string.action_cleanup_unused_images))
            }
        }
        item {
            SettingsSection(title = stringResource(R.string.settings_section_data)) {
                SettingNavigationRow(
                    label = stringResource(R.string.setting_backup_export),
                    onClick = actions.onExportJson,
                    enabled = !state.isProcessingTransfer,
                )
                SettingsDivider()
                SettingNavigationRow(
                    label = stringResource(R.string.setting_backup_import),
                    onClick = { isRestoreConfirmOpen = true },
                    enabled = !state.isProcessingTransfer,
                )
            }
        }
        item {
            SettingsSection(title = stringResource(R.string.settings_section_other)) {
                SettingNavigationRow(
                    label = stringResource(R.string.setting_glossary),
                    onClick = actions.onOpenGlossary,
                )
                SettingsDivider()
                SettingNavigationRow(
                    label = stringResource(R.string.setting_about_app),
                    value = stringResource(R.string.setting_about_app_version, versionText),
                    showArrow = false,
                )
            }
        }
        item {
            if (state.isProcessingTransfer) {
                Text(text = stringResource(R.string.message_processing_transfer))
            }
        }
        state.error?.let { error ->
            item {
                Text(
                    text = stringResource(error.messageResId),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
    if (isRestoreConfirmOpen) {
        RestoreConfirmDialog(
            onDismiss = { isRestoreConfirmOpen = false },
            onRestore = {
                isRestoreConfirmOpen = false
                actions.onImportJson()
            },
        )
    }
}

data class SettingsScreenActions(
    val onToggleHelpHints: (Boolean) -> Unit,
    val onToggleReviewSoundness: (Boolean) -> Unit,
    val onToggleAutoDeleteUnusedImages: (Boolean) -> Unit,
    val onCleanupUnusedImages: () -> Unit,
    val onExportJson: () -> Unit,
    val onImportJson: () -> Unit,
    val onOpenGlossary: () -> Unit,
    val onDismissMessage: () -> Unit,
)

@Composable
private fun RestoreConfirmDialog(
    onDismiss: () -> Unit,
    onRestore: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.title_restore_backup)) },
        text = { Text(stringResource(R.string.message_restore_backup)) },
        confirmButton = {
            TextButton(onClick = onRestore) {
                Text(stringResource(R.string.action_restore_backup))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(SETTINGS_CARD_CORNER.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Column(modifier = Modifier.fillMaxWidth(), content = content)
        }
    }
}

@Composable
private fun SettingSwitchRow(
    label: String,
    description: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = SETTINGS_ROW_MIN_HEIGHT.dp)
                .padding(start = 8.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
        )
    }
}

@Composable
private fun SettingNavigationRow(
    label: String,
    value: String? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    showArrow: Boolean = true,
) {
    val rowModifier =
        Modifier
            .fillMaxWidth()
            .heightIn(min = SETTINGS_ROW_MIN_HEIGHT.dp)
            .then(
                if (onClick != null && enabled) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                },
            ).padding(horizontal = 8.dp)
    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        value?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (showArrow) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun SettingsDivider() {
    Box(modifier = Modifier.padding(horizontal = 8.dp)) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun currentAppVersionText(): String {
    val context = LocalContext.current
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val rawVersion = packageInfo.versionName.orEmpty().ifBlank { DEFAULT_APP_VERSION }
    val parts = rawVersion.split(".")
    if (parts.size >= REQUIRED_VERSION_PARTS) {
        return "v$rawVersion"
    }
    val padding =
        List(REQUIRED_VERSION_PARTS - parts.size) {
            VERSION_PART_PADDING
        }
    val normalized = (parts + padding).joinToString(".")
    return "v$normalized"
}
