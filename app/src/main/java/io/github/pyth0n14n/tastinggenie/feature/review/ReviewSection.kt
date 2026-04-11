package io.github.pyth0n14n.tastinggenie.feature.review

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.pyth0n14n.tastinggenie.R

enum class ReviewSection(
    @StringRes val labelRes: Int,
) {
    APPEARANCE(R.string.label_review_section_appearance),
    AROMA(R.string.label_review_section_aroma),
    TASTE(R.string.label_review_section_taste),
    OTHER(R.string.label_review_section_other),
}

@Composable
fun ReviewSectionTabs(
    selectedSection: ReviewSection,
    onSectionSelected: (ReviewSection) -> Unit,
    modifier: Modifier = Modifier,
) {
    TabRow(modifier = modifier.fillMaxWidth(), selectedTabIndex = selectedSection.ordinal) {
        ReviewSection.entries.forEach { section ->
            Tab(
                selected = section == selectedSection,
                onClick = { onSectionSelected(section) },
                text = { Text(text = stringResource(section.labelRes)) },
            )
        }
    }
}
