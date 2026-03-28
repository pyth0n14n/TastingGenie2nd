package io.github.pyth0n14n.tastinggenie.domain.repository

interface SakeImageRepository {
    suspend fun importImage(
        sourceUri: String,
        previousImageUri: String? = null,
    ): String

    suspend fun deleteImage(imageUri: String?)
}
