@file:Suppress("CyclomaticComplexMethod", "LongMethod", "MagicNumber", "TooManyFunctions", "UnusedPrivateMember")

package io.github.pyth0n14n.tastinggenie.feature.review.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocalBar
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.Review
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Aroma
import io.github.pyth0n14n.tastinggenie.domain.model.enums.FlavorProfileType
import io.github.pyth0n14n.tastinggenie.domain.model.enums.FoodCompatibility
import io.github.pyth0n14n.tastinggenie.domain.model.enums.IntensityLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.OverallReview
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeColor
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SweetDryness
import io.github.pyth0n14n.tastinggenie.domain.model.enums.TasteLevel
import io.github.pyth0n14n.tastinggenie.feature.review.aftertasteLabel
import io.github.pyth0n14n.tastinggenie.ui.theme.TastingGenie2ndAndroidTheme
import io.github.pyth0n14n.tastinggenie.ui.theme.TastingSakeChipContainer
import io.github.pyth0n14n.tastinggenie.ui.theme.TastingSakeChipOutline
import io.github.pyth0n14n.tastinggenie.ui.theme.TastingTypeChipContainer
import io.github.pyth0n14n.tastinggenie.ui.theme.TastingTypeChipOutline

private val ScreenPadding = 16.dp
private val SectionSpacing = 16.dp
private val CardShape = RoundedCornerShape(8.dp)
private val SmallShape = RoundedCornerShape(6.dp)
private val ReviewChipHorizontalPadding = 10.dp
private val ReviewChipVerticalPadding = 3.dp
private const val SUMMARY_COMMENT_MAX_LENGTH = 54
private const val SCALE_STEPS = 5
private const val SWEET_DRY_STEPS = 4

@Composable
fun ReviewDetailContent(
    content: ReviewDetailContentState,
    modifier: Modifier = Modifier,
) {
    val textLabels = reviewDetailTextLabels()
    val display =
        content.toDisplayModel(
            textLabels = textLabels,
            viscosityLabels = viscosityLabels(),
        )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(SectionSpacing),
    ) {
        item(key = "summary") {
            ReviewSummaryCard(summary = display.summary)
        }
        display.highlights?.let { highlights ->
            item(key = "summary_highlights") {
                ReviewSummaryHighlights(highlights = highlights)
            }
        }
        display.sections.forEach { section ->
            item(key = section.key) {
                ExpandableSection(section = section)
            }
        }
    }
}

data class ReviewDetailLabels(
    val temperature: Map<String, String>,
    val color: Map<String, String>,
    val intensity: Map<String, String>,
    val taste: Map<String, String>,
    val overallReview: Map<String, String>,
    val aroma: Map<String, String>,
)

fun ReviewDetailUiState.toLabels(): ReviewDetailLabels =
    ReviewDetailLabels(
        temperature = temperatureLabels,
        color = colorLabels,
        intensity = intensityLabels,
        taste = tasteLabels,
        overallReview = overallReviewLabels,
        aroma = aromaLabels,
    )

data class ReviewDetailContentState(
    val review: Review,
    val sakeName: String,
    val labels: ReviewDetailLabels,
)

