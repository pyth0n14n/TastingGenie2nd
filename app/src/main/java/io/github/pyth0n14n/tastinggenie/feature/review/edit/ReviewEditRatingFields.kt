package io.github.pyth0n14n.tastinggenie.feature.review.edit

import androidx.compose.foundation.lazy.LazyListScope
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.ui.common.DiscreteSliderField
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
        DiscreteSliderField(
            label = reviewTextResource(labelRes),
            options = options,
            selectedValue = selectedValue,
            onValueChanged = { next ->
                onAction(ReviewEditAction.SelectionChanged(field = field, value = next ?: ""))
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
