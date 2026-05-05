package io.github.pyth0n14n.tastinggenie.domain.repository

import io.github.pyth0n14n.tastinggenie.domain.model.ReviewItemId
import io.github.pyth0n14n.tastinggenie.domain.model.ReviewModeDefinition
import kotlinx.coroutines.flow.Flow

interface ReviewModeRepository {
    fun observeModes(): Flow<List<ReviewModeDefinition>>

    fun observeEnabledItemIds(modeId: String): Flow<Set<ReviewItemId>>

    suspend fun ensureBuiltInModes()
}
