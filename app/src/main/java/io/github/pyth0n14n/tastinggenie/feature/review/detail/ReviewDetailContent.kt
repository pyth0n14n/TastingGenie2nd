@file:Suppress("TooManyFunctions")

package io.github.pyth0n14n.tastinggenie.feature.review.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import io.github.pyth0n14n.tastinggenie.domain.model.enums.FlavorProfileType
import io.github.pyth0n14n.tastinggenie.domain.model.enums.FoodCompatibility
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeColor
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SweetDryness
import io.github.pyth0n14n.tastinggenie.feature.review.ReviewFlavorProfileField
import io.github.pyth0n14n.tastinggenie.feature.review.ReviewSection
import io.github.pyth0n14n.tastinggenie.feature.review.aftertasteLabel

private const val SCREEN_PADDING = 16
private const val ITEM_SPACING = 16
private const val GROUP_SPACING = 24
private const val GROUP_HEADING_BOTTOM_SPACE = 8
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
            viscosity = stringResource(R.string.detail_label_viscosity),
            aromaTopHeading = stringResource(R.string.detail_heading_aroma_top),
            aromaComplexity = stringResource(R.string.detail_label_complexity),
            aromaExamples = stringResource(R.string.detail_label_examples),
            aromaStrength = stringResource(R.string.detail_label_strength),
            tasteAttack = stringResource(R.string.label_taste_attack),
            tasteTextureHeading = stringResource(R.string.detail_heading_texture),
            tasteTextureRoundness = stringResource(R.string.detail_label_roundness),
            tasteTextureSmoothness = stringResource(R.string.detail_label_smoothness),
            tasteSpecificHeading = stringResource(R.string.detail_heading_taste),
            tasteSweetDryness = stringResource(R.string.label_taste_sweet_dryness),
            tasteInPalateAromaHeading = stringResource(R.string.detail_heading_in_palate_aroma),
            tasteInPalateAromaIntensity = stringResource(R.string.detail_label_strength),
            tasteInPalateAroma = stringResource(R.string.detail_label_examples),
            sweet = stringResource(R.string.detail_label_sweet),
            sour = stringResource(R.string.detail_label_sour),
            bitter = stringResource(R.string.detail_label_bitter),
            umami = stringResource(R.string.detail_label_umami),
            aftertaste = stringResource(R.string.label_sharp),
            aftertasteNote = stringResource(R.string.detail_label_free_note),
            tasteComplexity = stringResource(R.string.detail_label_complexity),
            individuality = stringResource(R.string.label_other_individuality),
            sakeTypes = stringResource(R.string.label_other_sake_types),
            scene = stringResource(R.string.label_scene),
            dish = stringResource(R.string.label_dish),
            cautions = stringResource(R.string.label_cautions),
            freeComment = stringResource(R.string.label_comment),
            overallReview = stringResource(R.string.label_overall_review),
        )
    val viscosityLabels =
        mapOf(
            VISCOSITY_VERY_WEAK to stringResource(R.string.detail_label_viscosity_1),
            VISCOSITY_WEAK to stringResource(R.string.detail_label_viscosity_2),
            VISCOSITY_MEDIUM to stringResource(R.string.detail_label_viscosity_3),
            VISCOSITY_STRONG to stringResource(R.string.detail_label_viscosity_4),
            VISCOSITY_VERY_STRONG to stringResource(R.string.detail_label_viscosity_5),
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
        verticalArrangement = Arrangement.spacedBy(0.dp),
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
            if (row.children.isEmpty()) {
                DetailValue(label = row.label, value = row.value.orEmpty())
            } else {
                DetailGroup(row = row)
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
        value = review.foodCompatibility?.toLabel(),
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
        value = review.appearanceColor.displayColor(labels.color, review.appearanceColorOther),
    )
    addIfNotBlank(
        label = textLabels.viscosity,
        value = review.appearanceViscosity?.let { viscosity -> viscosityLabels[viscosity] ?: viscosity.toString() },
    )
}

