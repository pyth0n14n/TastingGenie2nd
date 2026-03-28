package io.github.pyth0n14n.tastinggenie.data.mapper

import io.github.pyth0n14n.tastinggenie.data.local.entity.ReviewEntity
import io.github.pyth0n14n.tastinggenie.domain.model.ReviewInput
import io.github.pyth0n14n.tastinggenie.domain.model.SakeInput
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Aroma
import io.github.pyth0n14n.tastinggenie.domain.model.enums.IntensityLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.OverallReview
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Prefecture
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeClassification
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeColor
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeGrade
import io.github.pyth0n14n.tastinggenie.domain.model.enums.TasteLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Temperature
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class MapperTest {
    @Test
    fun reviewMapper_roundTrip_preservesDate() {
        val input =
            ReviewInput(
                id = 10L,
                sakeId = 20L,
                date = LocalDate.parse("2026-02-28"),
            )

        val entity = input.toEntity()
        val domain = entity.toDomain()

        assertEquals(LocalDate.parse("2026-02-28").toEpochDay(), entity.dateEpochDay)
        assertEquals(LocalDate.parse("2026-02-28"), domain.date)
    }

    @Test
    fun reviewMapper_preservesEnumsAndLists() {
        val input =
            ReviewInput(
                id = 1L,
                sakeId = 2L,
                date = LocalDate.parse("2026-02-25"),
                temperature = Temperature.HANABIE,
                color = SakeColor.AMBER,
                intensity = IntensityLevel.STRONG,
                scentTop = listOf(Aroma.LEMON, Aroma.BANANA),
                scentBase = listOf(Aroma.KONBU),
                scentMouth = listOf(Aroma.PEAR),
                sweet = TasteLevel.MEDIUM,
                sour = TasteLevel.WEAK,
                bitter = TasteLevel.STRONG,
                umami = TasteLevel.VERY_STRONG,
                sharp = TasteLevel.WEAK,
                review = OverallReview.GOOD,
            )

        val restored = input.toEntity().toDomain()

        assertEquals(Temperature.HANABIE, restored.temperature)
        assertEquals(SakeColor.AMBER, restored.color)
        assertEquals(IntensityLevel.STRONG, restored.intensity)
        assertEquals(listOf(Aroma.LEMON, Aroma.BANANA), restored.scentTop)
        assertEquals(listOf(Aroma.KONBU), restored.scentBase)
        assertEquals(listOf(Aroma.PEAR), restored.scentMouth)
        assertEquals(TasteLevel.MEDIUM, restored.sweet)
        assertEquals(OverallReview.GOOD, restored.review)
    }

    @Test
    fun reviewMapper_handlesNullOptionalFields() {
        val entity =
            ReviewEntity(
                id = 1L,
                sakeId = 2L,
                dateEpochDay = LocalDate.parse("2026-02-25").toEpochDay(),
                bar = null,
                price = null,
                volume = null,
                temperature = null,
                color = null,
                viscosity = null,
                intensity = null,
                scentTop = emptyList(),
                scentBase = emptyList(),
                scentMouth = emptyList(),
                sweet = null,
                sour = null,
                bitter = null,
                umami = null,
                sharp = null,
                scene = null,
                dish = null,
                comment = null,
                review = null,
            )

        assertNull(entity.toDomain().comment)
    }

    @Test
    fun sakeMapper_roundTrip_preservesEnumListAndImageUri() {
        val input =
            SakeInput(
                id = 5L,
                name = "テスト銘柄",
                grade = SakeGrade.JUNMAI_GINJO,
                imageUri = "file:///images/sakes/1.jpg",
                type = listOf(SakeClassification.KIMOTO, SakeClassification.HIYAOROSHI),
                prefecture = Prefecture.KYOTO,
            )

        val restored = input.toEntity().toDomain()

        assertEquals("file:///images/sakes/1.jpg", restored.imageUri)
        assertEquals(SakeGrade.JUNMAI_GINJO, restored.grade)
        assertEquals(listOf(SakeClassification.KIMOTO, SakeClassification.HIYAOROSHI), restored.type)
        assertEquals(Prefecture.KYOTO, restored.prefecture)
    }
}
