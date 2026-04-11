package io.github.pyth0n14n.tastinggenie.feature.review.detail

import io.github.pyth0n14n.tastinggenie.domain.model.enums.AttackLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.ComplexityLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.ReviewSoundness
import io.github.pyth0n14n.tastinggenie.domain.model.enums.TextureRoundness
import io.github.pyth0n14n.tastinggenie.domain.model.enums.TextureSmoothness

internal fun ReviewSoundness.toLabel(): String =
    when (this) {
        ReviewSoundness.SOUND -> "健全"
        ReviewSoundness.UNSOUND -> "不健全"
    }

internal fun ComplexityLevel.toLabel(): String =
    when (this) {
        ComplexityLevel.SIMPLE -> "シンプル"
        ComplexityLevel.SLIGHTLY_SIMPLE -> "ややシンプル"
        ComplexityLevel.MEDIUM -> "中程度"
        ComplexityLevel.SLIGHTLY_COMPLEX -> "やや複雑"
        ComplexityLevel.COMPLEX -> "複雑"
    }

internal fun AttackLevel.toLabel(): String =
    when (this) {
        AttackLevel.WEAK -> "弱い"
        AttackLevel.SLIGHTLY_WEAK -> "やや弱い"
        AttackLevel.MEDIUM -> "中程度"
        AttackLevel.SLIGHTLY_STRONG -> "やや強い"
        AttackLevel.STRONG -> "強い"
    }

internal fun TextureRoundness.toLabel(): String =
    when (this) {
        TextureRoundness.FIRM_TIGHT -> "シャープ"
        TextureRoundness.SLIGHTLY_FIRM_SHARP -> "ややシャープ"
        TextureRoundness.BALANCED -> "中間"
        TextureRoundness.SLIGHTLY_SOFT_ROUND -> "やや柔らかい"
        TextureRoundness.SOFT -> "柔らかい"
        TextureRoundness.MELLOW -> "柔らかい"
    }

internal fun TextureSmoothness.toLabel(): String =
    when (this) {
        TextureSmoothness.ROUGH -> "粗い"
        TextureSmoothness.SLIGHTLY_ROUGH -> "やや粗い"
        TextureSmoothness.BALANCED -> "中間"
        TextureSmoothness.SLIGHTLY_FINE -> "ややなめらか"
        TextureSmoothness.SMOOTH -> "なめらか"
    }
