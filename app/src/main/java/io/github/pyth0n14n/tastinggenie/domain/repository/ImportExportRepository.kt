package io.github.pyth0n14n.tastinggenie.domain.repository

import java.io.InputStream
import java.io.OutputStream

interface ImportExportRepository {
    suspend fun exportBackup(output: OutputStream): Result<Unit>

    suspend fun restoreBackup(input: InputStream): Result<Unit>
}
