package io.github.pyth0n14n.tastinggenie.domain.repository

import io.github.pyth0n14n.tastinggenie.domain.model.Review
import io.github.pyth0n14n.tastinggenie.domain.model.ReviewId
import io.github.pyth0n14n.tastinggenie.domain.model.ReviewInput
import io.github.pyth0n14n.tastinggenie.domain.model.SakeId
import kotlinx.coroutines.flow.Flow

/**
 * レビュー情報へのアクセスを提供するRepository。
 */
interface ReviewRepository {
    fun observeReviews(sakeId: SakeId): Flow<List<Review>>

    suspend fun getReview(id: ReviewId): Review?

    suspend fun upsertReview(input: ReviewInput): ReviewId
}
