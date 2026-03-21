package io.github.pyth0n14n.tastinggenie.domain.repository

interface ImportExportRepository {
    suspend fun exportJson(): Result<String>

    suspend fun importJson(rawJson: String): Result<Unit>
}
