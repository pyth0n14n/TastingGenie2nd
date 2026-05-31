package io.github.pyth0n14n.tastinggenie.feature.review.edit

import androidx.compose.runtime.Composable
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.ui.common.GuidePage
import io.github.pyth0n14n.tastinggenie.ui.common.GuidePagerScreen

@Composable
fun TastingGuideScreen(
    launchMode: TastingGuideLaunchMode,
    onDismiss: () -> Unit,
) {
    GuidePagerScreen(
        pages = tastingGuidePages(),
        nonFinalLeadingActionLabelRes =
            when (launchMode) {
                TastingGuideLaunchMode.Initial -> R.string.action_skip
                TastingGuideLaunchMode.Manual -> R.string.action_close_message
            },
        finalTrailingActionLabelRes =
            when (launchMode) {
                TastingGuideLaunchMode.Initial -> R.string.tasting_guide_action_to_review
                TastingGuideLaunchMode.Manual -> R.string.action_done
            },
        imageContentDescriptionRes = R.string.tasting_guide_image_content_description,
        pageIndicatorDescriptionRes = R.string.tasting_guide_page_indicator,
        onDismiss = onDismiss,
    )
}

enum class TastingGuideLaunchMode {
    Initial,
    Manual,
}

private fun tastingGuidePages(): List<GuidePage> =
    listOf(
        GuidePage(
            titleResId = R.string.tasting_guide_look_title,
            messageResId = R.string.tasting_guide_look_message,
            imageResId = R.drawable.guide_tasting_look,
        ),
        GuidePage(
            titleResId = R.string.tasting_guide_smell_title,
            messageResId = R.string.tasting_guide_smell_message,
            imageResId = R.drawable.guide_tasting_smell,
        ),
        GuidePage(
            titleResId = R.string.tasting_guide_taste_title,
            messageResId = R.string.tasting_guide_taste_message,
            imageResId = R.drawable.guide_tasting_taste,
        ),
        GuidePage(
            titleResId = R.string.tasting_guide_record_title,
            messageResId = R.string.tasting_guide_record_message,
            imageResId = R.drawable.guide_tasting_record,
        ),
    )
