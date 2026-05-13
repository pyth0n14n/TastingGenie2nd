package io.github.pyth0n14n.tastinggenie.data.repository

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.File
import java.io.FileNotFoundException

class TestImageContentProvider : ContentProvider() {
    override fun onCreate(): Boolean = true

    override fun getType(uri: Uri): String? = images[uri]?.mimeType

    override fun openFile(
        uri: Uri,
        mode: String,
    ): ParcelFileDescriptor {
        val image = images[uri] ?: throw FileNotFoundException(uri.toString())
        return ParcelFileDescriptor.open(image.file, ParcelFileDescriptor.MODE_READ_ONLY)
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? = null

    override fun insert(
        uri: Uri,
        values: ContentValues?,
    ): Uri? = null

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0

    companion object {
        private const val AUTHORITY = "io.github.pyth0n14n.tastinggenie.testimages"
        private val images = mutableMapOf<Uri, TestImage>()

        fun register(
            file: File,
            mimeType: String,
        ): Uri {
            val uri =
                Uri.Builder()
                    .scheme("content")
                    .authority(AUTHORITY)
                    .appendPath(file.nameWithoutExtension)
                    .appendPath(file.name)
                    .build()
            images[uri] = TestImage(file = file, mimeType = mimeType)
            return uri
        }
    }

    private data class TestImage(
        val file: File,
        val mimeType: String,
    )
}
