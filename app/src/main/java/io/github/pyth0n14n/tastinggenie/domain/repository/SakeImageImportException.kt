package io.github.pyth0n14n.tastinggenie.domain.repository

import java.io.IOException

sealed class SakeImageImportException(
    message: String,
) : IOException(message) {
    class UnsupportedMimeType(
        mimeType: String?,
    ) : SakeImageImportException("Unsupported image MIME type: ${mimeType ?: "unknown"}")

    class ImageTooLarge(
        maxBytes: Long,
    ) : SakeImageImportException("Image exceeds maximum size: $maxBytes bytes")
}
