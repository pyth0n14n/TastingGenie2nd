package io.github.pyth0n14n.tastinggenie.data.repository

import androidx.room.withTransaction
import io.github.pyth0n14n.tastinggenie.data.local.AppDatabase
import io.github.pyth0n14n.tastinggenie.data.local.dao.ReviewDao
import io.github.pyth0n14n.tastinggenie.data.local.dao.SakeDao
import io.github.pyth0n14n.tastinggenie.data.mapper.toDomain
import io.github.pyth0n14n.tastinggenie.data.mapper.toEntity
import io.github.pyth0n14n.tastinggenie.di.IoDispatcher
import io.github.pyth0n14n.tastinggenie.domain.model.Sake
import io.github.pyth0n14n.tastinggenie.domain.model.SakeDeleteResult
import io.github.pyth0n14n.tastinggenie.domain.model.SakeId
import io.github.pyth0n14n.tastinggenie.domain.model.SakeInput
import io.github.pyth0n14n.tastinggenie.domain.repository.SakeImageRepository
import io.github.pyth0n14n.tastinggenie.domain.repository.SakeRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SakeRepositoryImpl
    @Inject
    constructor(
        private val database: AppDatabase,
        private val sakeDao: SakeDao,
        private val reviewDao: ReviewDao,
        private val sakeImageRepository: SakeImageRepository,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : SakeRepository {
        override fun observeSakes(): Flow<List<Sake>> = sakeDao.observeAll().map { list -> list.map { it.toDomain() } }

        override suspend fun getSake(id: SakeId): Sake? = withContext(ioDispatcher) { sakeDao.getById(id)?.toDomain() }

        override suspend fun upsertSake(input: SakeInput): SakeId =
            withContext(ioDispatcher) {
                val entity = input.toEntity()
                val id = input.id
                if (id == null) {
                    sakeDao.insert(entity)
                } else {
                    val updated = sakeDao.update(entity)
                    if (updated > 0) {
                        id
                    } else {
                        sakeDao.insert(entity)
                    }
                }
            }

        @Suppress("TooGenericExceptionCaught")
        override suspend fun deleteSake(id: SakeId): SakeDeleteResult =
            withContext(ioDispatcher) {
                val existing = sakeDao.getById(id) ?: return@withContext SakeDeleteResult(isDeleted = false)
                val deleted =
                    database.withTransaction {
                        reviewDao.deleteBySakeId(id)
                        sakeDao.deleteById(id) > 0
                    }
                if (!deleted) {
                    return@withContext SakeDeleteResult(isDeleted = false)
                }
                val cleanupFailure =
                    try {
                        sakeImageRepository.deleteImage(existing.imageUri)
                        null
                    } catch (throwable: CancellationException) {
                        throw throwable
                    } catch (throwable: Exception) {
                        throwable
                    }
                SakeDeleteResult(
                    isDeleted = true,
                    hasImageCleanupError = cleanupFailure != null,
                    imageCleanupErrorCauseKey = cleanupFailure?.message,
                )
            }
    }
