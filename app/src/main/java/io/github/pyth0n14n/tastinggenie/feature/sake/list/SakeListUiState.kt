package io.github.pyth0n14n.tastinggenie.feature.sake.list

import io.github.pyth0n14n.tastinggenie.domain.model.SakeListSummary
import io.github.pyth0n14n.tastinggenie.domain.model.UiError

data class SakeListUiState(
    val isLoading: Boolean = true,
    val error: UiError? = null,
    val deleteError: UiError? = null,
    val pendingDeleteSake: PendingDeleteSake? = null,
    val sakes: List<SakeListSummary> = emptyList(),
    val gradeLabels: Map<String, String> = emptyMap(),
    val overallReviewLabels: Map<String, String> = emptyMap(),
    val showHelpHints: Boolean = true,
    val showImagePreview: Boolean = true,
)

data class PendingDeleteSake(
    val sakeId: Long,
    val sakeName: String,
    val reviewCount: Int,
    val hasImage: Boolean,
)
