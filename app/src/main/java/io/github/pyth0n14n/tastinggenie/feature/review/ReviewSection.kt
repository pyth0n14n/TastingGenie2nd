package io.github.pyth0n14n.tastinggenie.feature.review

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.pyth0n14n.tastinggenie.R

enum class ReviewSection(
    @param:StringRes val labelRes: Int,
) {
    BASIC(R.string.label_review_section_basic),
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
            val label = stringResource(section.labelRes)
            Tab(
                selected = section == selectedSection,
                onClick = { onSectionSelected(section) },
                modifier = Modifier.semantics { contentDescription = label },
                icon = {
                    when (section) {
                        ReviewSection.BASIC ->
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                            )
                        ReviewSection.APPEARANCE ->
                            Icon(
                                painter = painterResource(R.drawable.eye),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                        ReviewSection.AROMA ->
                            Icon(
                                painter = painterResource(R.drawable.nose),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                        ReviewSection.TASTE ->
                            Icon(
                                painter = painterResource(R.drawable.tongue),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                        ReviewSection.OTHER ->
                            Icon(
                                imageVector = Icons.Outlined.Description,
                                contentDescription = null,
                            )
                    }
                },
            )
        }
    }
}
