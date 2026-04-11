package io.github.pyth0n14n.tastinggenie.feature.review.edit

import io.github.pyth0n14n.tastinggenie.domain.model.enums.AttackLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.ComplexityLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.ReviewSoundness
import io.github.pyth0n14n.tastinggenie.domain.model.enums.TextureRoundness
import io.github.pyth0n14n.tastinggenie.domain.model.enums.TextureSmoothness
import io.github.pyth0n14n.tastinggenie.ui.common.DropdownOption

fun reviewSoundnessOptions(): List<DropdownOption> =
    listOf(
        DropdownOption(value = ReviewSoundness.SOUND.name, label = "健全"),
        DropdownOption(value = ReviewSoundness.UNSOUND.name, label = "不健全"),
    )

fun complexityOptions(): List<DropdownOption> =
    listOf(
        DropdownOption(value = ComplexityLevel.SIMPLE.name, label = "シンプル"),
        DropdownOption(value = ComplexityLevel.SLIGHTLY_SIMPLE.name, label = "ややシンプル"),
        DropdownOption(value = ComplexityLevel.MEDIUM.name, label = "中程度"),
        DropdownOption(value = ComplexityLevel.SLIGHTLY_COMPLEX.name, label = "やや複雑"),
        DropdownOption(value = ComplexityLevel.COMPLEX.name, label = "複雑"),
    )

fun attackOptions(): List<DropdownOption> =
    listOf(
        DropdownOption(value = AttackLevel.WEAK.name, label = "弱い"),
        DropdownOption(value = AttackLevel.SLIGHTLY_WEAK.name, label = "やや弱い"),
        DropdownOption(value = AttackLevel.MEDIUM.name, label = "中程度"),
        DropdownOption(value = AttackLevel.SLIGHTLY_STRONG.name, label = "やや強い"),
        DropdownOption(value = AttackLevel.STRONG.name, label = "強い"),
    )

fun textureRoundnessOptions(): List<DropdownOption> =
    listOf(
        DropdownOption(value = TextureRoundness.FIRM_TIGHT.name, label = "シャープ"),
        DropdownOption(value = TextureRoundness.SLIGHTLY_FIRM_SHARP.name, label = "ややシャープ"),
        DropdownOption(value = TextureRoundness.BALANCED.name, label = "中間"),
        DropdownOption(value = TextureRoundness.SLIGHTLY_SOFT_ROUND.name, label = "やや柔らかい"),
        DropdownOption(value = TextureRoundness.SOFT.name, label = "柔らかい"),
    )

fun textureSmoothnessOptions(): List<DropdownOption> =
    listOf(
        DropdownOption(value = TextureSmoothness.ROUGH.name, label = "粗い"),
        DropdownOption(value = TextureSmoothness.SLIGHTLY_ROUGH.name, label = "やや粗い"),
        DropdownOption(value = TextureSmoothness.BALANCED.name, label = "中間"),
        DropdownOption(value = TextureSmoothness.SLIGHTLY_FINE.name, label = "ややなめらか"),
        DropdownOption(value = TextureSmoothness.SMOOTH.name, label = "なめらか"),
    )
