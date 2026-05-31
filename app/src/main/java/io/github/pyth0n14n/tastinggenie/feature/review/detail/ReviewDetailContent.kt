@file:Suppress("CyclomaticComplexMethod", "LongMethod", "MagicNumber", "TooManyFunctions", "UnusedPrivateMember")

package io.github.pyth0n14n.tastinggenie.feature.review.detail

import android.graphics.Paint
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
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.Tune
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
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
import io.github.pyth0n14n.tastinggenie.domain.model.enums.IntensityLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.OverallReview
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeColor
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SweetDryness
import io.github.pyth0n14n.tastinggenie.domain.model.enums.TasteLevel
import io.github.pyth0n14n.tastinggenie.feature.review.aftertasteLabel
import io.github.pyth0n14n.tastinggenie.feature.review.guideTemperatureLabel
import io.github.pyth0n14n.tastinggenie.ui.theme.TastingGenie2ndAndroidTheme
import io.github.pyth0n14n.tastinggenie.ui.theme.TastingSakeChipContainer
import io.github.pyth0n14n.tastinggenie.ui.theme.TastingSakeChipOutline
import io.github.pyth0n14n.tastinggenie.ui.theme.TastingTypeChipContainer
import io.github.pyth0n14n.tastinggenie.ui.theme.TastingTypeChipOutline
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

private val ScreenPadding = 16.dp
private val SectionSpacing = 16.dp
private val CardShape = RoundedCornerShape(8.dp)
private val SmallShape = RoundedCornerShape(6.dp)
private val DetailLabelWidth = 92.dp
private val DetailScaleWidth = 116.dp
private val RadarChartHeight = 260.dp
private val ReviewChipHorizontalPadding = 10.dp
private val ReviewChipVerticalPadding = 3.dp
private const val SUMMARY_COMMENT_MAX_LENGTH = 54
private const val SCALE_STEPS = 5
private const val SWEET_DRY_STEPS = 4
private const val RADAR_MAX_VALUE = 5f
private const val RADAR_START_ANGLE_DEGREES = -90.0

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
    val showReviewSoundness: Boolean = false,
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = badge.text,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                badge.supportingText?.let { supportingText ->
                    Text(
                        text = supportingText,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun ReviewSummaryHighlights(highlights: SummaryHighlights) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Tune,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "香り・味のサマリ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            if (highlights.hasChartData) {
                AromaTasteRadarChart(
                    axes = highlights.axes,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(RadarChartHeight),
                )
            }
            SummaryAromaChipLine(
                label = stringResource(R.string.label_scent_top),
                values = highlights.topAromaLabels,
                isTopAroma = true,
            )
            SummaryAromaChipLine(
                label = stringResource(R.string.label_scent_mouth),
                values = highlights.inPalateAromaLabels,
                isTopAroma = false,
            )
        }
    }
}

