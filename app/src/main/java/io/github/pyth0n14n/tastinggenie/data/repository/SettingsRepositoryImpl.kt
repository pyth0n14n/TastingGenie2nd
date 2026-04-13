package io.github.pyth0n14n.tastinggenie.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import io.github.pyth0n14n.tastinggenie.domain.model.AppSettings
import io.github.pyth0n14n.tastinggenie.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

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

        override suspend fun updateShowImagePreview(enabled: Boolean) {
            dataStore.edit { preferences ->
                preferences[showImagePreviewKey] = enabled
            }
        }

        override suspend fun updateShowReviewSoundness(enabled: Boolean) {
            dataStore.edit { preferences ->
                preferences[showReviewSoundnessKey] = enabled
            }
        }

        override suspend fun updateAutoDeleteUnusedImages(enabled: Boolean) {
            dataStore.edit { preferences ->
                preferences[autoDeleteUnusedImagesKey] = enabled
            }
        }

        private companion object {
            val showHelpHintsKey = booleanPreferencesKey("show_help_hints")
            val showImagePreviewKey = booleanPreferencesKey("show_image_preview")
            val showReviewSoundnessKey = booleanPreferencesKey("show_review_soundness")
            val autoDeleteUnusedImagesKey = booleanPreferencesKey("auto_delete_unused_images")
        }

        private fun Preferences.toAppSettings(): AppSettings =
            AppSettings(
                showHelpHints = this[showHelpHintsKey] ?: true,
                showImagePreview = this[showImagePreviewKey] ?: true,
                showReviewSoundness = this[showReviewSoundnessKey] ?: true,
                autoDeleteUnusedImages = this[autoDeleteUnusedImagesKey] ?: false,
            )
    }
