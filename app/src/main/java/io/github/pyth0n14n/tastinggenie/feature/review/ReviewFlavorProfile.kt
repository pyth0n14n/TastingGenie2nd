package io.github.pyth0n14n.tastinggenie.feature.review

import io.github.pyth0n14n.tastinggenie.domain.model.enums.ComplexityLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.IntensityLevel

enum class FlavorProfileType {
    SOUSHU,
    KUNSHU,
    JUNSHU,
    JUKUSHU,
}

data class FlavorProfileSelection(
    val intensity: IntensityLevel,
    val complexity: ComplexityLevel,
)

data class FlavorProfileCell(
    val xIndex: Int,
    val yIndex: Int,
)

internal val flavorProfileIntensityLevels: List<IntensityLevel> = IntensityLevel.entries

internal val flavorProfileComplexityLevels: List<ComplexityLevel> =
    listOf(
        ComplexityLevel.COMPLEX,
        ComplexityLevel.SLIGHTLY_COMPLEX,
        ComplexityLevel.MEDIUM,
        ComplexityLevel.SLIGHTLY_SIMPLE,
        ComplexityLevel.SIMPLE,
    )

fun deriveFlavorProfileType(
    intensity: IntensityLevel?,
    complexity: ComplexityLevel?,
): FlavorProfileType? {
    if (intensity == null || complexity == null) {
        return null
    }
    val isAromaHigh = intensity == IntensityLevel.STRONG || intensity == IntensityLevel.VERY_STRONG
    val isTasteHigh =
        complexity == ComplexityLevel.SLIGHTLY_COMPLEX || complexity == ComplexityLevel.COMPLEX
    return when {
        !isAromaHigh && !isTasteHigh -> FlavorProfileType.SOUSHU
        isAromaHigh && !isTasteHigh -> FlavorProfileType.KUNSHU
        !isAromaHigh && isTasteHigh -> FlavorProfileType.JUNSHU
        else -> FlavorProfileType.JUKUSHU
    }
}

fun selectedFlavorProfileCell(
    intensity: IntensityLevel?,
    complexity: ComplexityLevel?,
): FlavorProfileCell? {
    val xIndex = flavorProfileIntensityLevels.indexOf(intensity)
    val yIndex = flavorProfileComplexityLevels.indexOf(complexity)
    if (xIndex < 0 || yIndex < 0) {
        return null
    }
    return FlavorProfileCell(xIndex = xIndex, yIndex = yIndex)
}

fun flavorProfileSelectionAt(
    xIndex: Int,
    yIndex: Int,
): FlavorProfileSelection? {
    val intensity = flavorProfileIntensityLevels.getOrNull(xIndex)
    val complexity = flavorProfileComplexityLevels.getOrNull(yIndex)
    return if (intensity != null && complexity != null) {
        FlavorProfileSelection(
            intensity = intensity,
            complexity = complexity,
        )
    } else {
        null
    }
}
