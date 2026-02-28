package io.github.pyth0n14n.tastinggenie.data.repository

import io.github.pyth0n14n.tastinggenie.data.local.dao.ReviewDao
import io.github.pyth0n14n.tastinggenie.data.mapper.toDomain
import io.github.pyth0n14n.tastinggenie.data.mapper.toEntity
import io.github.pyth0n14n.tastinggenie.di.IoDispatcher
import io.github.pyth0n14n.tastinggenie.domain.model.Review
import io.github.pyth0n14n.tastinggenie.domain.model.ReviewId
import io.github.pyth0n14n.tastinggenie.domain.model.ReviewInput
import io.github.pyth0n14n.tastinggenie.domain.model.SakeId
import io.github.pyth0n14n.tastinggenie.domain.repository.ReviewRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ReviewRepositoryImpl
    @Inject
    constructor(
        private val reviewDao: ReviewDao,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ReviewRepository {
        override fun observeReviews(sakeId: SakeId): Flow<List<Review>> =
            reviewDao.observeBySakeId(sakeId).map { list -> list.map { it.toDomain() } }

        override suspend fun getReview(id: ReviewId): Review? =
            withContext(ioDispatcher) { reviewDao.getById(id)?.toDomain() }

        override suspend fun upsertReview(input: ReviewInput): ReviewId =
            withContext(ioDispatcher) {
                val entity = input.toEntity()
                val id = input.id
                if (id == null) {
                    reviewDao.insert(entity)
                } else {
                    val updated = reviewDao.update(entity)
                    if (updated > 0) {
                        id
                    } else {
                        reviewDao.insert(entity)
                    }
                }
            }
    }
