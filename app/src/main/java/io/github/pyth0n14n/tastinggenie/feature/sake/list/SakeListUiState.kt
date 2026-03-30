package io.github.pyth0n14n.tastinggenie.feature.sake.list

import io.github.pyth0n14n.tastinggenie.domain.model.Sake
import io.github.pyth0n14n.tastinggenie.domain.model.UiError

data class SakeListUiState(
    val isLoading: Boolean = true,
    val error: UiError? = null,
    val sakes: List<Sake> = emptyList(),
    val gradeLabels: Map<String, String> = emptyMap(),
    val showImagePreview: Boolean = true,
)
