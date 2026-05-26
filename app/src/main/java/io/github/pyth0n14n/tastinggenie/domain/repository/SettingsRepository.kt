package io.github.pyth0n14n.tastinggenie.domain.repository

import io.github.pyth0n14n.tastinggenie.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeSettings(): Flow<AppSettings>

    suspend fun getCurrentSettings(): AppSettings

    suspend fun updateShowHelpHints(enabled: Boolean)

    suspend fun updateShowReviewSoundness(enabled: Boolean)

    suspend fun updateReviewMode(modeId: String)

    suspend fun updateOnboardingCompleted(completed: Boolean) = Unit

    suspend fun updateSakeEmptyFabCoachmarkSeen(seen: Boolean) = Unit

    suspend fun updateReviewEmptyFabCoachmarkSeen(seen: Boolean) = Unit

    suspend fun updateHasSeenTastingGuide(seen: Boolean) = Unit

    suspend fun replaceSettings(settings: AppSettings)
}
