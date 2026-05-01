package io.github.pyth0n14n.tastinggenie.feature.review.edit

import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.enums.AttackLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.ComplexityLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.ReviewSoundness
import io.github.pyth0n14n.tastinggenie.domain.model.enums.TextureRoundness
import io.github.pyth0n14n.tastinggenie.domain.model.enums.TextureSmoothness

fun reviewSoundnessOptions(): List<ReviewResourceOption> =
    listOf(
        ReviewResourceOption(value = ReviewSoundness.SOUND.name, labelRes = R.string.label_soundness_sound),
        ReviewResourceOption(value = ReviewSoundness.UNSOUND.name, labelRes = R.string.label_soundness_unsound),
    )

fun complexityOptions(): List<ReviewResourceOption> =
    listOf(
        ReviewResourceOption(value = ComplexityLevel.SIMPLE.name, labelRes = R.string.label_complexity_simple),
        ReviewResourceOption(
            value = ComplexityLevel.SLIGHTLY_SIMPLE.name,
            labelRes = R.string.label_complexity_slightly_simple,
        ),
        ReviewResourceOption(value = ComplexityLevel.MEDIUM.name, labelRes = R.string.label_complexity_medium),
        ReviewResourceOption(
            value = ComplexityLevel.SLIGHTLY_COMPLEX.name,
            labelRes = R.string.label_complexity_slightly_complex,
        ),
        ReviewResourceOption(value = ComplexityLevel.COMPLEX.name, labelRes = R.string.label_complexity_complex),
    )

fun attackOptions(): List<ReviewResourceOption> =
    listOf(
        ReviewResourceOption(value = AttackLevel.WEAK.name, labelRes = R.string.label_attack_weak),
        ReviewResourceOption(value = AttackLevel.SLIGHTLY_WEAK.name, labelRes = R.string.label_attack_slightly_weak),
        ReviewResourceOption(value = AttackLevel.MEDIUM.name, labelRes = R.string.label_attack_medium),
        ReviewResourceOption(
            value = AttackLevel.SLIGHTLY_STRONG.name,
            labelRes = R.string.label_attack_slightly_strong,
        ),
        ReviewResourceOption(value = AttackLevel.STRONG.name, labelRes = R.string.label_attack_strong),
    )

fun textureRoundnessOptions(): List<ReviewResourceOption> =
    listOf(
        ReviewResourceOption(
            value = TextureRoundness.FIRM_TIGHT.name,
            labelRes = R.string.label_texture_roundness_firm_tight_short,
        ),
        ReviewResourceOption(
            value = TextureRoundness.SLIGHTLY_FIRM_SHARP.name,
            labelRes = R.string.label_texture_roundness_slightly_firm_sharp,
        ),
        ReviewResourceOption(
            value = TextureRoundness.BALANCED.name,
            labelRes = R.string.label_texture_roundness_balanced,
        ),
        ReviewResourceOption(
            value = TextureRoundness.SLIGHTLY_SOFT_ROUND.name,
            labelRes = R.string.label_texture_roundness_slightly_soft_round,
        ),
        ReviewResourceOption(
            value = TextureRoundness.SOFT.name,
            labelRes = R.string.label_texture_roundness_soft,
        ),
    )

fun textureSmoothnessOptions(): List<ReviewResourceOption> =
    listOf(
        ReviewResourceOption(value = TextureSmoothness.ROUGH.name, labelRes = R.string.label_texture_smoothness_rough),
        ReviewResourceOption(
            value = TextureSmoothness.SLIGHTLY_ROUGH.name,
            labelRes = R.string.label_texture_smoothness_slightly_rough,
        ),
        ReviewResourceOption(
            value = TextureSmoothness.BALANCED.name,
            labelRes = R.string.label_texture_smoothness_balanced,
        ),
        ReviewResourceOption(
            value = TextureSmoothness.SLIGHTLY_FINE.name,
            labelRes = R.string.label_texture_smoothness_slightly_fine_short,
        ),
        ReviewResourceOption(
            value = TextureSmoothness.SMOOTH.name,
            labelRes = R.string.label_texture_smoothness_smooth,
        ),
    )