private fun SakeColor?.displayColor(
    labels: Map<String, String>,
    otherText: String?,
): String? =
    when (this) {
        null -> null
        SakeColor.OTHER ->
            listOfNotNull(labels[name] ?: name, otherText?.takeIf { it.isNotBlank() })
                .joinToString(": ")
        else -> labels[name] ?: name
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
    addGroupHeadingIfNotEmpty(key = "aromaTopHeading", label = textLabels.aromaTopHeading) {
        addIfNotBlank(
            key = "aromaTopStrength",
            label = textLabels.aromaStrength,
            value = review.aromaIntensity?.let { labels.intensity[it.name] ?: it.name },
        )
        addIfNotBlank(
            key = "aromaTopExamples",
            label = textLabels.aromaExamples,
            value = review.aromaExamples.asDisplayText(labels.aroma),
        )
    }
    addIfNotBlank(
        key = "aromaComplexity",
        label = textLabels.aromaComplexity,
        value = review.aromaComplexity?.toLabel(),
    )
}

private fun MutableList<DetailRow>.addTasteRows(
    review: Review,
    labels: ReviewDetailLabels,
    textLabels: ReviewDetailTextLabels,
) {
    addIfNotBlank(label = textLabels.tasteAttack, value = review.tasteAttack?.toLabel())
    addTasteTextureRows(review = review, textLabels = textLabels)
    addSpecificTasteRows(review = review, labels = labels, textLabels = textLabels)
    addIfNotBlank(
        key = "tasteSweetDryness",
        label = textLabels.tasteSweetDryness,
        value = review.tasteSweetDryness?.toLabel(),
    )
    addInPalateAromaRows(review = review, labels = labels, textLabels = textLabels)
    addIfNotBlank(
        key = "tasteAftertaste",
        label = textLabels.aftertaste,
        value =
            review.tasteAftertaste?.let {
                aftertasteLabel(it.name) ?: labels.taste[it.name] ?: it.name
            },
    )
    addIfNotBlank(
        key = "tasteAftertasteNote",
        label = textLabels.aftertasteNote,
        value = review.tasteAftertasteNote,
    )
    addIfNotBlank(
        key = "tasteComplexity",
        label = textLabels.tasteComplexity,
        value = review.tasteComplexity?.toLabel(),
    )
}

private fun MutableList<DetailRow>.addTasteTextureRows(
    review: Review,
    textLabels: ReviewDetailTextLabels,
) {
    addGroupHeadingIfNotEmpty(key = "tasteTextureHeading", label = textLabels.tasteTextureHeading) {
        addIfNotBlank(
            key = "tasteTextureRoundness",
            label = textLabels.tasteTextureRoundness,
            value = review.tasteTextureRoundness?.toLabel(),
        )
        addIfNotBlank(
            key = "tasteTextureSmoothness",
            label = textLabels.tasteTextureSmoothness,
            value = review.tasteTextureSmoothness?.toLabel(),
        )
    }
}

private fun MutableList<DetailRow>.addSpecificTasteRows(
    review: Review,
    labels: ReviewDetailLabels,
    textLabels: ReviewDetailTextLabels,
) {
    addGroupHeadingIfNotEmpty(key = "tasteSpecificHeading", label = textLabels.tasteSpecificHeading) {
        addIfNotBlank(
            key = "tasteSweetness",
            label = textLabels.sweet,
            value = review.tasteSweetness?.let { labels.taste[it.name] ?: it.name },
        )
        addIfNotBlank(
            key = "tasteSourness",
            label = textLabels.sour,
            value = review.tasteSourness?.let { labels.taste[it.name] ?: it.name },
        )
        addIfNotBlank(
            key = "tasteBitterness",
            label = textLabels.bitter,
            value = review.tasteBitterness?.let { labels.taste[it.name] ?: it.name },
        )
        addIfNotBlank(
            key = "tasteUmami",
            label = textLabels.umami,
            value = review.tasteUmami?.let { labels.taste[it.name] ?: it.name },
        )
    }
}

