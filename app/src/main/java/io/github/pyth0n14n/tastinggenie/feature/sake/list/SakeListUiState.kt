package io.github.pyth0n14n.tastinggenie.feature.sake.list

import io.github.pyth0n14n.tastinggenie.domain.model.SakeListSummary
import io.github.pyth0n14n.tastinggenie.domain.model.UiError

data class SakeListUiState(
    val isLoading: Boolean = true,
    val error: UiError? = null,
    val deleteError: UiError? = null,
    val pendingDeleteSake: PendingDeleteSake? = null,
    val searchQuery: String = "",
    val sortMode: SakeListSortMode = SakeListSortMode.DEFAULT,
    val sakes: List<SakeListSummary> = emptyList(),
    val gradeLabels: Map<String, String> = emptyMap(),
    val classificationLabels: Map<String, String> = emptyMap(),
    val prefectureLabels: Map<String, String> = emptyMap(),
    val overallReviewLabels: Map<String, String> = emptyMap(),
    val showHelpHints: Boolean = true,
) {
    val displayedSakes: List<SakeListSummary>
        get() = sortedSakes(filteredSakes())

    private fun filteredSakes(): List<SakeListSummary> {
        val query = searchQuery.trim()
        if (query.isBlank()) {
            return sakes
        }
        return sakes.filter { summary ->
            val sake = summary.sake
            sake.name.contains(query, ignoreCase = true) ||
                sake.maker.orEmpty().contains(query, ignoreCase = true)
        }
    }

    private fun sortedSakes(source: List<SakeListSummary>): List<SakeListSummary> =
        when (sortMode) {
            SakeListSortMode.DEFAULT ->
                source.sortedByDescending { it.sake.id }

            SakeListSortMode.NAME_ASC ->
                source.sortedBy { it.sake.name }

            SakeListSortMode.RATING_DESC ->
                source.sortedWith(
                    compareByDescending<SakeListSummary> { it.averageOverallReview ?: -1.0 }
                        .thenBy { it.sake.name },
                )
        }
}

enum class SakeListSortMode {
    DEFAULT,
    NAME_ASC,
    RATING_DESC,
}

data class PendingDeleteSake(
    val sakeId: Long,
    val sakeName: String,
    val reviewCount: Int,
    val hasImage: Boolean,
)
