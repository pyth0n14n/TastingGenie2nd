package io.github.pyth0n14n.tastinggenie.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.ui.common.LoadingContent

private const val SCREEN_PADDING = 16
private const val ITEM_SPACING = 12

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsScreen(
        state = state,
        onBack = onBack,
        onToggleHelpHints = viewModel::toggleHelpHints,
        onToggleImagePreview = viewModel::toggleImagePreview,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SettingsScreen(
    state: SettingsUiState,
    onBack: () -> Unit,
    onToggleHelpHints: (Boolean) -> Unit,
    onToggleImagePreview: (Boolean) -> Unit,
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
                onCheckedChange = onToggleHelpHints,
            )
            SettingSwitchRow(
                label = stringResource(R.string.setting_image_preview),
                checked = state.settings.showImagePreview,
                onCheckedChange = onToggleImagePreview,
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

@Composable
private fun SettingSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}
