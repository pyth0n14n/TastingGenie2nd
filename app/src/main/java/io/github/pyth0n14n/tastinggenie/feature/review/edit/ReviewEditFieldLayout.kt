package io.github.pyth0n14n.tastinggenie.feature.review.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.pyth0n14n.tastinggenie.domain.model.ReviewItemId

internal val ReviewEditSubheaderTopSpacing = 12.dp
internal val ReviewEditSubheaderAfterGroupTopSpacing = 0.dp
internal val ReviewEditFirstSubheaderTopSpacing = 0.dp
internal val ReviewEditSubheaderBottomSpacing = 4.dp
internal val ReviewEditFieldSpacing = 16.dp
internal val ReviewEditGroupBottomSpacing = 24.dp
internal val ReviewEditLabelInputSpacing = 8.dp

@Composable
internal fun ReviewSectionSubheader(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier.fillMaxWidth(),
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
@Suppress("LongParameterList")
internal fun ReviewFieldGroup(
    heading: String,
    modifier: Modifier = Modifier,
    topSpacing: Dp = ReviewEditSubheaderTopSpacing,
    showHelpHints: Boolean = false,
    helpItemId: ReviewItemId? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = topSpacing, bottom = ReviewEditGroupBottomSpacing),
    ) {
        Column {
            ReviewHelpLabel(
                label = heading,
                itemId = helpItemId,
                showHelpHints = showHelpHints,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(ReviewEditSubheaderBottomSpacing))
        }
        Column(verticalArrangement = Arrangement.spacedBy(ReviewEditFieldSpacing)) {
            content()
        }
    }
}
