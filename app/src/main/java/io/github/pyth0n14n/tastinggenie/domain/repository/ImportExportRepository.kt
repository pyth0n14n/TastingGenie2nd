package io.github.pyth0n14n.tastinggenie.domain.repository

interface ImportExportRepository {
    suspend fun exportBackup(): Result<ByteArray>

    suspend fun importBackup(rawZip: ByteArray): Result<Unit>
}
