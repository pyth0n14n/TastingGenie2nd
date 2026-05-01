package io.github.pyth0n14n.tastinggenie.feature.review.edit

import io.github.pyth0n14n.tastinggenie.domain.model.enums.Aroma
import io.github.pyth0n14n.tastinggenie.domain.model.enums.AttackLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.ComplexityLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.IntensityLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.OverallReview
import io.github.pyth0n14n.tastinggenie.domain.model.enums.ReviewSoundness
import io.github.pyth0n14n.tastinggenie.domain.model.enums.SakeColor
import io.github.pyth0n14n.tastinggenie.domain.model.enums.TasteLevel
import io.github.pyth0n14n.tastinggenie.domain.model.enums.Temperature
import io.github.pyth0n14n.tastinggenie.domain.model.enums.TextureRoundness
import io.github.pyth0n14n.tastinggenie.domain.model.enums.TextureSmoothness
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

const val INVALID_NUMBER = Int.MIN_VALUE

fun MutableStateFlow<ReviewEditUiState>.updateEditable(transform: (ReviewEditUiState) -> ReviewEditUiState) {
    update { current ->
        if (current.isInputLocked) {
            current
        } else {
            transform(current)
        }
    }
}

fun ReviewEditUiState.withText(
    field: ReviewTextField,
    value: String,
): ReviewEditUiState =
    when (field) {
        ReviewTextField.DATE -> copy(date = value, error = null).clearValidationError(ReviewValidationField.DATE)
        ReviewTextField.BAR -> copy(bar = value, error = null)
        ReviewTextField.PRICE -> copy(price = value, error = null).clearValidationError(ReviewValidationField.PRICE)
        ReviewTextField.VOLUME ->
            copy(volume = value, error = null).clearValidationError(ReviewValidationField.VOLUME)
        ReviewTextField.AROMA_MAIN_NOTE -> copy(aromaMainNote = value, error = null)
        ReviewTextField.TASTE_MAIN_NOTE -> copy(tasteMainNote = value, error = null)
        ReviewTextField.OTHER_INDIVIDUALITY -> copy(otherIndividuality = value, error = null)
        ReviewTextField.OTHER_CAUTIONS -> copy(otherCautions = value, error = null)
        ReviewTextField.SCENE -> copy(scene = value, error = null)
        ReviewTextField.DISH -> copy(dish = value, error = null)
        ReviewTextField.COMMENT -> copy(comment = value, error = null)
    }

fun ReviewEditUiState.withSelection(
    field: ReviewSelectionField,
    value: String,
): ReviewEditUiState =
    if (value.isBlank()) {
        clearSelection(field)
    } else {
        withSoundnessSelection(field, value)
            ?: withChoiceSelection(field, value)
            ?: withTasteSelection(field, value)
            ?: withRatingSelection(field, value)
            ?: withViscositySelection(field, value)
            ?: this
    }

private fun ReviewEditUiState.withSoundnessSelection(
    field: ReviewSelectionField,
    value: String,
): ReviewEditUiState? =
    when (field) {
        ReviewSelectionField.APPEARANCE_SOUNDNESS ->
            copySelection(value, ReviewSoundness.entries.firstOrNull { it.name == value }) { state, selected ->
                state.copy(appearanceSoundness = selected ?: ReviewSoundness.SOUND)
            }
        ReviewSelectionField.AROMA_SOUNDNESS ->
            copySelection(value, ReviewSoundness.entries.firstOrNull { it.name == value }) { state, selected ->
                state.copy(aromaSoundness = selected ?: ReviewSoundness.SOUND)
            }
        ReviewSelectionField.TASTE_SOUNDNESS ->
            copySelection(value, ReviewSoundness.entries.firstOrNull { it.name == value }) { state, selected ->
                state.copy(tasteSoundness = selected ?: ReviewSoundness.SOUND)
            }
        else -> null
    }

