package io.github.pyth0n14n.tastinggenie.feature.review.edit

import io.github.pyth0n14n.tastinggenie.domain.model.MasterOption
import io.github.pyth0n14n.tastinggenie.domain.model.enums.FlavorProfileType
import io.github.pyth0n14n.tastinggenie.domain.model.enums.FoodCompatibility
import io.github.pyth0n14n.tastinggenie.feature.review.aftertasteLabel
import io.github.pyth0n14n.tastinggenie.ui.common.DropdownOption

fun List<MasterOption>.toOptions(): List<DropdownOption> =
    map { option ->
        DropdownOption(
            value = option.value,
            label = option.label,
        )
    }

fun List<MasterOption>.toAftertasteOptions(): List<DropdownOption> =
    map { option ->
        DropdownOption(
            value = option.value,
            label = aftertasteLabel(value = option.value) ?: option.label,
        )
    }

fun foodCompatibilityOptions(): List<DropdownOption> =
    listOf(
        FoodCompatibility.BAD to "悪い",
        FoodCompatibility.SLIGHTLY_BAD to "やや悪い",
        FoodCompatibility.MEDIUM to "普通",
        FoodCompatibility.SLIGHTLY_GOOD to "やや良い",
        FoodCompatibility.GOOD to "良い",
    ).map { (value, label) ->
        DropdownOption(
            value = value.name,
            label = label,
        )
    }

fun sakeTypeOptions(): List<DropdownOption> =
    listOf(
        FlavorProfileType.SOUSHU to "爽酒",
        FlavorProfileType.KUNSHU to "薫酒",
        FlavorProfileType.JUNSHU to "醇酒",
        FlavorProfileType.JUKUSHU to "熟酒",
    ).map { (value, label) ->
        DropdownOption(
            value = value.name,
            label = label,
        )
    }
