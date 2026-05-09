package io.github.pyth0n14n.tastinggenie.feature.review

import io.github.pyth0n14n.tastinggenie.domain.model.enums.ComplexityLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.IntensityLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReviewFlavorProfileTest {
    @Test
    fun deriveFlavorProfileType_returnsExpectedQuadrants() {
        assertEquals(
            FlavorProfileType.SOUSHU,
            deriveFlavorProfileType(
                intensity = IntensityLevel.MEDIUM,
                complexity = ComplexityLevel.MEDIUM,
            ),
        )
        assertEquals(
            FlavorProfileType.KUNSHU,
            deriveFlavorProfileType(
                intensity = IntensityLevel.STRONG,
                complexity = ComplexityLevel.MEDIUM,
            ),
        )
        assertEquals(
            FlavorProfileType.JUNSHU,
            deriveFlavorProfileType(
                intensity = IntensityLevel.WEAK,
                complexity = ComplexityLevel.COMPLEX,
            ),
        )
        assertEquals(
            FlavorProfileType.JUKUSHU,
            deriveFlavorProfileType(
                intensity = IntensityLevel.VERY_STRONG,
                complexity = ComplexityLevel.SLIGHTLY_COMPLEX,
            ),
        )
    }

    @Test
    fun deriveFlavorProfileType_returnsNullWhenIncomplete() {
        assertNull(
            deriveFlavorProfileType(
                intensity = IntensityLevel.WEAK,
                complexity = null,
            ),
        )
        assertNull(
            deriveFlavorProfileType(
                intensity = null,
                complexity = ComplexityLevel.COMPLEX,
            ),
        )
    }

    @Test
    fun flavorProfileSelectionAt_mapsGridCoordinatesToCanonicalValues() {
        assertEquals(
            FlavorProfileSelection(
                intensity = IntensityLevel.STRONG,
                complexity = ComplexityLevel.MEDIUM,
            ),
            flavorProfileSelectionAt(xIndex = 0, yIndex = 0),
        )
        assertEquals(
            FlavorProfileSelection(
                intensity = IntensityLevel.MEDIUM,
                complexity = ComplexityLevel.SLIGHTLY_COMPLEX,
            ),
            flavorProfileSelectionAt(xIndex = 1, yIndex = 1),
        )
        assertEquals(
            FlavorProfileCell(xIndex = 0, yIndex = 1),
            selectedFlavorProfileCell(
                intensity = IntensityLevel.MEDIUM,
                complexity = ComplexityLevel.MEDIUM,
            ),
        )
    }

    @Test
    fun flavorProfileTypeAt_returnsExpectedGridLayout() {
        assertEquals(FlavorProfileType.KUNSHU, flavorProfileTypeAt(xIndex = 0, yIndex = 0))
        assertEquals(FlavorProfileType.JUKUSHU, flavorProfileTypeAt(xIndex = 1, yIndex = 0))
        assertEquals(FlavorProfileType.SOUSHU, flavorProfileTypeAt(xIndex = 0, yIndex = 1))
        assertEquals(FlavorProfileType.JUNSHU, flavorProfileTypeAt(xIndex = 1, yIndex = 1))
    }
}
