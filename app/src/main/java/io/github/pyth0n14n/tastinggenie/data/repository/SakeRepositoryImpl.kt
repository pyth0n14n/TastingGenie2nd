package io.github.pyth0n14n.tastinggenie.data.repository

import io.github.pyth0n14n.tastinggenie.data.local.dao.SakeDao
import io.github.pyth0n14n.tastinggenie.data.mapper.toDomain
import io.github.pyth0n14n.tastinggenie.data.mapper.toEntity
import io.github.pyth0n14n.tastinggenie.di.IoDispatcher
import io.github.pyth0n14n.tastinggenie.domain.model.Sake
import io.github.pyth0n14n.tastinggenie.domain.model.SakeId
import io.github.pyth0n14n.tastinggenie.domain.model.SakeInput
import io.github.pyth0n14n.tastinggenie.domain.repository.SakeRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SakeRepositoryImpl
    @Inject
    constructor(
        private val sakeDao: SakeDao,
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
    }
