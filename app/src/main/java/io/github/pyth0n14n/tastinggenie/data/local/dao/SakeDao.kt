package io.github.pyth0n14n.tastinggenie.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.github.pyth0n14n.tastinggenie.data.local.entity.SakeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SakeDao {
    @Query("SELECT * FROM sakes ORDER BY name ASC")
    fun observeAll(): Flow<List<SakeEntity>>

    @Query("SELECT * FROM sakes WHERE id = :id")
    suspend fun getById(id: Long): SakeEntity?

    @Query("SELECT * FROM sakes ORDER BY id ASC")
    suspend fun getAllOnce(): List<SakeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SakeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<SakeEntity>)

    @Update
    suspend fun update(entity: SakeEntity): Int
}
