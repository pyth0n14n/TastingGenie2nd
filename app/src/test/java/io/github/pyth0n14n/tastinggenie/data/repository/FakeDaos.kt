package io.github.pyth0n14n.tastinggenie.data.repository

import io.github.pyth0n14n.tastinggenie.data.local.dao.ReviewDao
import io.github.pyth0n14n.tastinggenie.data.local.dao.SakeDao
import io.github.pyth0n14n.tastinggenie.data.local.entity.ReviewEntity
import io.github.pyth0n14n.tastinggenie.data.local.entity.SakeEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeSakeDao : SakeDao {
    private val entries = mutableListOf<SakeEntity>()
    private val stream = MutableStateFlow<List<SakeEntity>>(emptyList())
    private var nextId: Long = 1L

    override fun observeAll(): Flow<List<SakeEntity>> = stream.map { list -> list.sortedBy { it.name } }

    override suspend fun getById(id: Long): SakeEntity? = entries.firstOrNull { it.id == id }

    override suspend fun getAllOnce(): List<SakeEntity> = entries.toList()

    override suspend fun insert(entity: SakeEntity): Long {
        val targetId = if (entity.id == 0L) nextId++ else entity.id
        entries.removeAll { it.id == targetId }
        entries.add(entity.copy(id = targetId))
        emit()
        return targetId
    }

    override suspend fun insertAll(entities: List<SakeEntity>) {
        entities.forEach { insert(it) }
    }

    override suspend fun update(entity: SakeEntity): Int {
        val index = entries.indexOfFirst { it.id == entity.id }
        return if (index >= 0) {
            entries[index] = entity
            emit()
            1
        } else {
            0
        }
    }

    private fun emit() {
        stream.value = entries.toList()
    }
}

class FakeReviewDao : ReviewDao {
    private val entries = mutableListOf<ReviewEntity>()
    private val stream = MutableStateFlow<List<ReviewEntity>>(emptyList())
    private var nextId: Long = 1L

    override fun observeBySakeId(sakeId: Long): Flow<List<ReviewEntity>> =
        stream.map { list ->
            list
                .filter {
                    it.sakeId == sakeId
                }.sortedByDescending { it.dateEpochDay }
        }

    override suspend fun getById(id: Long): ReviewEntity? = entries.firstOrNull { it.id == id }

    override suspend fun getAllOnce(): List<ReviewEntity> = entries.toList()

    override suspend fun insert(entity: ReviewEntity): Long {
        val targetId = if (entity.id == 0L) nextId++ else entity.id
        entries.removeAll { it.id == targetId }
        entries.add(entity.copy(id = targetId))
        emit()
        return targetId
    }

    override suspend fun insertAll(entities: List<ReviewEntity>) {
        entities.forEach { insert(it) }
    }

    override suspend fun update(entity: ReviewEntity): Int {
        val index = entries.indexOfFirst { it.id == entity.id }
        return if (index >= 0) {
            entries[index] = entity
            emit()
            1
        } else {
            0
        }
    }

    private fun emit() {
        stream.value = entries.toList()
    }
}
