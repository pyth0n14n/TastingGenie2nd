package io.github.pyth0n14n.tastinggenie.feature.review.edit

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.pyth0n14n.tastinggenie.R
import io.github.pyth0n14n.tastinggenie.domain.model.ReviewItemId

private val HelpIconSize = 32.dp
private val LeftSwipeDismissDistance = 96.dp

@Composable
@Suppress("LongParameterList")
internal fun ReviewHelpLabel(
    label: String,
    itemId: ReviewItemId?,
    showHelpHints: Boolean,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = Color.Unspecified,
) {
    var visibleHelp by remember { mutableStateOf<ReviewHelpMessage?>(null) }
    val help = itemId?.toReviewHelpMessage()
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = style, color = color)
        if (showHelpHints && help != null) {
            IconButton(
                onClick = { visibleHelp = help },
                modifier = Modifier.size(HelpIconSize),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                    contentDescription = stringResource(R.string.cd_review_item_help, label),
                )
            }
        }
    }
    visibleHelp?.let { helpMessage ->
        ReviewHelpDialog(
            help = helpMessage,
            onDismiss = { visibleHelp = null },
        )
    }
}

@Composable
internal fun ReviewHelpAction(
    label: String,
    itemId: ReviewItemId?,
    showHelpHints: Boolean,
) {
    var visibleHelp by remember { mutableStateOf<ReviewHelpMessage?>(null) }
    val help = itemId?.toReviewHelpMessage()
    if (showHelpHints && help != null) {
        IconButton(
            onClick = { visibleHelp = help },
            modifier = Modifier.size(HelpIconSize),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                contentDescription = stringResource(R.string.cd_review_item_help, label),
            )
        }
    }
    visibleHelp?.let { helpMessage ->
        ReviewHelpDialog(
            help = helpMessage,
            onDismiss = { visibleHelp = null },
        )
    }
}

@Composable
private fun ReviewHelpDialog(
    help: ReviewHelpMessage,
    onDismiss: () -> Unit,
) {
    val dismissDistancePx = with(LocalDensity.current) { LeftSwipeDismissDistance.toPx() }
    var horizontalDrag by remember { mutableFloatStateOf(0f) }
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier =
            Modifier.pointerInput(onDismiss, dismissDistancePx) {
                detectHorizontalDragGestures(
                    onDragStart = { horizontalDrag = 0f },
                    onHorizontalDrag = { _, dragAmount ->
                        horizontalDrag += dragAmount
                        if (horizontalDrag <= -dismissDistancePx) {
                            onDismiss()
                        }
                    },
                    onDragEnd = { horizontalDrag = 0f },
                    onDragCancel = { horizontalDrag = 0f },
                )
            },
        title = { Text(text = stringResource(help.titleRes)) },
        text = {
            val message = stringResource(help.messageRes)
            val helpText =
                if (help.boldPrefixBeforeColon) {
                    message.boldPrefixBeforeColon()
                } else {
                    buildAnnotatedString { append(message) }
                }
            Text(
                text = helpText,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.action_ok))
            }
        },
    )
}

private fun ReviewItemId.toReviewHelpMessage(): ReviewHelpMessage? = ReviewHelpMessagesByItemId[this]

private data class ReviewHelpMessage(
    val titleRes: Int,
    val messageRes: Int,
    val boldPrefixBeforeColon: Boolean = false,
)

private fun String.boldPrefixBeforeColon() =
    buildAnnotatedString {
        lines().forEachIndexed { index, line ->
            if (index > 0) {
                append('\n')
            }
            val colonIndex = line.indexOf('：')
            if (colonIndex <= 0) {
                append(line)
            } else {
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                append(line.substring(0, colonIndex))
                pop()
                append(line.substring(colonIndex))
            }
        }
    }

