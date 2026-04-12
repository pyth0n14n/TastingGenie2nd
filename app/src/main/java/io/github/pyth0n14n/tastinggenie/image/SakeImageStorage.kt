package io.github.pyth0n14n.tastinggenie.image

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

const val SAKE_MANAGED_IMAGE_DIRECTORY = "images/sakes"
const val SAKE_CAPTURE_CACHE_DIRECTORY = "images/sakes/capture"

data class PendingSakeCameraCapture(
    val launchUri: Uri,
    val sourceUri: String,
)

fun Context.createPendingSakeCameraCapture(): PendingSakeCameraCapture {
    val directory = File(cacheDir, SAKE_CAPTURE_CACHE_DIRECTORY).apply { mkdirs() }
    val file = File(directory, "capture-${UUID.randomUUID()}.jpg")
    val launchUri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
    return PendingSakeCameraCapture(
        launchUri = launchUri,
        sourceUri = Uri.fromFile(file).toString(),
    )
}

fun Context.deletePendingSakeCameraCapture(sourceUri: String?) {
    captureFileOrNull(sourceUri)?.delete()
}

fun Context.ownedSakeImageFileOrNull(imageUri: String?): File? {
    val candidate = imageUri.toCanonicalFileOrNull() ?: return null
    val allowedRoots =
        listOf(
            File(filesDir, SAKE_MANAGED_IMAGE_DIRECTORY).canonicalFile,
            File(cacheDir, SAKE_CAPTURE_CACHE_DIRECTORY).canonicalFile,
        )
    return candidate.takeIf { file ->
        allowedRoots.any { root -> file.path.startsWith(root.path + File.separator) }
    }
}

private fun Context.captureFileOrNull(imageUri: String?): File? {
    val candidate = imageUri.toCanonicalFileOrNull() ?: return null
    val captureRoot = File(cacheDir, SAKE_CAPTURE_CACHE_DIRECTORY).canonicalFile
    return candidate.takeIf { file -> file.path.startsWith(captureRoot.path + File.separator) }
}

private fun String?.toCanonicalFileOrNull(): File? {
    val parsed = this?.let(Uri::parse)
    val filePath = parsed?.path?.takeIf { parsed.scheme == "file" }
    return filePath?.let(::File)?.canonicalFile
}
