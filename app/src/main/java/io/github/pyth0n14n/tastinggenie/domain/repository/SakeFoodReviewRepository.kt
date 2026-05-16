package io.github.pyth0n14n.tastinggenie.domain.repository

import io.github.pyth0n14n.tastinggenie.domain.model.SakeFoodReview
import io.github.pyth0n14n.tastinggenie.domain.model.SakeFoodReviewId
import io.github.pyth0n14n.tastinggenie.domain.model.SakeFoodReviewInput
import io.github.pyth0n14n.tastinggenie.domain.model.SakeId
import kotlinx.coroutines.flow.Flow

interface SakeFoodReviewRepository {
    fun observeFoodReviews(sakeId: SakeId): Flow<List<SakeFoodReview>>

    suspend fun getFoodReview(id: SakeFoodReviewId): SakeFoodReview?

    suspend fun upsertFoodReview(input: SakeFoodReviewInput): SakeFoodReviewId

    suspend fun deleteFoodReview(id: SakeFoodReviewId): Boolean
}
