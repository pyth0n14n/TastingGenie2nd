package io.github.pyth0n14n.tastinggenie.feature.review.edit

import io.github.pyth0n14n.tastinggenie.domain.model.AromaComponent
import io.github.pyth0n14n.tastinggenie.domain.model.AromaTaste
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Aroma
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AromaPickerTest {
    @Test
    fun buildAromaUiModel_groupsByTasteAndCategoryWithoutComponentLevel() {
        val model =
            buildAromaUiModel(
                query = "",
                tasteFilters = setOf(AromaTaste.SWEET),
                categoryFilters = emptySet(),
                selected = setOf(Aroma.LYCHEE),
            )

        val sweet = model.sections.single()
        val fruit = sweet.categories.first { it.label == "果実類" }

        assertEquals(AromaTaste.SWEET, sweet.taste)
        assertEquals(1, sweet.selectedCount)
        assertTrue(fruit.items.any { it.aroma == Aroma.LYCHEE && it.label == "ライチ" })
        assertEquals(listOf(AromaComponent.GINJO.label), fruit.componentLabels)
    }

    @Test
    fun buildAromaUiModel_searchFindsSpecificExamplesAcrossTastes() {
        val pear =
            buildAromaUiModel(
                query = "梨",
                tasteFilters = emptySet(),
                categoryFilters = emptySet(),
                selected = emptySet(),
            )
        val rawRice =
            buildAromaUiModel(
                query = "生米",
                tasteFilters = emptySet(),
                categoryFilters = emptySet(),
                selected = emptySet(),
            )

        val pearItems =
            pear.sections
                .single()
                .categories
                .single()
                .items
        val rawRiceItems =
            rawRice.sections
                .single()
                .categories
                .single()
                .items

        assertTrue(pearItems.any { it.aroma == Aroma.JAPANESE_PEAR })
        assertTrue(rawRiceItems.any { it.aroma == Aroma.RAW_RICE })
    }

    @Test
    fun buildAromaUiModel_tasteFilterExcludesOtherTastes() {
        val model =
            buildAromaUiModel(
                query = "",
                tasteFilters = setOf(AromaTaste.UMAMI),
                categoryFilters = emptySet(),
                selected = emptySet(),
            )

        assertEquals(listOf(AromaTaste.UMAMI), model.sections.map { it.taste })
        assertFalse(model.sections.any { section -> section.categories.any { it.label == "果実類" } })
    }

    @Test
    fun aromaSelectionChangedRemovesDuplicates() {
        val state =
            ReviewEditUiState().withAromaSelectionChanged(
                field = ReviewAromaField.TOP,
                values = listOf(Aroma.LYCHEE, Aroma.LYCHEE),
            )

        assertEquals(listOf(Aroma.LYCHEE), state.scentTop)
    }

    @Test
    fun savedAromaListUsesMasterOrderWithoutDuplicates() {
        val saved = setOf(Aroma.LYCHEE, Aroma.MELON).toSavedAromaList()

        assertEquals(listOf(Aroma.MELON, Aroma.LYCHEE), saved)
    }
}
