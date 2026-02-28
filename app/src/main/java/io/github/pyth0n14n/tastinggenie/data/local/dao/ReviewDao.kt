package io.github.pyth0n14n.tastinggenie.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.github.pyth0n14n.tastinggenie.data.local.entity.ReviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE sakeId = :sakeId ORDER BY dateEpochDay DESC, id DESC")
    fun observeBySakeId(sakeId: Long): Flow<List<ReviewEntity>>

    @Query("SELECT * FROM reviews WHERE id = :id")
    suspend fun getById(id: Long): ReviewEntity?

    @Query("SELECT * FROM reviews ORDER BY id ASC")
    suspend fun getAllOnce(): List<ReviewEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ReviewEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ReviewEntity>)

    @Update
    suspend fun update(entity: ReviewEntity): Int
}
