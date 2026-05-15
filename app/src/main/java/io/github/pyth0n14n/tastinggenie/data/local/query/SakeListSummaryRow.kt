package io.github.pyth0n14n.tastinggenie.data.local.query

import androidx.room.Embedded
import io.github.pyth0n14n.tastinggenie.data.local.entity.SakeEntity

data class SakeListSummaryRow(
    @Embedded
    val sake: SakeEntity,
    val averageOverallReview: Double?,
)
