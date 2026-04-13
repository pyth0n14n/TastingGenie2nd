package io.github.pyth0n14n.tastinggenie.domain.repository

interface SakeImageRepository {
    suspend fun importImage(sourceUri: String): String

    suspend fun deleteImage(imageUri: String?)

    suspend fun cleanupUnusedImages(): Int
}
