package io.github.pyth0n14n.tastinggenie.feature.review.edit

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.github.pyth0n14n.tastinggenie.domain.model.AromaCategoryMaster
import io.github.pyth0n14n.tastinggenie.ui.common.DropdownOptionGroup

@Composable
fun reviewTextResource(
    @StringRes labelRes: Int,
): String = stringResource(labelRes)

fun List<AromaCategoryMaster>.toDropdownGroups(): List<DropdownOptionGroup> =
    map { category ->
        DropdownOptionGroup(
            label = category.label,
            options = category.items.toOptions(),
        )
    }
