package io.github.pyth0n14n.tastinggenie.feature.review.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.Review
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Aroma

private const val SCREEN_PADDING = 16
private const val ITEM_SPACING = 12
private const val VISCOSITY_VERY_WEAK = 1
private const val VISCOSITY_WEAK = 2
private const val VISCOSITY_MEDIUM = 3
private const val VISCOSITY_STRONG = 4
private const val VISCOSITY_VERY_STRONG = 5

@Composable
fun ReviewDetailContent(
    review: Review,
    sakeName: String,
    labels: ReviewDetailLabels,
    modifier: Modifier = Modifier,
) {
    val textLabels =
        ReviewDetailTextLabels(
            sake = stringResource(R.string.label_sake),
            reviewDate = stringResource(R.string.label_review_date),
            bar = stringResource(R.string.label_bar),
            price = stringResource(R.string.label_price),
            volume = stringResource(R.string.label_volume),
            temperature = stringResource(R.string.label_temperature),
            color = stringResource(R.string.label_color),
            viscosity = stringResource(R.string.label_viscosity),
            intensity = stringResource(R.string.label_intensity),
            scentTop = stringResource(R.string.label_scent_top),
            scentBase = stringResource(R.string.label_scent_base),
            scentMouth = stringResource(R.string.label_scent_mouth),
            sweet = stringResource(R.string.label_sweet),
            sour = stringResource(R.string.label_sour),
            bitter = stringResource(R.string.label_bitter),
            umami = stringResource(R.string.label_umami),
            sharp = stringResource(R.string.label_sharp),
            scene = stringResource(R.string.label_scene),
            dish = stringResource(R.string.label_dish),
            comment = stringResource(R.string.label_comment),
            overallReview = stringResource(R.string.label_overall_review),
        )
    val viscosityLabels =
        mapOf(
            VISCOSITY_VERY_WEAK to stringResource(R.string.label_viscosity_1),
            VISCOSITY_WEAK to stringResource(R.string.label_viscosity_2),
            VISCOSITY_MEDIUM to stringResource(R.string.label_viscosity_3),
            VISCOSITY_STRONG to stringResource(R.string.label_viscosity_4),
            VISCOSITY_VERY_STRONG to stringResource(R.string.label_viscosity_5),
        )
    val rows =
        buildList {
            addGeneralRows(
                review = review,
                sakeName = sakeName,
                labels = labels,
                textLabels = textLabels,
                viscosityLabels = viscosityLabels,
            )
            addAromaRows(review = review, labels = labels, textLabels = textLabels)
            addTasteRows(review = review, labels = labels, textLabels = textLabels)
            addTextRows(review = review, labels = labels, textLabels = textLabels)
        }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(SCREEN_PADDING.dp),
        verticalArrangement = Arrangement.spacedBy(ITEM_SPACING.dp),
    ) {
        items(items = rows, key = { row -> row.label }) { row ->
            DetailValue(label = row.label, value = row.value)
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

private fun MutableList<DetailRow>.addGeneralRows(
    review: Review,
    sakeName: String,
    labels: ReviewDetailLabels,
    textLabels: ReviewDetailTextLabels,
    viscosityLabels: Map<Int, String>,
) {
    add(DetailRow(label = textLabels.sake, value = sakeName))
    add(DetailRow(label = textLabels.reviewDate, value = review.date.toString()))
    addIfNotBlank(label = textLabels.bar, value = review.bar)
    addIfNotBlank(label = textLabels.price, value = review.price?.toString())
    addIfNotBlank(label = textLabels.volume, value = review.volume?.toString())
    addIfNotBlank(
        label = textLabels.temperature,
        value = review.temperature?.let { labels.temperature[it.name] ?: it.name },
    )
    addIfNotBlank(
        label = textLabels.color,
        value = review.color?.let { labels.color[it.name] ?: it.name },
    )
    addIfNotBlank(
        label = textLabels.viscosity,
        value = review.viscosity?.let { viscosity -> viscosityLabels[viscosity] ?: viscosity.toString() },
    )
    addIfNotBlank(
        label = textLabels.intensity,
        value = review.intensity?.let { labels.intensity[it.name] ?: it.name },
    )
}

private fun MutableList<DetailRow>.addAromaRows(
    review: Review,
    labels: ReviewDetailLabels,
    textLabels: ReviewDetailTextLabels,
) {
    addIfNotBlank(label = textLabels.scentTop, value = review.scentTop.asDisplayText(labels.aroma))
    addIfNotBlank(label = textLabels.scentBase, value = review.scentBase.asDisplayText(labels.aroma))
    addIfNotBlank(label = textLabels.scentMouth, value = review.scentMouth.asDisplayText(labels.aroma))
}

private fun MutableList<DetailRow>.addTasteRows(
    review: Review,
    labels: ReviewDetailLabels,
    textLabels: ReviewDetailTextLabels,
) {
    addIfNotBlank(label = textLabels.sweet, value = review.sweet?.let { labels.taste[it.name] ?: it.name })
    addIfNotBlank(label = textLabels.sour, value = review.sour?.let { labels.taste[it.name] ?: it.name })
    addIfNotBlank(label = textLabels.bitter, value = review.bitter?.let { labels.taste[it.name] ?: it.name })
    addIfNotBlank(label = textLabels.umami, value = review.umami?.let { labels.taste[it.name] ?: it.name })
    addIfNotBlank(label = textLabels.sharp, value = review.sharp?.let { labels.taste[it.name] ?: it.name })
}

private fun MutableList<DetailRow>.addTextRows(
    review: Review,
    labels: ReviewDetailLabels,
    textLabels: ReviewDetailTextLabels,
) {
    addIfNotBlank(label = textLabels.scene, value = review.scene)
    addIfNotBlank(label = textLabels.dish, value = review.dish)
    addIfNotBlank(label = textLabels.comment, value = review.comment)
    addIfNotBlank(
        label = textLabels.overallReview,
        value = review.review?.let { labels.overallReview[it.name] ?: it.name },
    )
}

@Composable
private fun DetailValue(
    label: String,
    value: String,
) {
    Text(
        text = "$label: $value",
        style = MaterialTheme.typography.bodyLarge,
    )
}

private data class DetailRow(
    val label: String,
    val value: String,
)

private data class ReviewDetailTextLabels(
    val sake: String,
    val reviewDate: String,
    val bar: String,
    val price: String,
    val volume: String,
    val temperature: String,
    val color: String,
    val viscosity: String,
    val intensity: String,
    val scentTop: String,
    val scentBase: String,
    val scentMouth: String,
    val sweet: String,
    val sour: String,
    val bitter: String,
    val umami: String,
    val sharp: String,
    val scene: String,
    val dish: String,
    val comment: String,
    val overallReview: String,
)

private fun MutableList<DetailRow>.addIfNotBlank(
    label: String,
    value: String?,
) {
    value?.takeIf { it.isNotBlank() }?.let { nonBlankValue ->
        add(DetailRow(label = label, value = nonBlankValue))
    }
}

private fun List<Aroma>.asDisplayText(labels: Map<String, String>): String? =
    takeIf { it.isNotEmpty() }?.joinToString { aroma -> labels[aroma.name] ?: aroma.name }
