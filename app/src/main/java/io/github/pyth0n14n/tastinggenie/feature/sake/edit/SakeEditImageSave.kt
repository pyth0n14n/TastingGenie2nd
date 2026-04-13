package io.github.pyth0n14n.tastinggenie.feature.sake.edit

import io.github.pyth0n14n.tastinggenie.domain.model.SakeInput
import io.github.pyth0n14n.tastinggenie.domain.repository.SakeImageRepository
import io.github.pyth0n14n.tastinggenie.domain.repository.SakeRepository

internal suspend fun saveSake(
    snapshot: SakeEditUiState,
    input: SakeInput,
    sakeRepository: SakeRepository,
    sakeImageRepository: SakeImageRepository,
    autoDeleteUnusedImages: Boolean,
) {
    val importedImageUris = mutableListOf<String>()
    var isCommitted = false
    try {
        val importedImageMap =
            snapshot.pendingImageSourceUris.associateWith { sourceUri ->
                sakeImageRepository.importImage(sourceUri).also { importedUri ->
                    importedImageUris += importedUri
                }
            }
        val resolvedImageUris =
            snapshot.imagePreviewUris
                .map { imageUri -> importedImageMap[imageUri] ?: imageUri }
                .distinct()
        sakeRepository.upsertSake(input.copy(imageUris = resolvedImageUris))
        isCommitted = true
        cleanupCommittedImageMutation(
            snapshot = snapshot,
            sakeImageRepository = sakeImageRepository,
            autoDeleteUnusedImages = autoDeleteUnusedImages,
        )
    } finally {
        if (!isCommitted) {
            importedImageUris.forEach { importedImageUri ->
                // If save failed after importing managed images, remove the orphaned copies.
                sakeImageRepository.deleteImage(importedImageUri)
            }
        }
    }
}

private suspend fun cleanupCommittedImageMutation(
    snapshot: SakeEditUiState,
    sakeImageRepository: SakeImageRepository,
    autoDeleteUnusedImages: Boolean,
) {
    runCatching {
        snapshot.pendingImageSourceUris.forEach { sourceUri ->
            sakeImageRepository.deleteImage(sourceUri)
        }
        if (autoDeleteUnusedImages) {
            sakeImageRepository.cleanupUnusedImages()
        }
    }
}