private fun ReviewEditUiState.withChoiceSelection(
    field: ReviewSelectionField,
    value: String,
): ReviewEditUiState? =
    when (field) {
        ReviewSelectionField.TEMPERATURE ->
            copySelection(value, Temperature.entries.firstOrNull { it.name == value }) { state, selected ->
                state.copy(temperature = selected)
            }
        ReviewSelectionField.COLOR ->
            copySelection(value, SakeColor.entries.firstOrNull { it.name == value }) { state, selected ->
                state.copy(color = selected)
            }
        ReviewSelectionField.INTENSITY ->
            copySelection(value, IntensityLevel.entries.firstOrNull { it.name == value }) { state, selected ->
                state.copy(intensity = selected)
            }
        ReviewSelectionField.AROMA_COMPLEXITY ->
            copySelection(value, ComplexityLevel.entries.firstOrNull { it.name == value }) { state, selected ->
                state.copy(aromaComplexity = selected)
            }
        ReviewSelectionField.TASTE_COMPLEXITY ->
            copySelection(value, ComplexityLevel.entries.firstOrNull { it.name == value }) { state, selected ->
                state.copy(tasteComplexity = selected)
            }
        ReviewSelectionField.TASTE_ATTACK ->
            copySelection(value, AttackLevel.entries.firstOrNull { it.name == value }) { state, selected ->
                state.copy(tasteAttack = selected)
            }
        ReviewSelectionField.TASTE_TEXTURE_ROUNDNESS ->
            copySelection(value, TextureRoundness.entries.firstOrNull { it.name == value }) { state, selected ->
                state.copy(tasteTextureRoundness = selected)
            }
        ReviewSelectionField.TASTE_TEXTURE_SMOOTHNESS ->
            copySelection(value, TextureSmoothness.entries.firstOrNull { it.name == value }) { state, selected ->
                state.copy(tasteTextureSmoothness = selected)
            }
        else -> null
    }

private fun ReviewEditUiState.withTasteSelection(
    field: ReviewSelectionField,
    value: String,
): ReviewEditUiState? =
    when (field) {
        ReviewSelectionField.SWEET ->
            copySelection(value, TasteLevel.entries.firstOrNull { it.name == value }) { state, selected ->
                state.copy(sweet = selected)
            }
        ReviewSelectionField.SOUR ->
            copySelection(value, TasteLevel.entries.firstOrNull { it.name == value }) { state, selected ->
                state.copy(sour = selected)
            }
        ReviewSelectionField.BITTER ->
            copySelection(value, TasteLevel.entries.firstOrNull { it.name == value }) { state, selected ->
                state.copy(bitter = selected)
            }
        ReviewSelectionField.UMAMI ->
            copySelection(value, TasteLevel.entries.firstOrNull { it.name == value }) { state, selected ->
                state.copy(umami = selected)
            }
        ReviewSelectionField.SHARP ->
            copySelection(value, TasteLevel.entries.firstOrNull { it.name == value }) { state, selected ->
                state.copy(sharp = selected)
            }
        else -> null
    }

private fun ReviewEditUiState.withRatingSelection(
    field: ReviewSelectionField,
    value: String,
): ReviewEditUiState? =
    when (field) {
        ReviewSelectionField.OVERALL_REVIEW ->
            copySelection(value, OverallReview.entries.firstOrNull { it.name == value }) { state, selected ->
                state.copy(review = selected)
            }
        else -> null
    }

private fun ReviewEditUiState.withViscositySelection(
    field: ReviewSelectionField,
    value: String,
): ReviewEditUiState? =
    when (field) {
        ReviewSelectionField.VISCOSITY -> withViscosity(value)
        else -> null
    }

fun ReviewEditUiState.withAromaToggled(
    field: ReviewAromaField,
    value: String,
): ReviewEditUiState {
    val aroma =
        Aroma.entries.firstOrNull { it.name == value }
            ?: return copy(error = invalidSelectionError(value))
    return when (field) {
        ReviewAromaField.TOP -> copy(scentTop = scentTop.toggle(aroma), error = null)
        ReviewAromaField.MOUTH -> copy(scentMouth = scentMouth.toggle(aroma), error = null)
    }
}
