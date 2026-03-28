package io.github.pyth0n14n.tastinggenie.data.repository

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SakeImageRepositoryImplTest {
    @Test
    fun importImage_copiesIntoManagedDirectoryAndDeletesPreviousImage() =
        runTest {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val repository = SakeImageRepositoryImpl(context = context, ioDispatcher = Dispatchers.Unconfined)
            val firstSource = createSourceFile(context, name = "first.jpg", content = "first")
            val firstUri = repository.importImage(sourceUri = Uri.fromFile(firstSource).toString())
            val firstManagedFile = requireNotNull(Uri.parse(firstUri).path).let(::File)

            val secondSource = createSourceFile(context, name = "second.jpg", content = "second")
            val secondUri =
                repository.importImage(
                    sourceUri = Uri.fromFile(secondSource).toString(),
                    previousImageUri = firstUri,
                )
            val secondManagedFile = requireNotNull(Uri.parse(secondUri).path).let(::File)

            assertFalse(firstManagedFile.exists())
            assertTrue(secondManagedFile.exists())
            assertEquals("second", secondManagedFile.readText())
        }

    @Test
    fun deleteImage_ignoresFilesOutsideManagedDirectory() =
        runTest {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val repository = SakeImageRepositoryImpl(context = context, ioDispatcher = Dispatchers.Unconfined)
            val unmanagedFile = createSourceFile(context, name = "outside.jpg", content = "keep")

            repository.deleteImage(Uri.fromFile(unmanagedFile).toString())

            assertTrue(unmanagedFile.exists())
        }

    private fun createSourceFile(
        context: Context,
        name: String,
        content: String,
    ): File =
        File(context.cacheDir, name).apply {
            parentFile?.mkdirs()
            writeText(content)
        }
}
