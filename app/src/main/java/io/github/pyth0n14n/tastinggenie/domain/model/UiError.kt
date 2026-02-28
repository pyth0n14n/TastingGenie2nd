package io.github.pyth0n14n.tastinggenie.domain.model

import androidx.annotation.StringRes

/**
 * UI層で表示可能なエラー情報。
 */
data class UiError(
    @StringRes val messageResId: Int,
    val causeKey: String? = null,
)