@Composable
private fun ReviewSummaryCard(summary: ReviewSummary) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        SummaryPrimaryColumn(
            summary = summary,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Composable
private fun SummaryPrimaryColumn(
    summary: ReviewSummary,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = summary.date,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        summary.overallReview?.let { rating ->
            SummaryRating(rating = rating)
        }
        if (summary.badges.isNotEmpty()) {
            SummaryBadgeGrid(badges = summary.badges)
        }
        summary.commentPreview?.let { comment ->
            Text(
                text = comment,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun SummaryRating(rating: RatingSummary) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(SCALE_STEPS) { index ->
            Icon(
                imageVector = if (index < rating.score) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = null,
                tint =
                    if (index < rating.score) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
                modifier = Modifier.size(30.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = rating.label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun SummaryBadgeGrid(badges: List<SummaryBadge>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        badges.chunked(2).forEach { rowBadges ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowBadges.forEach { badge ->
                    SummaryBadge(
                        badge = badge,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (rowBadges.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SummaryBadge(
    badge: SummaryBadge,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = SmallShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = badge.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = badge.text,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ReviewSummaryHighlights(highlights: SummaryHighlights) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            SummaryHighlight(
                title = stringResource(R.string.label_review_section_aroma),
                value = highlights.aroma,
                icon = Icons.Outlined.Eco,
                modifier = Modifier.weight(1f),
            )
            if (highlights.aroma != null && highlights.taste != null) {
                Box(
                    modifier =
                        Modifier
                            .height(44.dp)
                            .width(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant),
                )
            }
            SummaryHighlight(
                title = stringResource(R.string.label_review_section_taste),
                value = highlights.taste,
                icon = Icons.Outlined.WaterDrop,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SummaryHighlight(
    title: String,
    value: String?,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    if (value == null) {
        Spacer(modifier = modifier)
        return
    }
    Row(
        modifier = modifier.padding(horizontal = 8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun ExpandableSection(section: DetailSection) {
    var expanded by rememberSaveable(section.key) { mutableStateOf(section.initiallyExpanded) }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 10.dp, bottom = 10.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = section.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = section.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        contentDescription = section.title,
                    )
                }
            }
            AnimatedVisibility(visible = expanded) {
                SectionBody(section = section)
            }
        }
    }
}

@Composable
private fun SectionBody(section: DetailSection) {
    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
        section.rows.forEachIndexed { index, row ->
            when (row) {
                is DetailDisplayRow.KeyValue -> InfoKeyValueRow(label = row.label, value = row.value)
                is DetailDisplayRow.Chips ->
                    ReadonlyChipGroup(
                        label = row.label,
                        values = row.values,
                        isTopAroma = row.isTopAroma,
                    )
                is DetailDisplayRow.TasteScale -> TasteScaleRow(row = row)
                is DetailDisplayRow.TextBlock -> TextBlock(label = row.label, value = row.value)
                is DetailDisplayRow.ColorValue -> ColorInfoRow(label = row.label, value = row.value, color = row.color)
            }
            if (index != section.rows.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }
        }
    }
}

@Composable
private fun InfoKeyValueRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            modifier = Modifier.width(128.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun ReadonlyChipGroup(
    label: String,
    values: List<String>,
    isTopAroma: Boolean,
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(
            text = label,
            modifier = Modifier.width(128.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            values.forEach { value ->
                Surface(
                    color = if (isTopAroma) TastingTypeChipContainer else TastingSakeChipContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    shape = MaterialTheme.shapes.small,
                    border =
                        BorderStroke(
                            1.dp,
                            if (isTopAroma) TastingTypeChipOutline else TastingSakeChipOutline,
                        ),
                ) {
                    Text(
                        text = value,
                        modifier =
                            Modifier.padding(
                                horizontal = ReviewChipHorizontalPadding,
                                vertical = ReviewChipVerticalPadding,
                            ),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun TasteScaleRow(row: DetailDisplayRow.TasteScale) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = row.label,
            modifier = Modifier.width(92.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = row.value,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        ScaleIndicator(
            selectedIndex = row.position,
            steps = row.steps,
            modifier = Modifier.width(116.dp),
        )
    }
}

@Composable
private fun ScaleIndicator(
    selectedIndex: Int,
    steps: Int,
    modifier: Modifier = Modifier,
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.outlineVariant
    Canvas(
        modifier =
            modifier
                .height(22.dp)
                .padding(horizontal = 4.dp),
    ) {
        val centerY = size.height / 2f
        val startX = 8.dp.toPx()
        val endX = size.width - 8.dp.toPx()
        drawLine(
            color = inactiveColor,
            start = Offset(startX, centerY),
            end = Offset(endX, centerY),
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Round,
        )
        repeat(steps) { index ->
            val x = startX + (endX - startX) * index / (steps - 1).coerceAtLeast(1)
            drawCircle(
                color = if (index == selectedIndex) activeColor else Color.White,
                radius = if (index == selectedIndex) 5.dp.toPx() else 4.dp.toPx(),
                center = Offset(x, centerY),
            )
            drawCircle(
                color = activeColor,
                radius = 4.dp.toPx(),
                center = Offset(x, centerY),
                style = Stroke(width = 1.dp.toPx()),
            )
        }
    }
}

@Composable
private fun TextBlock(
    label: String,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ColorInfoRow(
    label: String,
    value: String,
    color: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.width(128.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Box(
            modifier =
                Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private data class ReviewDetailDisplay(
    val summary: ReviewSummary,
    val highlights: SummaryHighlights?,
    val sections: List<DetailSection>,
)

private data class ReviewSummary(
    val date: String,
    val overallReview: RatingSummary?,
    val badges: List<SummaryBadge>,
    val commentPreview: String?,
)

private data class RatingSummary(
    val score: Int,
    val label: String,
)

private data class SummaryBadge(
    val text: String,
    val icon: ImageVector,
)

private data class SummaryHighlights(
    val aroma: String?,
    val taste: String?,
)

private data class DetailSection(
    val key: String,
    val title: String,
    val icon: ImageVector,
    val initiallyExpanded: Boolean,
    val rows: List<DetailDisplayRow>,
)

private sealed interface DetailDisplayRow {
    val label: String

    data class KeyValue(
        override val label: String,
        val value: String,
    ) : DetailDisplayRow

    data class Chips(
        override val label: String,
        val values: List<String>,
        val isTopAroma: Boolean = false,
    ) : DetailDisplayRow

    data class TasteScale(
        override val label: String,
        val value: String,
        val position: Int,
        val steps: Int = SCALE_STEPS,
    ) : DetailDisplayRow

    data class TextBlock(
        override val label: String,
        val value: String,
    ) : DetailDisplayRow

    data class ColorValue(
        override val label: String,
        val value: String,
        val color: Color,
    ) : DetailDisplayRow
}

private data class ReviewDetailTextLabels(
    val date: String,
    val price: String,
    val volume: String,
    val temperature: String,
    val bar: String,
    val dish: String,
    val foodCompatibility: String,
    val color: String,
    val viscosity: String,
    val aromaIntensity: String,
    val aromaComplexity: String,
    val aromaMainNote: String,
    val aromaExamples: String,
    val tasteAttack: String,
    val tasteRoundness: String,
    val tasteSmoothness: String,
    val tasteTextureNote: String,
    val sweet: String,
    val sour: String,
    val bitter: String,
    val umami: String,
    val sweetDryness: String,
    val inPalateAromaIntensity: String,
    val inPalateAroma: String,
    val aftertaste: String,
    val aftertasteNote: String,
    val tasteDescription: String,
    val tasteComplexity: String,
    val individuality: String,
    val cautions: String,
    val sakeTypes: String,
    val freeComment: String,
    val overallReview: String,
)

@Composable
private fun reviewDetailTextLabels(): ReviewDetailTextLabels =
    ReviewDetailTextLabels(
        date = stringResource(R.string.label_review_date),
        price = stringResource(R.string.label_price),
        volume = stringResource(R.string.label_volume),
        temperature = stringResource(R.string.label_temperature),
        bar = stringResource(R.string.label_bar),
        dish = stringResource(R.string.label_dish),
        foodCompatibility = stringResource(R.string.label_scene),
        color = stringResource(R.string.label_color),
        viscosity = stringResource(R.string.detail_label_viscosity),
        aromaIntensity = stringResource(R.string.detail_label_strength),
        aromaComplexity = stringResource(R.string.detail_label_complexity),
        aromaMainNote = stringResource(R.string.label_aroma_main_note),
        aromaExamples = stringResource(R.string.label_scent_top),
        tasteAttack = stringResource(R.string.label_taste_attack),
        tasteRoundness = stringResource(R.string.detail_label_roundness),
        tasteSmoothness = stringResource(R.string.detail_label_smoothness),
        tasteTextureNote = stringResource(R.string.label_taste_texture_note),
        sweet = stringResource(R.string.detail_label_sweet),
        sour = stringResource(R.string.detail_label_sour),
        bitter = stringResource(R.string.detail_label_bitter),
        umami = stringResource(R.string.detail_label_umami),
        sweetDryness = stringResource(R.string.label_taste_sweet_dryness),
        inPalateAromaIntensity = stringResource(R.string.label_taste_in_palate_aroma_intensity),
        inPalateAroma = stringResource(R.string.label_scent_mouth),
        aftertaste = stringResource(R.string.label_sharp),
        aftertasteNote = stringResource(R.string.detail_label_aftertaste_note),
        tasteDescription = stringResource(R.string.detail_label_taste_description),
        tasteComplexity = stringResource(R.string.detail_label_complexity),
        individuality = stringResource(R.string.label_other_individuality),
        cautions = stringResource(R.string.label_cautions),
        sakeTypes = stringResource(R.string.label_other_sake_types),
        freeComment = stringResource(R.string.label_comment),
        overallReview = stringResource(R.string.label_overall_review),
    )

@Composable
private fun viscosityLabels(): Map<Int, String> =
    mapOf(
        1 to stringResource(R.string.detail_label_viscosity_1),
        2 to stringResource(R.string.detail_label_viscosity_2),
        3 to stringResource(R.string.detail_label_viscosity_3),
        4 to stringResource(R.string.detail_label_viscosity_4),
        5 to stringResource(R.string.detail_label_viscosity_5),
    )

private fun ReviewDetailContentState.toDisplayModel(
    textLabels: ReviewDetailTextLabels,
    viscosityLabels: Map<Int, String>,
): ReviewDetailDisplay {
    val summary = review.toSummary(labels = labels, textLabels = textLabels)
    val highlights = review.toHighlights(labels = labels)
    val sections =
        listOfNotNull(
            review.toAromaSection(labels = labels, textLabels = textLabels),
            review.toTasteSection(labels = labels, textLabels = textLabels),
            review.toBasicSection(labels = labels, textLabels = textLabels),
            review.toAppearanceSection(labels = labels, textLabels = textLabels, viscosityLabels = viscosityLabels),
            review.toMemoSection(labels = labels, textLabels = textLabels),
        )
    return ReviewDetailDisplay(summary = summary, highlights = highlights, sections = sections)
}

private fun Review.toSummary(
    labels: ReviewDetailLabels,
    textLabels: ReviewDetailTextLabels,
): ReviewSummary =
    ReviewSummary(
        date = date.toString(),
        overallReview =
            otherOverallReview?.let { review ->
                RatingSummary(
                    score = review.ordinal + 1,
                    label = labels.overallReview[review.name] ?: review.name,
                )
            },
        badges =
            listOfNotNull(
                temperature?.let { SummaryBadge(it.labelFrom(labels.temperature), Icons.Outlined.Thermostat) },
                volume?.let { SummaryBadge("${it}ml", Icons.Outlined.LocalBar) },
                foodCompatibility?.let {
                    SummaryBadge("${textLabels.foodCompatibility}: ${it.toLabel()}", Icons.Outlined.Restaurant)
                },
                otherSakeTypes.firstOrNull()?.let {
                    SummaryBadge("${textLabels.sakeTypes}: ${it.toLabel()}", Icons.Outlined.Eco)
                },
            ),
        commentPreview = otherFreeComment.trimmedOrNull()?.summaryPreview(),
    )

private fun Review.toHighlights(labels: ReviewDetailLabels): SummaryHighlights? {
    val aroma = aromaExamples.asDisplayText(labels.aroma)
    val taste =
        listOfNotNull(
            tasteSweetDryness?.toLabel(),
            tasteSourness?.labelFrom(labels.taste),
            tasteUmami?.labelFrom(labels.taste),
            tasteAftertaste?.let { aftertasteLabel(it.name) ?: it.labelFrom(labels.taste) },
        ).joinToString("・").takeIf { it.isNotBlank() }
    return SummaryHighlights(aroma = aroma, taste = taste).takeIf { it.aroma != null || it.taste != null }
}

private fun Review.toAromaSection(
    labels: ReviewDetailLabels,
    textLabels: ReviewDetailTextLabels,
): DetailSection? {
    val rows =
        buildList {
            aromaIntensity?.let {
                add(DetailDisplayRow.TasteScale(textLabels.aromaIntensity, it.labelFrom(labels.intensity), it.ordinal))
            }
            aromaComplexity?.let {
                add(DetailDisplayRow.TasteScale(textLabels.aromaComplexity, it.toLabel(), it.ordinal))
            }
            aromaExamples.asLabels(labels.aroma)?.let {
                add(DetailDisplayRow.Chips(textLabels.aromaExamples, it, isTopAroma = true))
            }
            aromaMainNote.trimmedOrNull()?.let {
                add(DetailDisplayRow.TextBlock(textLabels.aromaMainNote, it))
            }
        }
    return rows.toSection(
        key = "aroma",
        title = "香り",
        icon = Icons.Outlined.Eco,
        initiallyExpanded = true,
    )
}

private fun Review.toTasteSection(
    labels: ReviewDetailLabels,
    textLabels: ReviewDetailTextLabels,
): DetailSection? {
    val rows =
        buildList {
            tasteAttack?.let {
                add(DetailDisplayRow.TasteScale(textLabels.tasteAttack, it.toLabel(), it.ordinal))
            }
            tasteTextureRoundness?.let {
                add(DetailDisplayRow.TasteScale(textLabels.tasteRoundness, it.toLabel(), it.ordinal))
            }
            tasteTextureSmoothness?.let {
                add(DetailDisplayRow.TasteScale(textLabels.tasteSmoothness, it.toLabel(), it.ordinal))
            }
            tasteTextureNote.trimmedOrNull()?.let {
                add(DetailDisplayRow.TextBlock(textLabels.tasteTextureNote, it))
            }
            tasteSweetness?.let {
                add(DetailDisplayRow.TasteScale(textLabels.sweet, it.labelFrom(labels.taste), it.ordinal))
            }
            tasteSourness?.let {
                add(DetailDisplayRow.TasteScale(textLabels.sour, it.labelFrom(labels.taste), it.ordinal))
            }
            tasteBitterness?.let {
                add(DetailDisplayRow.TasteScale(textLabels.bitter, it.labelFrom(labels.taste), it.ordinal))
            }
            tasteUmami?.let {
                add(DetailDisplayRow.TasteScale(textLabels.umami, it.labelFrom(labels.taste), it.ordinal))
            }
            tasteSweetDryness?.let {
                add(
                    DetailDisplayRow.TasteScale(
                        label = textLabels.sweetDryness,
                        value = it.toLabel(),
                        position = it.ordinal,
                        steps = SWEET_DRY_STEPS,
                    ),
                )
            }
            tasteInPalateAromaIntensity?.let {
                add(
                    DetailDisplayRow.TasteScale(
                        textLabels.inPalateAromaIntensity,
                        it.labelFrom(labels.intensity),
                        it.ordinal,
                    ),
                )
            }
            tasteInPalateAroma.asLabels(labels.aroma)?.let {
                add(DetailDisplayRow.Chips(textLabels.inPalateAroma, it))
            }
            tasteAftertaste?.let {
                add(
                    DetailDisplayRow.TasteScale(
                        textLabels.aftertaste,
                        aftertasteLabel(it.name) ?: it.labelFrom(labels.taste),
                        it.ordinal,
                    ),
                )
            }
            tasteAftertasteNote.trimmedOrNull()?.let {
                add(DetailDisplayRow.TextBlock(textLabels.aftertasteNote, it))
            }
            tasteDescription.trimmedOrNull()?.let {
                add(DetailDisplayRow.TextBlock(textLabels.tasteDescription, it))
            }
            tasteComplexity?.let {
                add(DetailDisplayRow.TasteScale(textLabels.tasteComplexity, it.toLabel(), it.ordinal))
            }
        }
    return rows.toSection(
        key = "taste",
        title = "味",
        icon = Icons.Outlined.WaterDrop,
        initiallyExpanded = true,
    )
}

private fun Review.toBasicSection(
    labels: ReviewDetailLabels,
    textLabels: ReviewDetailTextLabels,
): DetailSection? {
    val rows =
        buildList {
            add(DetailDisplayRow.KeyValue(textLabels.date, date.toString()))
            price?.let { add(DetailDisplayRow.KeyValue(textLabels.price, "${it}円")) }
            volume?.let { add(DetailDisplayRow.KeyValue(textLabels.volume, "${it}ml")) }
            temperature?.let {
                add(DetailDisplayRow.KeyValue(textLabels.temperature, it.labelFrom(labels.temperature)))
            }
            bar.trimmedOrNull()?.let { add(DetailDisplayRow.KeyValue(textLabels.bar, it)) }
            dish.trimmedOrNull()?.let { add(DetailDisplayRow.KeyValue(textLabels.dish, it)) }
            foodCompatibility?.let {
                add(DetailDisplayRow.TasteScale(textLabels.foodCompatibility, it.toLabel(), it.ordinal))
            }
        }
    return rows.toSection(
        key = "basic",
        title = "基本情報",
        icon = Icons.Outlined.Info,
        initiallyExpanded = false,
    )
}

private fun Review.toAppearanceSection(
    labels: ReviewDetailLabels,
    textLabels: ReviewDetailTextLabels,
    viscosityLabels: Map<Int, String>,
): DetailSection? {
    val rows =
        buildList {
            appearanceColor.displayColor(labels.color, appearanceColorOther)?.let { color ->
                add(DetailDisplayRow.ColorValue(textLabels.color, color, appearanceColor.toSwatchColor()))
            }
            appearanceViscosity?.let { viscosity ->
                add(
                    DetailDisplayRow.TasteScale(
                        label = textLabels.viscosity,
                        value = viscosityLabels[viscosity] ?: viscosity.toString(),
                        position = (viscosity - 1).coerceIn(0, SCALE_STEPS - 1),
                    ),
                )
            }
        }
    return rows.toSection(
        key = "appearance",
        title = "見た目",
        icon = Icons.Outlined.Visibility,
        initiallyExpanded = false,
    )
}

private fun Review.toMemoSection(
    labels: ReviewDetailLabels,
    textLabels: ReviewDetailTextLabels,
): DetailSection? {
    val rows =
        buildList {
            otherIndividuality.trimmedOrNull()?.let { add(DetailDisplayRow.TextBlock(textLabels.individuality, it)) }
            otherCautions.trimmedOrNull()?.let { add(DetailDisplayRow.TextBlock(textLabels.cautions, it)) }
            otherFreeComment.trimmedOrNull()?.let { add(DetailDisplayRow.TextBlock(textLabels.freeComment, it)) }
            otherOverallReview?.let {
                add(DetailDisplayRow.KeyValue(textLabels.overallReview, labels.overallReview[it.name] ?: it.name))
            }
            otherSakeTypes.asFlavorProfileLabels()?.let { add(DetailDisplayRow.Chips(textLabels.sakeTypes, it)) }
        }
    return rows.toSection(
        key = "memo",
        title = "メモ・評価",
        icon = Icons.Outlined.Description,
        initiallyExpanded = false,
    )
}

private fun List<DetailDisplayRow>.toSection(
    key: String,
    title: String,
    icon: ImageVector,
    initiallyExpanded: Boolean,
): DetailSection? =
    takeIf { it.isNotEmpty() }?.let { rows ->
        DetailSection(
            key = key,
            title = title,
            icon = icon,
            initiallyExpanded = initiallyExpanded,
            rows = rows,
        )
    }

private fun Enum<*>.labelFrom(labels: Map<String, String>): String = labels[name] ?: name

private fun List<Aroma>.asDisplayText(labels: Map<String, String>): String? = asLabels(labels)?.joinToString("、")

private fun List<Aroma>.asLabels(labels: Map<String, String>): List<String>? =
    takeIf { it.isNotEmpty() }?.map { aroma -> labels[aroma.name] ?: aroma.name }

private fun List<FlavorProfileType>.asFlavorProfileLabels(): List<String>? =
    takeIf { it.isNotEmpty() }?.map { type -> type.toLabel() }

private fun SakeColor?.displayColor(
    labels: Map<String, String>,
    otherText: String?,
): String? =
    when (this) {
        null -> null
        SakeColor.OTHER -> listOfNotNull(labels[name] ?: name, otherText.trimmedOrNull()).joinToString(": ")
        else -> labels[name] ?: name
    }

private fun SakeColor?.toSwatchColor(): Color =
    when (this) {
        SakeColor.AOZAE -> Color(0xFFEAF7EE)
        SakeColor.CLEAR -> Color(0xFFFFFFFF)
        SakeColor.ALMOST_CLEAR -> Color(0xFFFFFDF0)
        SakeColor.LIGHT_YELLOW -> Color(0xFFFFF4A8)
        SakeColor.PALE_YELLOW -> Color(0xFFFFF8C7)
        SakeColor.YELLOW -> Color(0xFFFFE083)
        SakeColor.YAMABUKI -> Color(0xFFF4C14F)
        SakeColor.DARK_YELLOW -> Color(0xFFD8A736)
        SakeColor.AMBER -> Color(0xFFB87531)
        SakeColor.BROWN -> Color(0xFF8A5A36)
        SakeColor.DARK_BROWN -> Color(0xFF5D3B28)
        SakeColor.ORANGE -> Color(0xFFE58C38)
        SakeColor.WHITE -> Color(0xFFF7F5EA)
        SakeColor.CLOUDY -> Color(0xFFECE7D8)
        SakeColor.GREEN -> Color(0xFFDDEBC8)
        SakeColor.OTHER, null -> Color(0xFFE4E3D5)
    }

private fun String?.trimmedOrNull(): String? = this?.trim()?.takeIf { it.isNotBlank() }

private fun String.summaryPreview(): String =
    if (length <= SUMMARY_COMMENT_MAX_LENGTH) {
        this
    } else {
        take(SUMMARY_COMMENT_MAX_LENGTH) + "..."
    }

private fun FoodCompatibility.toLabel(): String =
    when (this) {
        FoodCompatibility.BAD -> "悪い"
        FoodCompatibility.SLIGHTLY_BAD -> "やや悪い"
        FoodCompatibility.MEDIUM -> "普通"
        FoodCompatibility.SLIGHTLY_GOOD -> "やや良い"
        FoodCompatibility.GOOD -> "良い"
    }

private fun SweetDryness.toLabel(): String =
    when (this) {
        SweetDryness.SWEET -> "甘口"
        SweetDryness.MEDIUM_SWEET -> "やや甘口"
        SweetDryness.MEDIUM_DRY -> "やや辛口"
        SweetDryness.DRY -> "辛口"
    }

private fun FlavorProfileType.toLabel(): String =
    when (this) {
        FlavorProfileType.SOUSHU -> "爽酒"
        FlavorProfileType.KUNSHU -> "薫酒"
        FlavorProfileType.JUNSHU -> "醇酒"
        FlavorProfileType.JUKUSHU -> "熟酒"
    }

@Preview(showBackground = true)
@Composable
private fun ReviewDetailContentPreview() {
    TastingGenie2ndAndroidTheme {
        ReviewDetailContent(
            content =
                ReviewDetailContentState(
                    sakeName = "asdf",
                    labels =
                        ReviewDetailLabels(
                            temperature = mapOf("HANABIE" to "花冷え"),
                            color = mapOf("PALE_YELLOW" to "淡い黄色"),
                            intensity = mapOf("STRONG" to "やや強い"),
                            taste =
                                mapOf(
                                    "WEAK" to "やや弱い",
                                    "MEDIUM" to "中程度",
                                    "STRONG" to "やや強い",
                                ),
                            overallReview = mapOf("GOOD" to "好き"),
                            aroma = mapOf("CRESSON" to "クレソン", "MITSUBA" to "三つ葉"),
                        ),
                    review =
                        Review(
                            id = 1,
                            sakeId = 1,
                            date = java.time.LocalDate.parse("2026-05-09"),
                            volume = 720,
                            otherOverallReview = OverallReview.GOOD,
                            otherFreeComment = "すっきりした立ち上がりだが、後半に旨味が伸びる。",
                            foodCompatibility = FoodCompatibility.SLIGHTLY_GOOD,
                            temperature = io.github.pyth0n14n.tastinggenie.domain.model.enums.Temperature.HANABIE,
                            aromaIntensity = IntensityLevel.STRONG,
                            aromaExamples = listOf(Aroma.CRESS, Aroma.MITSUBA),
                            tasteSweetness = TasteLevel.WEAK,
                            tasteSourness = TasteLevel.MEDIUM,
                            tasteUmami = TasteLevel.STRONG,
                            tasteAftertaste = TasteLevel.STRONG,
                            otherSakeTypes = listOf(FlavorProfileType.SOUSHU),
                        ),
                ),
        )
    }
}
