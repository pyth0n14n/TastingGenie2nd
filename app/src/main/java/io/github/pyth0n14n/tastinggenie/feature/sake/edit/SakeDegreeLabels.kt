package io.github.pyth0n14n.tastinggenie.feature.sake.edit

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.pyth0n14n.tastinggenie.R

private const val SUPER_SWEET_MAX = -6.0f
private const val SWEET_MIN = -5.9f
private const val SWEET_MAX = -3.5f
private const val SLIGHTLY_SWEET_MIN = -3.4f
private const val SLIGHTLY_SWEET_MAX = -1.5f
private const val SLIGHTLY_DRY_MIN = 1.5f
private const val SLIGHTLY_DRY_MAX = 3.4f
private const val DRY_MIN = 3.5f
private const val DRY_MAX = 5.9f
private const val SUPER_DRY_MIN = 6.0f

@Composable
internal fun SakeDegreeTasteLabel(value: String) {
    val labelRes = sakeDegreeTasteLabelRes(value)
    if (labelRes == null) {
        Spacer(modifier = Modifier.height(20.dp))
    } else {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(2.dp))
    }
}

internal fun sakeDegreePrefix(value: String): String? =
    when {
        value.isBlank() -> null
        value.trimStart().startsWith("-") -> null
        else -> "+"
    }

private fun sakeDegreeTasteLabelRes(value: String): Int? {
    val sakeDegree = value.toFloatOrNull() ?: return null
    return when {
        sakeDegree <= SUPER_SWEET_MAX -> R.string.label_sake_degree_super_sweet
        sakeDegree in SWEET_MIN..SWEET_MAX -> R.string.label_sake_degree_sweet
        sakeDegree in SLIGHTLY_SWEET_MIN..SLIGHTLY_SWEET_MAX -> R.string.label_sake_degree_slightly_sweet
        sakeDegree in SLIGHTLY_DRY_MIN..SLIGHTLY_DRY_MAX -> R.string.label_sake_degree_slightly_dry
        sakeDegree in DRY_MIN..DRY_MAX -> R.string.label_sake_degree_dry
        sakeDegree >= SUPER_DRY_MIN -> R.string.label_sake_degree_super_dry
        else -> R.string.label_sake_degree_medium
    }
}
