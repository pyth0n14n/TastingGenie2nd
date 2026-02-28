package io.github.pyth0n14n.tastinggenie.data.repository

import io.github.pyth0n14n.tastinggenie.data.master.AssetTextSource
import io.github.pyth0n14n.tastinggenie.data.master.parseMasterData
import io.github.pyth0n14n.tastinggenie.di.IoDispatcher
import io.github.pyth0n14n.tastinggenie.domain.model.MasterDataBundle
import io.github.pyth0n14n.tastinggenie.domain.repository.MasterDataRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject

class MasterDataRepositoryImpl
    @Inject
    constructor(
        private val source: AssetTextSource,
        private val json: Json,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : MasterDataRepository {
        private val mutex = Mutex()

        @Volatile
        private var cache: MasterDataBundle? = null

        override suspend fun getMasterData(): MasterDataBundle {
            val cached = cache
            if (cached != null) {
                return cached
            }
            return mutex.withLock {
                val recheck = cache
                if (recheck != null) {
                    recheck
                } else {
                    withContext(ioDispatcher) {
                        parseMasterData(source = source, json = json).also { parsed -> cache = parsed }
                    }
                }
            }
        }
    }
