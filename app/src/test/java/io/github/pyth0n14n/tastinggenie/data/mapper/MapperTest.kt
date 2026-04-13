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
                appearanceSoundness = io.github.pyth0n14n.tastinggenie.domain.model.enums.ReviewSoundness.SOUND,
                appearanceColor = SakeColor.AMBER,
                aromaSoundness = io.github.pyth0n14n.tastinggenie.domain.model.enums.ReviewSoundness.SOUND,
                aromaIntensity = IntensityLevel.STRONG,
                aromaExamples = listOf(Aroma.LEMON, Aroma.BANANA),
                tasteSoundness = io.github.pyth0n14n.tastinggenie.domain.model.enums.ReviewSoundness.SOUND,
                tasteInPalateAroma = listOf(Aroma.PEAR),
                tasteSweetness = TasteLevel.MEDIUM,
                tasteSourness = TasteLevel.WEAK,
                tasteBitterness = TasteLevel.STRONG,
                tasteUmami = TasteLevel.VERY_STRONG,
                tasteAftertaste = TasteLevel.WEAK,
                otherOverallReview = OverallReview.GOOD,
            )

        val restored = input.toEntity().toDomain()

        assertEquals(Temperature.HANABIE, restored.temperature)
        assertEquals(SakeColor.AMBER, restored.appearanceColor)
        assertEquals(IntensityLevel.STRONG, restored.aromaIntensity)
        assertEquals(listOf(Aroma.LEMON, Aroma.BANANA), restored.aromaExamples)
        assertEquals(listOf(Aroma.PEAR), restored.tasteInPalateAroma)
        assertEquals(TasteLevel.MEDIUM, restored.tasteSweetness)
        assertEquals(OverallReview.GOOD, restored.otherOverallReview)
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
                scene = null,
                dish = null,
                appearanceSoundness = io.github.pyth0n14n.tastinggenie.domain.model.enums.ReviewSoundness.SOUND,
                appearanceColor = null,
                appearanceViscosity = null,
                aromaSoundness = io.github.pyth0n14n.tastinggenie.domain.model.enums.ReviewSoundness.SOUND,
                aromaIntensity = null,
                aromaExamples = emptyList(),
                aromaMainNote = null,
                aromaComplexity = null,
                tasteSoundness = io.github.pyth0n14n.tastinggenie.domain.model.enums.ReviewSoundness.SOUND,
                tasteAttack = null,
                tasteTextureRoundness = null,
                tasteTextureSmoothness = null,
                tasteMainNote = null,
                tasteSweetness = null,
                tasteSourness = null,
                tasteBitterness = null,
                tasteUmami = null,
                tasteInPalateAroma = emptyList(),
                tasteAftertaste = null,
                tasteComplexity = null,
                otherIndividuality = null,
                otherCautions = null,
                otherOverallReview = null,
            )

        assertNull(entity.toDomain().otherCautions)
    }

    @Test
    fun sakeMapper_roundTrip_preservesEnumListAndImageUris() {
        val input =
            SakeInput(
                id = 5L,
                name = "テスト銘柄",
                grade = SakeGrade.JUNMAI_GINJO,
                imageUris = listOf("file:///images/sakes/1.jpg"),
                type = listOf(SakeClassification.KIMOTO, SakeClassification.HIYAOROSHI),
                prefecture = Prefecture.KYOTO,
            )

        val restored = input.toEntity().toDomain()

        assertEquals(listOf("file:///images/sakes/1.jpg"), restored.imageUris)
        assertEquals(SakeGrade.JUNMAI_GINJO, restored.grade)
        assertEquals(listOf(SakeClassification.KIMOTO, SakeClassification.HIYAOROSHI), restored.type)
        assertEquals(Prefecture.KYOTO, restored.prefecture)
    }
}
