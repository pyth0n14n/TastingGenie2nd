@file:Suppress("TooManyFunctions")

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
import io.github.pyth0n14n.tastinggenie.feature.review.ReviewFlavorProfileField
import io.github.pyth0n14n.tastinggenie.feature.review.ReviewSection
import io.github.pyth0n14n.tastinggenie.feature.review.aftertasteLabel

private const val SCREEN_PADDING = 16
private const val ITEM_SPACING = 12
private const val VISCOSITY_VERY_WEAK = 1
private const val VISCOSITY_WEAK = 2
private const val VISCOSITY_MEDIUM = 3
private const val VISCOSITY_STRONG = 4
private const val VISCOSITY_VERY_STRONG = 5

@Composable
@Suppress("LongMethod")
fun ReviewDetailContent(
    content: ReviewDetailContentState,
    modifier: Modifier = Modifier,
) {
    val textLabels =
        ReviewDetailTextLabels(
            reviewDate = stringResource(R.string.label_review_date),
            bar = stringResource(R.string.label_bar),
            price = stringResource(R.string.label_price),
            volume = stringResource(R.string.label_volume),
            temperature = stringResource(R.string.label_temperature),
            soundness = stringResource(R.string.label_soundness),
            color = stringResource(R.string.label_color),
            viscosity = stringResource(R.string.label_viscosity),
            intensity = stringResource(R.string.label_intensity),
            aromaMainNote = stringResource(R.string.label_aroma_main_note),
            aromaComplexity = stringResource(R.string.label_aroma_complexity),
            aromaExamples = stringResource(R.string.label_scent_top),
            tasteInPalateAroma = stringResource(R.string.label_scent_mouth),
            tasteAttack = stringResource(R.string.label_taste_attack),
            tasteTextureRoundness = stringResource(R.string.label_taste_texture_roundness),
            tasteTextureSmoothness = stringResource(R.string.label_taste_texture_smoothness),
            tasteMainNote = stringResource(R.string.label_taste_main_note),
            sweet = stringResource(R.string.label_sweet),
            sour = stringResource(R.string.label_sour),
            bitter = stringResource(R.string.label_bitter),
            umami = stringResource(R.string.label_umami),
            aftertaste = stringResource(R.string.label_sharp),
            tasteComplexity = stringResource(R.string.label_taste_complexity),
            individuality = stringResource(R.string.label_other_individuality),
            scene = stringResource(R.string.label_scene),
            dish = stringResource(R.string.label_dish),
            cautions = stringResource(R.string.label_cautions),
            freeComment = stringResource(R.string.label_comment),
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
    val sectionRows =
        reviewDetailSectionRows(
            review = content.review,
            labels = content.labels,
            textLabels = textLabels,
            viscosityLabels = viscosityLabels,
            selectedSection = content.selectedSection,
        )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(SCREEN_PADDING.dp),
        verticalArrangement = Arrangement.spacedBy(ITEM_SPACING.dp),
    ) {
        if (content.selectedSection == ReviewSection.OTHER) {
            item(key = "flavor_profile_grid", contentType = "flavor_profile") {
                ReviewFlavorProfileField(
                    intensity = content.review.aromaIntensity,
                    complexity = content.review.tasteComplexity,
                    onSelectionChanged = null,
                )
            }
        }
        items(items = sectionRows, key = { row -> row.key }) { row ->
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

data class ReviewDetailContentState(
    val review: Review,
    val sakeName: String,
    val labels: ReviewDetailLabels,
    val selectedSection: ReviewSection,
)

private fun MutableList<DetailRow>.addBasicRows(
    review: Review,
    labels: ReviewDetailLabels,
    textLabels: ReviewDetailTextLabels,
) {
    add(DetailRow(key = "reviewDate", label = textLabels.reviewDate, value = review.date.toString()))
    addIfNotBlank(label = textLabels.bar, value = review.bar)
    addIfNotBlank(label = textLabels.price, value = review.price?.toString())
    addIfNotBlank(label = textLabels.volume, value = review.volume?.toString())
    addIfNotBlank(
        label = textLabels.temperature,
        value = review.temperature?.let { labels.temperature[it.name] ?: it.name },
    )
    addIfNotBlank(
        label = textLabels.scene,
        value = review.scene,
    )
    addIfNotBlank(
        label = textLabels.dish,
        value = review.dish,
    )
}

private fun MutableList<DetailRow>.addAppearanceRows(
    review: Review,
    labels: ReviewDetailLabels,
    textLabels: ReviewDetailTextLabels,
    viscosityLabels: Map<Int, String>,
) {
    addIfNotBlank(
        key = "appearanceSoundness",
        label = textLabels.soundness,
        value = review.appearanceSoundness.toLabel(),
    )
    addIfNotBlank(
        label = textLabels.color,
        value = review.appearanceColor?.let { labels.color[it.name] ?: it.name },
    )
    addIfNotBlank(
        label = textLabels.viscosity,
        value = review.appearanceViscosity?.let { viscosity -> viscosityLabels[viscosity] ?: viscosity.toString() },
    )
}

private fun MutableList<DetailRow>.addAromaRows(
    review: Review,
    labels: ReviewDetailLabels,
    textLabels: ReviewDetailTextLabels,
) {
    addIfNotBlank(
        key = "aromaSoundness",
        label = textLabels.soundness,
        value = review.aromaSoundness.toLabel(),
    )
    addIfNotBlank(
        label = textLabels.intensity,
        value = review.aromaIntensity?.let { labels.intensity[it.name] ?: it.name },
    )
    addIfNotBlank(label = textLabels.aromaExamples, value = review.aromaExamples.asDisplayText(labels.aroma))
    addIfNotBlank(label = textLabels.aromaMainNote, value = review.aromaMainNote)
    addIfNotBlank(label = textLabels.aromaComplexity, value = review.aromaComplexity?.toLabel())
}

private fun MutableList<DetailRow>.addTasteRows(
    review: Review,
    labels: ReviewDetailLabels,
    textLabels: ReviewDetailTextLabels,
) {
    addIfNotBlank(
        key = "tasteSoundness",
        label = textLabels.soundness,
        value = review.tasteSoundness.toLabel(),
    )
    addIfNotBlank(label = textLabels.tasteAttack, value = review.tasteAttack?.toLabel())
    addIfNotBlank(label = textLabels.tasteTextureRoundness, value = review.tasteTextureRoundness?.toLabel())
    addIfNotBlank(label = textLabels.tasteTextureSmoothness, value = review.tasteTextureSmoothness?.toLabel())
    addIfNotBlank(label = textLabels.tasteMainNote, value = review.tasteMainNote)
    addIfNotBlank(
        label = textLabels.tasteInPalateAroma,
        value = review.tasteInPalateAroma.asDisplayText(labels.aroma),
    )
    addIfNotBlank(
        label = textLabels.sweet,
        value = review.tasteSweetness?.let { labels.taste[it.name] ?: it.name },
    )
    addIfNotBlank(label = textLabels.sour, value = review.tasteSourness?.let { labels.taste[it.name] ?: it.name })
    addIfNotBlank(
        label = textLabels.bitter,
        value = review.tasteBitterness?.let { labels.taste[it.name] ?: it.name },
    )
    addIfNotBlank(label = textLabels.umami, value = review.tasteUmami?.let { labels.taste[it.name] ?: it.name })
    addIfNotBlank(
        label = textLabels.aftertaste,
        value = review.tasteAftertaste?.let { aftertasteLabel(it.name) ?: labels.taste[it.name] ?: it.name },
    )
    addIfNotBlank(label = textLabels.tasteComplexity, value = review.tasteComplexity?.toLabel())
}

private fun MutableList<DetailRow>.addOtherRows(
    review: Review,
    textLabels: ReviewDetailTextLabels,
    labels: ReviewDetailLabels,
) {
    addIfNotBlank(label = textLabels.individuality, value = review.otherIndividuality)
    addIfNotBlank(label = textLabels.cautions, value = review.otherCautions)
    addIfNotBlank(label = textLabels.freeComment, value = review.otherFreeComment)
    addIfNotBlank(
        label = textLabels.overallReview,
        value = review.otherOverallReview?.let { labels.overallReview[it.name] ?: it.name },
    )
}

private fun reviewDetailSectionRows(
    review: Review,
    labels: ReviewDetailLabels,
    textLabels: ReviewDetailTextLabels,
    viscosityLabels: Map<Int, String>,
    selectedSection: ReviewSection,
): List<DetailRow> =
    buildList {
        when (selectedSection) {
            ReviewSection.BASIC ->
                addBasicRows(
                    review = review,
                    labels = labels,
                    textLabels = textLabels,
                )
            ReviewSection.APPEARANCE ->
                addAppearanceRows(
                    review = review,
                    labels = labels,
                    textLabels = textLabels,
                    viscosityLabels = viscosityLabels,
                )
            ReviewSection.AROMA -> addAromaRows(review = review, labels = labels, textLabels = textLabels)
            ReviewSection.TASTE -> addTasteRows(review = review, labels = labels, textLabels = textLabels)
            ReviewSection.OTHER -> addOtherRows(review = review, textLabels = textLabels, labels = labels)
        }
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
    val key: String,
    val label: String,
    val value: String,
)

private data class ReviewDetailTextLabels(
    val reviewDate: String,
    val bar: String,
    val price: String,
    val volume: String,
    val temperature: String,
    val soundness: String,
    val color: String,
    val viscosity: String,
    val intensity: String,
    val aromaMainNote: String,
    val aromaComplexity: String,
    val aromaExamples: String,
    val tasteInPalateAroma: String,
    val tasteAttack: String,
    val tasteTextureRoundness: String,
    val tasteTextureSmoothness: String,
    val tasteMainNote: String,
    val sweet: String,
    val sour: String,
    val bitter: String,
    val umami: String,
    val aftertaste: String,
    val tasteComplexity: String,
    val individuality: String,
    val scene: String,
    val dish: String,
    val cautions: String,
    val freeComment: String,
    val overallReview: String,
)

private fun MutableList<DetailRow>.addIfNotBlank(
    label: String,
    value: String?,
    key: String = label,
) {
    value?.takeIf { it.isNotBlank() }?.let { nonBlankValue ->
        add(DetailRow(key = key, label = label, value = nonBlankValue))
    }
}

private fun List<Aroma>.asDisplayText(labels: Map<String, String>): String? =
    takeIf { it.isNotEmpty() }?.joinToString { aroma -> labels[aroma.name] ?: aroma.name }
