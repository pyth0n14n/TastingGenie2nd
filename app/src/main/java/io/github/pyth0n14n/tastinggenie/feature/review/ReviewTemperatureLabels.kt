@file:Suppress("MagicNumber")

package io.github.pyth0n14n.tastinggenie.feature.review

import androidx.compose.ui.graphics.Color
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Temperature

fun Temperature.guideTemperatureLabel(): String =
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

fun Temperature.temperatureAccentColor(): Color =
    when (this) {
        Temperature.YUKIBIE,
        Temperature.HANABIE,
        Temperature.SUZUHIE,
        -> Color(0xFF3B82F6)

        Temperature.JOON -> Color(0xFF9A7B52)

        Temperature.HINATA,
        Temperature.HITOHADA,
        Temperature.NURUKAN,
        -> Color(0xFFE97835)

        Temperature.JOKAN,
        Temperature.ATSUKAN,
        Temperature.TOBIKIRI_KAN,
        -> Color(0xFFD83B2D)
    }
