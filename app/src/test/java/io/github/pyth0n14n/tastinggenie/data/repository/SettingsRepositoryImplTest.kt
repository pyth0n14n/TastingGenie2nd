package io.github.pyth0n14n.tastinggenie.data.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import io.github.pyth0n14n.tastinggenie.domain.model.AppSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.file.Files
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsRepositoryImplTest {
    @Test
    fun observeSettings_defaultsToConfiguredFlags() =
        runTest {
            val repository = createRepository()

            assertEquals(AppSettings(), repository.observeSettings().first())
        }

    @Test
    fun updateOnboardingCompleted_persistsValue() =
        runTest {
            val repository = createRepository()

            repository.updateOnboardingCompleted(completed = true)

            assertEquals(AppSettings(onboardingCompleted = true), repository.observeSettings().first())
        }

    @Test
    fun updateSakeEmptyFabCoachmarkSeen_persistsValue() =
        runTest {
            val repository = createRepository()

            repository.updateSakeEmptyFabCoachmarkSeen(seen = true)

            assertEquals(
                AppSettings(sakeEmptyFabCoachmarkSeen = true),
                repository.observeSettings().first(),
            )
        }

    @Test
    fun updateReviewEmptyFabCoachmarkSeen_persistsValue() =
        runTest {
            val repository = createRepository()

            repository.updateReviewEmptyFabCoachmarkSeen(seen = true)

            assertEquals(
                AppSettings(reviewEmptyFabCoachmarkSeen = true),
                repository.observeSettings().first(),
            )
        }

    @Test
    fun updateHasSeenTastingGuide_persistsValue() =
        runTest {
            val repository = createRepository()

            repository.updateHasSeenTastingGuide(seen = true)

            assertEquals(
                AppSettings(hasSeenTastingGuide = true),
                repository.observeSettings().first(),
            )
        }

    @Test
    fun replaceSettings_persistsOnboardingAndCoachmarkFlags() =
        runTest {
            val repository = createRepository()
            val settings =
                AppSettings(
                    showHelpHints = false,
                    showReviewSoundness = true,
                    onboardingCompleted = true,
                    sakeEmptyFabCoachmarkSeen = true,
                    reviewEmptyFabCoachmarkSeen = true,
                    hasSeenTastingGuide = true,
                )

            repository.replaceSettings(settings)

            assertEquals(settings, repository.observeSettings().first())
        }

    private fun createRepository(
        fileName: String = "settings-test-${UUID.randomUUID()}.preferences_pb",
    ): SettingsRepositoryImpl {
        val tempDirectory = Files.createTempDirectory("settings-datastore")
        val dataStore =
            PreferenceDataStoreFactory.create(
                produceFile = { tempDirectory.resolve(fileName).toFile() },
            )
        return SettingsRepositoryImpl(dataStore = dataStore)
    }
}
