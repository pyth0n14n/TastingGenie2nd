package io.github.pyth0n14n.tastinggenie.feature.review.list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.Review
import io.github.pyth0n14n.tastinggenie.ui.common.OverflowAction
import io.github.pyth0n14n.tastinggenie.ui.common.OverflowActionsMenu
import io.github.pyth0n14n.tastinggenie.ui.theme.TastingSakeChipContainer
import io.github.pyth0n14n.tastinggenie.ui.theme.TastingSakeChipOutline
import io.github.pyth0n14n.tastinggenie.ui.theme.TastingTypeChipContainer
import io.github.pyth0n14n.tastinggenie.ui.theme.TastingTypeChipOutline

private val TimelineWidth = 18.dp
private val TimelineDotRadius = 6.dp
private val TimelineDotCenterY = TimelineDotRadius
private val ReviewRowVerticalPadding = 8.dp
private val ReviewChipHorizontalPadding = 10.dp
private val ReviewChipVerticalPadding = 3.dp
private val ReviewHeaderTemperatureSpacing = 20.dp
private val ReviewOverflowEndPadding = 2.dp
private val ReviewRatingIconHeight = 18.dp
private val ReviewTemperatureIconHeight = 16.dp
private const val MAX_TOP_AROMA_CHIPS = 2
private const val MAX_IN_PALATE_AROMA_CHIPS = 2
private const val MAX_REVIEW_CHIPS = 4

@Composable
internal fun ReviewTimelineItem(
    review: Review,
    state: ReviewListUiState,
    actions: ReviewListActionHandlers,
    onDeleteRequest: () -> Unit,
) {
    val timelineColor = MaterialTheme.colorScheme.onSurface
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .clickable { actions.onOpenReview(review.id) },
    ) {
        ReviewTimelineMarker(color = timelineColor)
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(start = 6.dp, top = ReviewRowVerticalPadding, bottom = ReviewRowVerticalPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ReviewTimelineHeader(
                review = review,
                state = state,
                actions = actions,
                onDeleteRequest = onDeleteRequest,
            )
            ReviewComment(review = review)
            ReviewFeatureChips(review = review, state = state)
        }
    }
}

@Composable
private fun ReviewTimelineHeader(
    review: Review,
    state: ReviewListUiState,
    actions: ReviewListActionHandlers,
    onDeleteRequest: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = review.date.toString().replace("-", "/"),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.width(ReviewHeaderTemperatureSpacing))
        review.temperature?.let { temperature ->
            Icon(
                imageVector = Icons.Filled.Thermostat,
                contentDescription = null,
                modifier = Modifier.height(ReviewTemperatureIconHeight),
            )
            Text(
                text = temperature.reviewListLabel(state),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        ReviewRating(review = review)
        ReviewListItemActions(
            hasSakeImage = state.hasSakeImage,
            onOpenImage = { actions.onOpenImage(review.id) },
            onDeleteRequest = onDeleteRequest,
        )
    }
}

@Composable
private fun ReviewTimelineMarker(color: Color) {
    Canvas(
        modifier =
            Modifier
                .width(TimelineWidth)
                .fillMaxHeight(),
    ) {
        val centerX = TimelineDotRadius.toPx()
        val centerY = TimelineDotCenterY.toPx()
        val dotRadius = TimelineDotRadius.toPx()
        drawLine(
            color = color.copy(alpha = 0.22f),
            start = Offset(centerX, centerY + dotRadius),
            end = Offset(centerX, size.height),
            strokeWidth = 1.dp.toPx(),
        )
        drawCircle(
            color = color,
            radius = dotRadius,
            center = Offset(centerX, centerY),
        )
    }
}

@Composable
private fun ReviewRating(review: Review) {
    review.otherOverallReview?.let { rating ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(end = ReviewOverflowEndPadding),
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.height(ReviewRatingIconHeight),
            )
            Text(
                text = "${rating.ordinal + 1}.00",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun ReviewComment(review: Review) {
    val comment = review.otherFreeComment ?: review.otherCautions ?: review.otherIndividuality
    if (comment != null) {
        Text(
            text = comment,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ReviewFeatureChips(
    review: Review,
    state: ReviewListUiState,
) {
    val chips = review.listChipLabels(state)
    if (chips.isEmpty()) {
        return
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        chips.forEach { chip ->
            ReviewFeatureChip(
                chip = chip,
            )
        }
    }
}

@Composable
private fun ReviewFeatureChip(chip: ReviewChipUi) {
    Surface(
        color = if (chip.isTopAroma) TastingTypeChipContainer else TastingSakeChipContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, if (chip.isTopAroma) TastingTypeChipOutline else TastingSakeChipOutline),
    ) {
        Text(
            text = chip.label,
            style = MaterialTheme.typography.bodyMedium,
            modifier =
                Modifier.padding(
                    horizontal = ReviewChipHorizontalPadding,
                    vertical = ReviewChipVerticalPadding,
                ),
        )
    }
}

private fun Review.listChipLabels(state: ReviewListUiState): List<ReviewChipUi> =
    buildList {
        aromaExamples.take(MAX_TOP_AROMA_CHIPS).forEach { aroma ->
            add(ReviewChipUi(label = state.aromaLabels[aroma.name] ?: aroma.name, isTopAroma = true))
        }
        tasteInPalateAroma.take(MAX_IN_PALATE_AROMA_CHIPS).forEach { aroma ->
            add(ReviewChipUi(label = state.aromaLabels[aroma.name] ?: aroma.name, isTopAroma = false))
        }
    }.take(MAX_REVIEW_CHIPS)

private data class ReviewChipUi(
    val label: String,
    val isTopAroma: Boolean,
)

@Composable
private fun ReviewListItemActions(
    hasSakeImage: Boolean,
    onOpenImage: () -> Unit,
    onDeleteRequest: () -> Unit,
) {
    OverflowActionsMenu(
        actions =
            buildList {
                if (hasSakeImage) {
                    add(OverflowAction(labelRes = R.string.action_view_image, onClick = onOpenImage))
                }
                add(OverflowAction(labelRes = R.string.action_delete, onClick = onDeleteRequest))
            },
    )
}
