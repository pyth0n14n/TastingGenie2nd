package io.github.pyth0n14n.tastinggenie.feature.review

import io.github.pyth0n14n.tastinggenie.domain.model.enums.TasteLevel

fun aftertasteLabel(value: String): String? =
    when (value) {
        TasteLevel.VERY_WEAK.name -> "短い"
        TasteLevel.WEAK.name -> "やや短い"
        TasteLevel.MEDIUM.name -> "中程度"
        TasteLevel.STRONG.name -> "やや長い"
        TasteLevel.VERY_STRONG.name -> "長い"
        else -> null
    }
