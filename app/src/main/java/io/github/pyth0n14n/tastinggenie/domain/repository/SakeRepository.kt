package io.github.pyth0n14n.tastinggenie.domain.repository

import io.github.pyth0n14n.tastinggenie.domain.model.Sake
import io.github.pyth0n14n.tastinggenie.domain.model.SakeId
import io.github.pyth0n14n.tastinggenie.domain.model.SakeInput
import kotlinx.coroutines.flow.Flow

/**
 * 酒情報へのアクセスを提供するRepository。
 */
interface SakeRepository {
    fun observeSakes(): Flow<List<Sake>>

    suspend fun getSake(id: SakeId): Sake?

    suspend fun upsertSake(input: SakeInput): SakeId
}