@Composable
private fun AromaTasteRadarChart(
    axes: List<RadarAxis>,
    modifier: Modifier = Modifier,
) {
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val axisColor = MaterialTheme.colorScheme.outline
    val valueColor = MaterialTheme.colorScheme.primary
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val labelTextSize = MaterialTheme.typography.labelSmall.fontSize
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val labelSpace = 36.dp.toPx()
        val radius = (min(size.width, size.height) / 2f - labelSpace).coerceAtLeast(0f)
        val textPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = labelColor.toArgb()
                textAlign = Paint.Align.CENTER
                textSize = labelTextSize.toPx()
            }

        repeat(SCALE_STEPS) { levelIndex ->
            val levelRadius = radius * (levelIndex + 1) / SCALE_STEPS
            drawRadarPolygon(
                axes = axes,
                center = center,
                radius = levelRadius,
                color = gridColor,
                strokeWidth = 1.dp.toPx(),
            )
        }
        axes.forEachIndexed { index, _ ->
            drawLine(
                color = axisColor,
                start = center,
                end = radarPoint(center = center, radius = radius, index = index, count = axes.size),
                strokeWidth = 1.dp.toPx(),
            )
        }

        val valuePath = Path()
        val valuePoints =
            axes.mapIndexed { index, axis ->
                val scale = (axis.value ?: 0).coerceIn(0, SCALE_STEPS) / RADAR_MAX_VALUE
                radarPoint(center = center, radius = radius * scale, index = index, count = axes.size)
            }
        valuePoints.forEachIndexed { index, point ->
            if (index == 0) {
                valuePath.moveTo(point.x, point.y)
            } else {
                valuePath.lineTo(point.x, point.y)
            }
        }
        valuePath.close()
        drawPath(path = valuePath, color = valueColor.copy(alpha = 0.18f))
        drawPath(path = valuePath, color = valueColor, style = Stroke(width = 2.dp.toPx()))
        valuePoints.forEachIndexed { index, point ->
            if ((axes[index].value ?: 0) > 0) {
                drawCircle(color = valueColor, radius = 3.5.dp.toPx(), center = point)
            }
        }

        axes.forEachIndexed { index, axis ->
            val labelPoint =
                radarPoint(
                    center = center,
                    radius = radius + 22.dp.toPx(),
                    index = index,
                    count = axes.size,
                )
            drawContext.canvas.nativeCanvas.drawText(
                axis.label,
                labelPoint.x,
                labelPoint.y + textPaint.textSize / 3f,
                textPaint,
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRadarPolygon(
    axes: List<RadarAxis>,
    center: Offset,
    radius: Float,
    color: Color,
    strokeWidth: Float,
) {
    val path = Path()
    axes.indices.forEach { index ->
        val point = radarPoint(center = center, radius = radius, index = index, count = axes.size)
        if (index == 0) {
            path.moveTo(point.x, point.y)
        } else {
            path.lineTo(point.x, point.y)
        }
    }
    path.close()
    drawPath(path = path, color = color, style = Stroke(width = strokeWidth))
}

private fun radarPoint(
    center: Offset,
    radius: Float,
    index: Int,
    count: Int,
): Offset {
    val angle = (RADAR_START_ANGLE_DEGREES + 360.0 * index / count) * PI / 180.0
    return Offset(
        x = center.x + radius * cos(angle).toFloat(),
        y = center.y + radius * sin(angle).toFloat(),
    )
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun SummaryAromaChipLine(
    label: String,
    values: List<String>,
    isTopAroma: Boolean,
) {
    if (values.isEmpty()) {
        return
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            values.forEach { value ->
                ReadonlyChip(value = value, isTopAroma = isTopAroma)
            }
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
            modifier = Modifier.width(DetailLabelWidth),
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
            modifier = Modifier.width(DetailLabelWidth),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            values.forEach { value ->
                ReadonlyChip(value = value, isTopAroma = isTopAroma)
            }
        }
    }
}

@Composable
private fun ReadonlyChip(
    value: String,
    isTopAroma: Boolean,
) {
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
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
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
            modifier = Modifier.width(DetailLabelWidth),
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
            modifier = Modifier.width(DetailScaleWidth),
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            modifier = Modifier.width(DetailLabelWidth),
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
            modifier = Modifier.width(DetailLabelWidth),
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
    val supportingText: String? = null,
)

private data class SummaryHighlights(
    val axes: List<RadarAxis>,
    val topAromaLabels: List<String>,
    val inPalateAromaLabels: List<String>,
) {
    val hasChartData: Boolean = axes.any { it.value != null }
    val hasLabels: Boolean = topAromaLabels.isNotEmpty() || inPalateAromaLabels.isNotEmpty()
}

private data class RadarAxis(
    val label: String,
    val value: Int?,
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
    val color: String,
    val viscosity: String,
    val soundness: String,
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
        color = stringResource(R.string.label_color),
        viscosity = stringResource(R.string.detail_label_viscosity),
        soundness = stringResource(R.string.label_soundness),
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
            review.toAromaSection(
                labels = labels,
                textLabels = textLabels,
                showReviewSoundness = showReviewSoundness,
            ),
            review.toTasteSection(
                labels = labels,
                textLabels = textLabels,
                showReviewSoundness = showReviewSoundness,
            ),
            review.toBasicSection(labels = labels, textLabels = textLabels),
            review.toAppearanceSection(
                labels = labels,
                textLabels = textLabels,
                viscosityLabels = viscosityLabels,
                showReviewSoundness = showReviewSoundness,
            ),
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
                temperature?.let {
                    SummaryBadge(it.summaryLabel(labels.temperature), Icons.Outlined.Thermostat)
                },
                bar.trimmedOrNull()?.let { SummaryBadge(it, Icons.Outlined.LocalBar) },
                pricePer100mlText()?.let { SummaryBadge(it, Icons.Outlined.Payments) },
                tasteSweetDryness?.let { SummaryBadge(it.toLabel(), Icons.Outlined.WaterDrop) },
                otherSakeTypes.firstOrNull()?.let {
                    SummaryBadge(it.toLabel(), Icons.Outlined.Eco)
                },
            ),
        commentPreview = otherIndividuality.trimmedOrNull()?.summaryPreview(),
    )

private fun Review.toHighlights(labels: ReviewDetailLabels): SummaryHighlights? {
    val highlights =
        SummaryHighlights(
            axes =
                listOf(
                    RadarAxis("香りの強さ", aromaIntensity?.scaleValue()),
                    RadarAxis("アタック", tasteAttack?.scaleValue()),
                    RadarAxis("甘味", tasteSweetness?.scaleValue()),
                    RadarAxis("酸味", tasteSourness?.scaleValue()),
                    RadarAxis("旨味", tasteUmami?.scaleValue()),
                    RadarAxis("余韻", tasteAftertaste?.scaleValue()),
                    RadarAxis("味の複雑性", tasteComplexity?.scaleValue()),
                ),
            topAromaLabels = aromaExamples.asLabels(labels.aroma).orEmpty(),
            inPalateAromaLabels = tasteInPalateAroma.asLabels(labels.aroma).orEmpty(),
        )
    return highlights.takeIf { it.hasChartData || it.hasLabels }
}

private fun Review.toAromaSection(
    labels: ReviewDetailLabels,
    textLabels: ReviewDetailTextLabels,
    showReviewSoundness: Boolean,
): DetailSection? {
    val rows =
        buildList {
            if (showReviewSoundness) {
                aromaSoundness?.let { soundness ->
                    add(DetailDisplayRow.KeyValue(textLabels.soundness, soundness.toLabel()))
                }
            }
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
        initiallyExpanded = false,
    )
}

private fun Review.toTasteSection(
    labels: ReviewDetailLabels,
    textLabels: ReviewDetailTextLabels,
    showReviewSoundness: Boolean,
): DetailSection? {
    val rows =
        buildList {
            if (showReviewSoundness) {
                tasteSoundness?.let { soundness ->
                    add(DetailDisplayRow.KeyValue(textLabels.soundness, soundness.toLabel()))
                }
            }
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
        initiallyExpanded = false,
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
    showReviewSoundness: Boolean,
): DetailSection? {
    val rows =
        buildList {
            if (showReviewSoundness) {
                appearanceSoundness?.let { soundness ->
                    add(DetailDisplayRow.KeyValue(textLabels.soundness, soundness.toLabel()))
                }
            }
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
                add(
                    DetailDisplayRow.TasteScale(
                        textLabels.overallReview,
                        labels.overallReview[it.name] ?: it.name,
                        it.ordinal,
                    ),
                )
            }
            otherSakeTypes.asFlavorProfileText()?.let { add(DetailDisplayRow.KeyValue(textLabels.sakeTypes, it)) }
        }
    return rows.toSection(
        key = "memo",
        title = "評価・特記事項",
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

private fun Enum<*>.scaleValue(): Int = ordinal + 1

private fun io.github.pyth0n14n.tastinggenie.domain.model.enums.Temperature.summaryLabel(
    labels: Map<String, String>,
): String = "${labelFrom(labels)}（${guideTemperatureLabel()}）"

private fun Review.pricePer100mlText(): String? {
    val totalPrice = price
    val totalVolume = volume?.takeIf { it > 0 }
    return if (totalPrice != null && totalVolume != null) {
        val unitPrice = (totalPrice.toDouble() / totalVolume * 100).roundToInt()
        "$unitPrice 円 / 100ml"
    } else {
        null
    }
}

private fun List<Aroma>.asDisplayText(labels: Map<String, String>): String? = asLabels(labels)?.joinToString("、")

private fun List<Aroma>.asLabels(labels: Map<String, String>): List<String>? =
    takeIf { it.isNotEmpty() }?.map { aroma -> labels[aroma.name] ?: aroma.name }

private fun List<FlavorProfileType>.asFlavorProfileText(): String? =
    takeIf { it.isNotEmpty() }?.joinToString { type -> type.toLabel() }

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

private fun SweetDryness.toLabel(): String =
    when (this) {
        SweetDryness.SWEET -> "甘口"
        SweetDryness.MEDIUM_SWEET -> "やや甘口"
        SweetDryness.MEDIUM -> "中程度"
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
