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
    fun observeSettings_defaultsToEnabledFlags() =
        runTest {
            val repository = createRepository()

            assertEquals(AppSettings(), repository.observeSettings().first())
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
