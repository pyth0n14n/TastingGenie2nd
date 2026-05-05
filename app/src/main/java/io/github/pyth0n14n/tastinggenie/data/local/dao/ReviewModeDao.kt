package io.github.pyth0n14n.tastinggenie.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import io.github.pyth0n14n.tastinggenie.data.local.entity.ReviewModeEntity
import io.github.pyth0n14n.tastinggenie.data.local.entity.ReviewModeItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewModeDao {
    @Query("SELECT * FROM review_modes ORDER BY isBuiltIn DESC, id ASC")
    fun observeModes(): Flow<List<ReviewModeEntity>>

    @Query("SELECT itemId FROM review_mode_items WHERE modeId = :modeId AND isEnabled = 1")
    fun observeEnabledItemIds(modeId: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertModes(modes: List<ReviewModeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertModeItems(items: List<ReviewModeItemEntity>)

    @Transaction
    suspend fun upsertModeDefinitions(
        modes: List<ReviewModeEntity>,
        items: List<ReviewModeItemEntity>,
    ) {
        upsertModes(modes)
        upsertModeItems(items)
    }
}
