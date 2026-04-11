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
                intensity = IntensityLevel.VERY_WEAK,
                complexity = ComplexityLevel.COMPLEX,
            ),
            flavorProfileSelectionAt(xIndex = 0, yIndex = 0),
        )
        assertEquals(
            FlavorProfileSelection(
                intensity = IntensityLevel.VERY_STRONG,
                complexity = ComplexityLevel.SIMPLE,
            ),
            flavorProfileSelectionAt(xIndex = 4, yIndex = 4),
        )
        assertEquals(
            FlavorProfileCell(xIndex = 2, yIndex = 2),
            selectedFlavorProfileCell(
                intensity = IntensityLevel.MEDIUM,
                complexity = ComplexityLevel.MEDIUM,
            ),
        )
    }
}
