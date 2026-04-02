package io.github.pyth0n14n.tastinggenie.domain.repository

import io.github.pyth0n14n.tastinggenie.domain.model.ManagedSakeImage

interface SakeImageRepository {
    suspend fun importImage(
        sourceUri: String,
        previousImageUri: String? = null,
    ): String

    suspend fun importImageBytes(
        filenameHint: String,
        bytes: ByteArray,
        previousImageUri: String? = null,
    ): String

    suspend fun exportImage(imageUri: String?): ManagedSakeImage?

    suspend fun deleteImage(imageUri: String?)
}
