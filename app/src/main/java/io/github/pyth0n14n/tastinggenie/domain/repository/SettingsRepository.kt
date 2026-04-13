package io.github.pyth0n14n.tastinggenie.domain.repository

import io.github.pyth0n14n.tastinggenie.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeSettings(): Flow<AppSettings>

    suspend fun getCurrentSettings(): AppSettings

    suspend fun updateShowHelpHints(enabled: Boolean)

    suspend fun updateShowImagePreview(enabled: Boolean)

    suspend fun updateShowReviewSoundness(enabled: Boolean)

    suspend fun updateAutoDeleteUnusedImages(enabled: Boolean)
}
