package io.github.pyth0n14n.tastinggenie.domain.repository

import io.github.pyth0n14n.tastinggenie.domain.model.MasterDataBundle

/**
 * マスタデータを提供するRepository。
 */
interface MasterDataRepository {
    suspend fun getMasterData(): MasterDataBundle
}
