package io.github.pyth0n14n.tastinggenie.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.pyth0n14n.tastinggenie.domain.model.AppSettings
import io.github.pyth0n14n.tastinggenie.domain.model.ReviewMode
import io.github.pyth0n14n.tastinggenie.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@Suppress("TooManyFunctions")
class SettingsRepositoryImpl
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) : SettingsRepository {
        override fun observeSettings(): Flow<AppSettings> =
            dataStore.data.map { preferences ->
                preferences.toAppSettings()
            }

        override suspend fun getCurrentSettings(): AppSettings = dataStore.data.first().toAppSettings()

        override suspend fun updateShowHelpHints(enabled: Boolean) {
            dataStore.edit { preferences ->
                preferences[showHelpHintsKey] = enabled
            }
        }

        override suspend fun updateShowReviewSoundness(enabled: Boolean) {
            dataStore.edit { preferences ->
                preferences[showReviewSoundnessKey] = enabled
            }
        }

        override suspend fun updateReviewMode(modeId: String) {
            dataStore.edit { preferences ->
                preferences[reviewModeIdKey] = modeId
            }
        }

        override suspend fun updateOnboardingCompleted(completed: Boolean) {
            dataStore.edit { preferences ->
                preferences[onboardingCompletedKey] = completed
            }
        }

        override suspend fun updateSakeEmptyFabCoachmarkSeen(seen: Boolean) {
            dataStore.edit { preferences ->
                preferences[sakeEmptyFabCoachmarkSeenKey] = seen
            }
        }

        override suspend fun updateReviewEmptyFabCoachmarkSeen(seen: Boolean) {
            dataStore.edit { preferences ->
                preferences[reviewEmptyFabCoachmarkSeenKey] = seen
            }
        }

        override suspend fun updateHasSeenTastingGuide(seen: Boolean) {
            dataStore.edit { preferences ->
                preferences[hasSeenTastingGuideKey] = seen
            }
        }

        override suspend fun replaceSettings(settings: AppSettings) {
            dataStore.edit { preferences ->
                preferences[showHelpHintsKey] = settings.showHelpHints
                preferences[showReviewSoundnessKey] = settings.showReviewSoundness
                preferences[reviewModeIdKey] = settings.reviewModeId
                preferences[onboardingCompletedKey] = settings.onboardingCompleted
                preferences[sakeEmptyFabCoachmarkSeenKey] = settings.sakeEmptyFabCoachmarkSeen
                preferences[reviewEmptyFabCoachmarkSeenKey] = settings.reviewEmptyFabCoachmarkSeen
                preferences[hasSeenTastingGuideKey] = settings.hasSeenTastingGuide
            }
        }

        private companion object {
            val showHelpHintsKey = booleanPreferencesKey("show_help_hints")
            val showReviewSoundnessKey = booleanPreferencesKey("show_review_soundness")
            val reviewModeIdKey = stringPreferencesKey("review_mode_id")
            val onboardingCompletedKey = booleanPreferencesKey("onboarding_completed")
            val sakeEmptyFabCoachmarkSeenKey = booleanPreferencesKey("sake_empty_fab_coachmark_seen")
            val reviewEmptyFabCoachmarkSeenKey = booleanPreferencesKey("review_empty_fab_coachmark_seen")
            val hasSeenTastingGuideKey = booleanPreferencesKey("has_seen_tasting_guide")
        }

        private fun Preferences.toAppSettings(): AppSettings =
            AppSettings(
                showHelpHints = this[showHelpHintsKey] ?: true,
                showReviewSoundness = this[showReviewSoundnessKey] ?: false,
                reviewModeId = this[reviewModeIdKey] ?: ReviewMode.NORMAL.id,
                onboardingCompleted = this[onboardingCompletedKey] ?: false,
                sakeEmptyFabCoachmarkSeen = this[sakeEmptyFabCoachmarkSeenKey] ?: false,
                reviewEmptyFabCoachmarkSeen = this[reviewEmptyFabCoachmarkSeenKey] ?: false,
                hasSeenTastingGuide = this[hasSeenTastingGuideKey] ?: false,
            )
    }
