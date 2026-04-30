package io.github.pyth0n14n.tastinggenie.feature.review.list

import io.github.pyth0n14n.tastinggenie.domain.model.enums.Temperature
import io.github.pyth0n14n.tastinggenie.feature.review.guideTemperatureLabel

private const val REVIEW_TEMPERATURE_FORMAT = "%s（%s）"

internal fun Temperature.reviewListLabel(state: ReviewListUiState): String =
    REVIEW_TEMPERATURE_FORMAT.format(
        state.temperatureLabels[name] ?: name,
        guideTemperatureLabel(),
    )
