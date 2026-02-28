package io.github.pyth0n14n.tastinggenie.data.local.converter

import io.github.pyth0n14n.tastinggenie.domain.model.enums.Aroma
import io.github.pyth0n14n.tastinggenie.domain.model.enums.IntensityLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Prefecture
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeClassification
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeColor
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeGrade
import io.github.pyth0n14n.tastinggenie.domain.model.enums.TasteLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Temperature
import org.junit.Assert.assertEquals
import org.junit.Test

class ConvertersTest {
    @Test
    fun sakeConverters_roundTrip() {
        val converter = SakeConverters()
        val gradeRaw = converter.fromGrade(SakeGrade.JUNMAI_GINJO)
        val prefectureRaw = converter.fromPrefecture(Prefecture.KYOTO)
        val listRaw = converter.fromClassificationList(
            listOf(SakeClassification.KIMOTO, SakeClassification.HIYAOROSHI),
        )

        assertEquals(SakeGrade.JUNMAI_GINJO, converter.toGrade(gradeRaw))
        assertEquals(Prefecture.KYOTO, converter.toPrefecture(prefectureRaw))
        assertEquals(
            listOf(SakeClassification.KIMOTO, SakeClassification.HIYAOROSHI),
            converter.toClassificationList(listRaw),
        )
    }

    @Test
    fun reviewEnumConverters_roundTrip() {
        val converter = ReviewEnumConverters()
        val temperature = converter.toTemperature(converter.fromTemperature(Temperature.JOON))
        val color = converter.toColor(converter.fromColor(SakeColor.AMBER))
        val intensity = converter.toIntensity(converter.fromIntensity(IntensityLevel.MEDIUM))
        val taste = converter.toTaste(converter.fromTaste(TasteLevel.STRONG))

        assertEquals(Temperature.JOON, temperature)
        assertEquals(SakeColor.AMBER, color)
        assertEquals(IntensityLevel.MEDIUM, intensity)
        assertEquals(TasteLevel.STRONG, taste)
    }

    @Test
    fun aromaListConverter_roundTrip() {
        val converter = AromaListConverter()
        val raw = converter.fromAromaList(listOf(Aroma.LEMON, Aroma.BANANA))
        val restored = converter.toAromaList(raw)

        assertEquals(listOf(Aroma.LEMON, Aroma.BANANA), restored)
    }
}