private val ReviewHelpMessagesByItemId =
    mapOf(
        ReviewItemId.APPEARANCE_SOUNDNESS to
            ReviewHelpMessage(
                R.string.review_help_appearance_soundness_title,
                R.string.review_help_appearance_soundness_message,
            ),
        ReviewItemId.APPEARANCE_VISCOSITY to
            ReviewHelpMessage(
                R.string.review_help_appearance_viscosity_title,
                R.string.review_help_appearance_viscosity_message,
            ),
        ReviewItemId.AROMA_INTENSITY to
            ReviewHelpMessage(
                R.string.review_help_aroma_uwatachika_title,
                R.string.review_help_aroma_uwatachika_message,
            ),
        ReviewItemId.AROMA_EXAMPLES to
            ReviewHelpMessage(
                R.string.review_help_aroma_examples_title,
                R.string.review_help_aroma_examples_message,
            ),
        ReviewItemId.AROMA_MAIN_NOTE to
            ReviewHelpMessage(
                R.string.review_help_aroma_main_note_title,
                R.string.review_help_aroma_main_note_message,
            ),
        ReviewItemId.AROMA_COMPLEXITY to
            ReviewHelpMessage(
                R.string.review_help_aroma_complexity_title,
                R.string.review_help_aroma_complexity_message,
            ),
        ReviewItemId.TASTE_ATTACK to
            ReviewHelpMessage(
                R.string.review_help_taste_attack_title,
                R.string.review_help_taste_attack_message,
            ),
        ReviewItemId.TASTE_TEXTURE_ROUNDNESS to
            ReviewHelpMessage(
                R.string.review_help_taste_texture_title,
                R.string.review_help_taste_texture_message,
            ),
        ReviewItemId.TASTE_TEXTURE_SMOOTHNESS to
            ReviewHelpMessage(
                R.string.review_help_taste_texture_title,
                R.string.review_help_taste_texture_message,
            ),
        ReviewItemId.TASTE_TEXTURE_NOTE to
            ReviewHelpMessage(
                R.string.review_help_taste_texture_note_title,
                R.string.review_help_taste_texture_note_message,
            ),
        ReviewItemId.TASTE_SWEETNESS to
            ReviewHelpMessage(
                R.string.review_help_taste_sweetness_title,
                R.string.review_help_taste_sweetness_message,
            ),
        ReviewItemId.TASTE_SOURNESS to
            ReviewHelpMessage(
                R.string.review_help_taste_sourness_title,
                R.string.review_help_taste_sourness_message,
            ),
        ReviewItemId.TASTE_UMAMI to
            ReviewHelpMessage(
                R.string.review_help_taste_umami_title,
                R.string.review_help_taste_umami_message,
            ),
        ReviewItemId.TASTE_BITTERNESS to
            ReviewHelpMessage(
                R.string.review_help_taste_bitterness_title,
                R.string.review_help_taste_bitterness_message,
            ),
        ReviewItemId.TASTE_DESCRIPTION to
            ReviewHelpMessage(
                R.string.review_help_taste_description_title,
                R.string.review_help_taste_description_message,
            ),
        ReviewItemId.TASTE_SWEET_DRYNESS to
            ReviewHelpMessage(
                R.string.review_help_taste_sweet_dryness_title,
                R.string.review_help_taste_sweet_dryness_message,
            ),
        ReviewItemId.TASTE_IN_PALATE_AROMA_INTENSITY to
            ReviewHelpMessage(
                R.string.review_help_taste_in_palate_aroma_intensity_title,
                R.string.review_help_taste_in_palate_aroma_intensity_message,
            ),
        ReviewItemId.TASTE_AFTERTASTE_LENGTH to
            ReviewHelpMessage(
                R.string.review_help_taste_aftertaste_length_title,
                R.string.review_help_taste_aftertaste_length_message,
            ),
        ReviewItemId.TASTE_AFTERTASTE_NOTE to
            ReviewHelpMessage(
                R.string.review_help_taste_aftertaste_note_title,
                R.string.review_help_taste_aftertaste_note_message,
            ),
        ReviewItemId.TASTE_COMPLEXITY to
            ReviewHelpMessage(
                R.string.review_help_taste_complexity_title,
                R.string.review_help_taste_complexity_message,
            ),
        ReviewItemId.OTHER_INDIVIDUALITY to
            ReviewHelpMessage(
                R.string.review_help_other_individuality_title,
                R.string.review_help_other_individuality_message,
            ),
        ReviewItemId.OTHER_CAUTIONS to
            ReviewHelpMessage(
                R.string.review_help_other_cautions_title,
                R.string.review_help_other_cautions_message,
            ),
        ReviewItemId.OTHER_SAKE_TYPES to
            ReviewHelpMessage(
                R.string.review_help_other_sake_types_title,
                R.string.review_help_other_sake_types_message,
                boldPrefixBeforeColon = true,
            ),
        ReviewItemId.OTHER_FREE_COMMENT to
            ReviewHelpMessage(
                R.string.review_help_other_free_comment_title,
                R.string.review_help_other_free_comment_message,
            ),
        ReviewItemId.OTHER_OVERALL_REVIEW to
            ReviewHelpMessage(
                R.string.review_help_other_overall_review_title,
                R.string.review_help_other_overall_review_message,
            ),
    )
