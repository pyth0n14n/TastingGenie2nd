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

private const val SIMPLE_TASTE_INDEX = 0
private const val COMPLEX_TASTE_INDEX = 1
private const val HIGH_AROMA_INDEX = 0
private const val LOW_AROMA_INDEX = 1
private val flavorProfileAromaRepresentativeLevels: List<IntensityLevel> =
    listOf(IntensityLevel.STRONG, IntensityLevel.MEDIUM)
private val flavorProfileTasteRepresentativeLevels: List<ComplexityLevel> =
    listOf(ComplexityLevel.MEDIUM, ComplexityLevel.SLIGHTLY_COMPLEX)
private val highAromaLevels: Set<IntensityLevel> =
    setOf(IntensityLevel.STRONG, IntensityLevel.VERY_STRONG)
private val highTasteLevels: Set<ComplexityLevel> =
    setOf(ComplexityLevel.SLIGHTLY_COMPLEX, ComplexityLevel.COMPLEX)

fun deriveFlavorProfileType(
    intensity: IntensityLevel?,
    complexity: ComplexityLevel?,
): FlavorProfileType? {
    if (intensity == null || complexity == null) {
        return null
    }
    val isAromaHigh = intensity.isHighAroma()
    val isTasteHigh = complexity.isHighTaste()
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
    if (intensity == null || complexity == null) {
        return null
    }
    val xIndex = if (complexity.isHighTaste()) COMPLEX_TASTE_INDEX else SIMPLE_TASTE_INDEX
    val yIndex = if (intensity.isHighAroma()) HIGH_AROMA_INDEX else LOW_AROMA_INDEX
    return FlavorProfileCell(xIndex = xIndex, yIndex = yIndex)
}

fun flavorProfileSelectionAt(
    xIndex: Int,
    yIndex: Int,
): FlavorProfileSelection? {
    val intensity = flavorProfileAromaRepresentativeLevels.getOrNull(yIndex)
    val complexity = flavorProfileTasteRepresentativeLevels.getOrNull(xIndex)
    return if (intensity != null && complexity != null) {
        FlavorProfileSelection(
            intensity = intensity,
            complexity = complexity,
        )
    } else {
        null
    }
}

fun flavorProfileTypeAt(
    xIndex: Int,
    yIndex: Int,
): FlavorProfileType? =
    when (yIndex to xIndex) {
        HIGH_AROMA_INDEX to SIMPLE_TASTE_INDEX -> FlavorProfileType.KUNSHU
        HIGH_AROMA_INDEX to COMPLEX_TASTE_INDEX -> FlavorProfileType.JUKUSHU
        LOW_AROMA_INDEX to SIMPLE_TASTE_INDEX -> FlavorProfileType.SOUSHU
        LOW_AROMA_INDEX to COMPLEX_TASTE_INDEX -> FlavorProfileType.JUNSHU
        else -> null
    }

private fun IntensityLevel.isHighAroma(): Boolean = this in highAromaLevels

private fun ComplexityLevel.isHighTaste(): Boolean = this in highTasteLevels
