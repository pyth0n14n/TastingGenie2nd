package io.github.pyth0n14n.tastinggenie.feature.review.edit

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.res.stringResource
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.feature.review.ReviewFlavorProfileField
import io.github.pyth0n14n.tastinggenie.ui.common.DropdownOption
import io.github.pyth0n14n.tastinggenie.ui.common.StarRatingField

fun LazyListScope.steppedField(
    labelRes: Int,
    selectedValue: String?,
    options: List<DropdownOption>,
    field: ReviewSelectionField,
    onAction: (ReviewEditAction) -> Unit,
) {
    item {
        ReviewEditChoiceField(
            label = reviewTextResource(labelRes),
            options = options,
            selectedValue = selectedValue,
            onValueChanged = { nextValue ->
                onAction(ReviewEditAction.SelectionChanged(field = field, value = nextValue ?: ""))
            },
        )
    }
}

fun LazyListScope.steppedResourceField(
    labelRes: Int,
    selectedValue: String?,
    options: List<ReviewResourceOption>,
    field: ReviewSelectionField,
    onAction: (ReviewEditAction) -> Unit,
) {
    item {
        ReviewEditChoiceField(
            label = reviewTextResource(labelRes),
            options =
                options.map { option ->
                    DropdownOption(
                        value = option.value,
                        label = stringResource(option.labelRes),
                    )
                },
            selectedValue = selectedValue,
            onValueChanged = { nextValue ->
                onAction(ReviewEditAction.SelectionChanged(field = field, value = nextValue ?: ""))
            },
        )
    }
}

fun LazyListScope.overallReviewField(
    selectedValue: String?,
    options: List<DropdownOption>,
    onAction: (ReviewEditAction) -> Unit,
) {
    item {
        StarRatingField(
            label = reviewTextResource(R.string.label_overall_review),
            options = options,
            selectedValue = selectedValue,
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
