package io.github.pyth0n14n.tastinggenie.data.repository

import io.github.pyth0n14n.tastinggenie.data.local.dao.SakeFoodReviewDao
import io.github.pyth0n14n.tastinggenie.data.mapper.toDomain
import io.github.pyth0n14n.tastinggenie.data.mapper.toEntity
import io.github.pyth0n14n.tastinggenie.di.IoDispatcher
import io.github.pyth0n14n.tastinggenie.domain.model.SakeFoodReview
import io.github.pyth0n14n.tastinggenie.domain.model.SakeFoodReviewId
import io.github.pyth0n14n.tastinggenie.domain.model.SakeFoodReviewInput
import io.github.pyth0n14n.tastinggenie.domain.model.SakeId
import io.github.pyth0n14n.tastinggenie.domain.repository.SakeFoodReviewRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SakeFoodReviewRepositoryImpl
    @Inject
    constructor(
        private val foodReviewDao: SakeFoodReviewDao,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : SakeFoodReviewRepository {
        override fun observeFoodReviews(sakeId: SakeId): Flow<List<SakeFoodReview>> =
            foodReviewDao.observeBySakeId(sakeId).map { list -> list.map { it.toDomain() } }

        override suspend fun getFoodReview(id: SakeFoodReviewId): SakeFoodReview? =
            withContext(ioDispatcher) { foodReviewDao.getById(id)?.toDomain() }

        override suspend fun upsertFoodReview(input: SakeFoodReviewInput): SakeFoodReviewId =
            withContext(ioDispatcher) {
                val entity = input.toEntity()
                val id = input.id
                if (id == null) {
                    foodReviewDao.insert(entity)
                } else {
                    val updated = foodReviewDao.update(entity)
                    if (updated > 0) {
                        id
                    } else {
                        foodReviewDao.insert(entity)
                    }
                }
            }

        override suspend fun deleteFoodReview(id: SakeFoodReviewId): Boolean =
            withContext(ioDispatcher) {
                foodReviewDao.deleteById(id) > 0
            }
    }
