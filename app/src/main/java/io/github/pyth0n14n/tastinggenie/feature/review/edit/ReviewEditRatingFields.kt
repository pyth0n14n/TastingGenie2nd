package io.github.pyth0n14n.tastinggenie.feature.review.edit

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.res.stringResource
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.ReviewItemId
import io.github.pyth0n14n.tastinggenie.feature.review.ReviewFlavorProfileField
import io.github.pyth0n14n.tastinggenie.ui.common.DropdownOption
import io.github.pyth0n14n.tastinggenie.ui.common.StarRatingField

fun LazyListScope.steppedField(
    ui: ReviewStepFieldUi,
    options: List<DropdownOption>,
    showHelpHints: Boolean = false,
    onAction: (ReviewEditAction) -> Unit,
) {
    item {
        ReviewEditChoiceField(
            ui =
                ReviewEditChoiceFieldUi(
                    label = reviewTextResource(ui.labelRes),
                    options = options,
                    selectedValue = ui.selectedValue,
                    showHelpHints = showHelpHints,
                    helpItemId = ui.helpItemId,
                ),
            onValueChanged = { nextValue ->
                onAction(ReviewEditAction.SelectionChanged(field = ui.field, value = nextValue ?: ""))
            },
        )
    }
}

fun LazyListScope.steppedResourceField(
    ui: ReviewStepFieldUi,
    options: List<ReviewResourceOption>,
    showHelpHints: Boolean = false,
    onAction: (ReviewEditAction) -> Unit,
) {
    item {
        ReviewEditChoiceField(
            ui =
                ReviewEditChoiceFieldUi(
                    label = reviewTextResource(ui.labelRes),
                    options =
                        options.map { option ->
                            DropdownOption(
                                value = option.value,
                                label = stringResource(option.labelRes),
                            )
                        },
                    selectedValue = ui.selectedValue,
                    showHelpHints = showHelpHints,
                    helpItemId = ui.helpItemId,
                ),
            onValueChanged = { nextValue ->
                onAction(ReviewEditAction.SelectionChanged(field = ui.field, value = nextValue ?: ""))
            },
        )
    }
}

data class ReviewStepFieldUi(
    val labelRes: Int,
    val selectedValue: String?,
    val field: ReviewSelectionField,
    val helpItemId: ReviewItemId? = null,
)

fun LazyListScope.overallReviewField(
    selectedValue: String?,
    options: List<DropdownOption>,
    showHelpHints: Boolean,
    onAction: (ReviewEditAction) -> Unit,
) {
    item {
        val label = reviewTextResource(R.string.label_overall_review)
        StarRatingField(
            label = label,
            options = options,
            selectedValue = selectedValue,
            labelAction = {
                ReviewHelpAction(
                    label = label,
                    itemId = ReviewItemId.OTHER_OVERALL_REVIEW,
                    showHelpHints = showHelpHints,
                )
            },
            onValueChanged = { next ->
                onAction(
                    ReviewEditAction.SelectionChanged(
                        field = ReviewSelectionField.OVERALL_REVIEW,
                        value = next ?: "",
                    ),
                )
            },
        )
    }
}

fun LazyListScope.flavorProfileField(
    state: ReviewEditUiState,
    onAction: (ReviewEditAction) -> Unit,
) {
    item {
        ReviewFlavorProfileField(
            intensity = state.intensity,
            complexity = state.tasteComplexity,
            onSelectionChanged = { selection ->
                onAction(
                    ReviewEditAction.FlavorProfileSelected(
                        intensity = selection.intensity,
                        complexity = selection.complexity,
                    ),
                )
            },
        )
    }
}
