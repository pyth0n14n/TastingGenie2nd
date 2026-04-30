package io.github.pyth0n14n.tastinggenie.feature.review.edit

import io.github.pyth0n14n.tastinggenie.domain.model.MasterOption
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

fun List<MasterOption>.toPairingOptions(): List<DropdownOption> =
    map { option ->
        DropdownOption(
            value = option.label,
            label = option.label,
        )
    }
