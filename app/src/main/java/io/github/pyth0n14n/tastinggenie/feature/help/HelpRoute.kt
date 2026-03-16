package io.github.pyth0n14n.tastinggenie.feature.help

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.pyth0n14n.tastinggenie.R

private const val SCREEN_PADDING = 16
private const val ITEM_SPACING = 12

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HelpRoute(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.screen_help)) },
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
            Text(
                text = stringResource(R.string.help_intro),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(text = stringResource(R.string.help_color))
            Text(text = stringResource(R.string.help_aroma))
            Text(text = stringResource(R.string.help_taste))
        }
    }
}
