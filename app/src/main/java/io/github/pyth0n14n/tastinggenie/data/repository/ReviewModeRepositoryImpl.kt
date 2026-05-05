package io.github.pyth0n14n.tastinggenie.data.repository

import io.github.pyth0n14n.tastinggenie.data.local.dao.ReviewModeDao
import io.github.pyth0n14n.tastinggenie.data.local.entity.ReviewModeEntity
import io.github.pyth0n14n.tastinggenie.data.local.entity.ReviewModeItemEntity
import io.github.pyth0n14n.tastinggenie.di.IoDispatcher
import io.github.pyth0n14n.tastinggenie.domain.model.ReviewItemId
import io.github.pyth0n14n.tastinggenie.domain.model.ReviewMode
import io.github.pyth0n14n.tastinggenie.domain.model.ReviewModeDefinition
import io.github.pyth0n14n.tastinggenie.domain.model.builtInReviewModeDefinitions
import io.github.pyth0n14n.tastinggenie.domain.repository.ReviewModeRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ReviewModeRepositoryImpl
    @Inject
    constructor(
        private val dao: ReviewModeDao,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ReviewModeRepository {
        override fun observeModes(): Flow<List<ReviewModeDefinition>> =
            dao.observeModes().map { modes ->
                val builtIns = builtInReviewModeDefinitions.associateBy { it.id }
                modes.map { mode ->
                    builtIns[mode.id] ?: ReviewModeDefinition(
                        id = mode.id,
                        label = mode.label,
                        isBuiltIn = mode.isBuiltIn,
                        enabledItemIds = emptySet(),
                    )
                }
            }

        override fun observeEnabledItemIds(modeId: String): Flow<Set<ReviewItemId>> =
            dao.observeEnabledItemIds(modeId).map { rawIds ->
                rawIds
                    .mapNotNull { rawId -> runCatching { ReviewItemId.valueOf(rawId) }.getOrNull() }
                    .toSet()
                    .ifEmpty { fallbackItems(modeId) }
            }

        override suspend fun ensureBuiltInModes() {
            withContext(ioDispatcher) {
                dao.upsertModeDefinitions(
                    modes =
                        builtInReviewModeDefinitions.map { mode ->
                            ReviewModeEntity(
                                id = mode.id,
                                label = mode.label,
                                isBuiltIn = mode.isBuiltIn,
                            )
                        },
                    items =
                        builtInReviewModeDefinitions.flatMap { mode ->
                            mode.enabledItemIds.map { itemId ->
                                ReviewModeItemEntity(
                                    modeId = mode.id,
                                    itemId = itemId.name,
                                    isEnabled = true,
                                )
                            }
                        },
                )
            }
        }

        private fun fallbackItems(modeId: String): Set<ReviewItemId> =
            builtInReviewModeDefinitions.firstOrNull { it.id == modeId }?.enabledItemIds
                ?: builtInReviewModeDefinitions.first { it.id == ReviewMode.NORMAL.id }.enabledItemIds
    }
