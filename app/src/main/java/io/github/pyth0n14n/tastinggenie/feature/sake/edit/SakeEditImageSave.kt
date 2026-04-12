package io.github.pyth0n14n.tastinggenie.feature.sake.edit

import io.github.pyth0n14n.tastinggenie.domain.model.SakeInput
import io.github.pyth0n14n.tastinggenie.domain.repository.SakeImageRepository
import io.github.pyth0n14n.tastinggenie.domain.repository.SakeRepository

internal suspend fun saveSake(
    snapshot: SakeEditUiState,
    input: SakeInput,
    sakeRepository: SakeRepository,
    sakeImageRepository: SakeImageRepository,
) {
    val previousImageUri = snapshot.persistedImageUri
    var importedImageUri: String? = null
    var isCommitted = false
    try {
        val resolvedImageUri =
            when {
                !snapshot.pendingImageSourceUri.isNullOrBlank() -> {
                    importedImageUri =
                        sakeImageRepository.importImage(
                            sourceUri = snapshot.pendingImageSourceUri,
                        )
                    importedImageUri
                }

                snapshot.isImageMarkedForDeletion -> null
                else -> previousImageUri
            }
        sakeRepository.upsertSake(input.copy(imageUri = resolvedImageUri))
        isCommitted = true
        cleanupCommittedImageMutation(
            snapshot = snapshot,
            previousImageUri = previousImageUri,
            importedImageUri = importedImageUri,
            sakeImageRepository = sakeImageRepository,
        )
    } finally {
        if (!isCommitted && importedImageUri != null) {
            // If save failed after importing a managed image, remove the orphaned copy.
            sakeImageRepository.deleteImage(importedImageUri)
        }
    }
}

private suspend fun cleanupCommittedImageMutation(
    snapshot: SakeEditUiState,
    previousImageUri: String?,
    importedImageUri: String?,
    sakeImageRepository: SakeImageRepository,
) {
    // Old-image cleanup runs after the DB commit, so failures here must not flip the save result.
    runCatching {
        when {
            !snapshot.pendingImageSourceUri.isNullOrBlank() -> {
                if (!previousImageUri.isNullOrBlank() && previousImageUri != importedImageUri) {
                    sakeImageRepository.deleteImage(previousImageUri)
                }
                sakeImageRepository.deleteImage(snapshot.pendingImageSourceUri)
            }

            snapshot.isImageMarkedForDeletion -> sakeImageRepository.deleteImage(previousImageUri)
        }
    }
}
