package io.github.pyth0n14n.tastinggenie.feature.review

import io.github.pyth0n14n.tastinggenie.domain.model.Review
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Aroma
import io.github.pyth0n14n.tastinggenie.domain.model.enums.IntensityLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.OverallReview
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeColor
import io.github.pyth0n14n.tastinggenie.domain.model.enums.TasteLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Temperature
import java.time.LocalDate

internal const val TEST_SAKE_ID = 7L
internal const val TEST_REVIEW_ID = 11L
private const val TEST_REVIEW_DATE = "2026-03-14"

internal fun testReview(
    id: Long = TEST_REVIEW_ID,
    sakeId: Long = TEST_SAKE_ID,
    date: LocalDate = LocalDate.parse(TEST_REVIEW_DATE),
    otherOverallReview: OverallReview? = OverallReview.GOOD,
    tasteInPalateAroma: List<Aroma> = emptyList(),
): Review =
    Review(
        id = id,
        sakeId = sakeId,
        date = date,
        temperature = Temperature.JOON,
        appearanceColor = SakeColor.CLEAR,
        aromaIntensity = IntensityLevel.WEAK,
        aromaExamples = listOf(Aroma.MELON),
        tasteInPalateAroma = tasteInPalateAroma,
        tasteSweetness = TasteLevel.STRONG,
        otherOverallReview = otherOverallReview,
    )