private fun MutableList<DetailRow>.addInPalateAromaRows(
    review: Review,
    labels: ReviewDetailLabels,
    textLabels: ReviewDetailTextLabels,
) {
    addGroupHeadingIfNotEmpty(key = "tasteInPalateAromaHeading", label = textLabels.tasteInPalateAromaHeading) {
        addIfNotBlank(
            key = "tasteInPalateAromaIntensity",
            label = textLabels.tasteInPalateAromaIntensity,
            value = review.tasteInPalateAromaIntensity?.let { labels.intensity[it.name] ?: it.name },
        )
        addIfNotBlank(
            key = "tasteInPalateAroma",
            label = textLabels.tasteInPalateAroma,
            value = review.tasteInPalateAroma.asDisplayText(labels.aroma),
        )
    }
}

private fun MutableList<DetailRow>.addOtherRows(
    review: Review,
    textLabels: ReviewDetailTextLabels,
    labels: ReviewDetailLabels,
) {
    addIfNotBlank(label = textLabels.individuality, value = review.otherIndividuality)
    addIfNotBlank(label = textLabels.cautions, value = review.otherCautions)
    addIfNotBlank(label = textLabels.sakeTypes, value = review.otherSakeTypes.asDisplayText())
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
private fun DetailGroup(row: DetailRow) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = GROUP_SPACING.dp),
    ) {
        Column {
            Text(
                text = row.label,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(GROUP_HEADING_BOTTOM_SPACE.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(ITEM_SPACING.dp)) {
            row.children.forEach { child ->
                DetailValue(label = child.label, value = child.value.orEmpty(), includeBottomPadding = false)
            }
        }
    }
}

@Composable
private fun DetailValue(
    label: String,
    value: String,
    includeBottomPadding: Boolean,
) {
    val bottomPadding =
        if (includeBottomPadding) {
            ITEM_SPACING.dp
        } else {
            0.dp
        }
    Text(
        modifier = Modifier.padding(bottom = bottomPadding),
        text = "$label: $value",
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun DetailValue(
    label: String,
    value: String,
) {
    DetailValue(label = label, value = value, includeBottomPadding = true)
}

private data class DetailRow(
    val key: String,
    val label: String,
    val value: String?,
    val children: List<DetailRow> = emptyList(),
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
    val aromaTopHeading: String,
    val aromaComplexity: String,
    val aromaExamples: String,
    val aromaStrength: String,
    val tasteInPalateAroma: String,
    val tasteAttack: String,
    val tasteTextureHeading: String,
    val tasteTextureRoundness: String,
    val tasteTextureSmoothness: String,
    val tasteSpecificHeading: String,
    val tasteSweetDryness: String,
    val tasteInPalateAromaHeading: String,
    val tasteInPalateAromaIntensity: String,
    val sweet: String,
    val sour: String,
    val bitter: String,
    val umami: String,
    val aftertaste: String,
    val aftertasteNote: String,
    val tasteComplexity: String,
    val individuality: String,
    val sakeTypes: String,
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

private inline fun MutableList<DetailRow>.addGroupHeadingIfNotEmpty(
    key: String,
    label: String,
    addRows: MutableList<DetailRow>.() -> Unit,
) {
    val rows = mutableListOf<DetailRow>()
    rows.addRows()
    if (rows.isNotEmpty()) {
        add(DetailRow(key = key, label = label, value = null, children = rows))
    }
}

private fun List<Aroma>.asDisplayText(labels: Map<String, String>): String? =
    takeIf { it.isNotEmpty() }?.joinToString { aroma -> labels[aroma.name] ?: aroma.name }

private fun List<FlavorProfileType>.asDisplayText(): String? =
    takeIf { it.isNotEmpty() }?.joinToString { type -> type.toLabel() }

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
