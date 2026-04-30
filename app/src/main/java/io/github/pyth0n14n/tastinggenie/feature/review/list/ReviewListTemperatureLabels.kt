package io.github.pyth0n14n.tastinggenie.feature.review.list

import io.github.pyth0n14n.tastinggenie.domain.model.enums.Temperature

private const val REVIEW_TEMPERATURE_FORMAT = "%s（%s）"

internal fun Temperature.reviewListLabel(state: ReviewListUiState): String =
    REVIEW_TEMPERATURE_FORMAT.format(
        state.temperatureLabels[name] ?: name,
        guideTemperature,
    )

private val Temperature.guideTemperature: String
    get() =
        when (this) {
            Temperature.TOBIKIRI_KAN -> "55℃以上"
            Temperature.ATSUKAN -> "50℃"
            Temperature.JOKAN -> "45℃"
            Temperature.NURUKAN -> "40℃"
            Temperature.HITOHADA -> "35℃"
            Temperature.HINATA -> "30℃"
            Temperature.JOON -> "20℃"
            Temperature.SUZUHIE -> "15℃"
            Temperature.HANABIE -> "10℃"
            Temperature.YUKIBIE -> "5℃"
        }
