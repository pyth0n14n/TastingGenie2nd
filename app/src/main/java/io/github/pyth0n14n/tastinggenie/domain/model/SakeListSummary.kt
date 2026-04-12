package io.github.pyth0n14n.tastinggenie.domain.model

import io.github.pyth0n14n.tastinggenie.domain.model.enums.OverallReview

data class SakeListSummary(
    val sake: Sake,
    val latestOverallReview: OverallReview? = null,
)
