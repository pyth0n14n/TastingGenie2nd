package io.github.pyth0n14n.tastinggenie.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.github.pyth0n14n.tastinggenie.data.local.entity.SakeFoodReviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SakeFoodReviewDao {
    @Query("SELECT * FROM sake_food_reviews WHERE sakeId = :sakeId ORDER BY dateEpochDay DESC, id DESC")
    fun observeBySakeId(sakeId: Long): Flow<List<SakeFoodReviewEntity>>

    @Query("SELECT * FROM sake_food_reviews WHERE id = :id")
    suspend fun getById(id: Long): SakeFoodReviewEntity?

    @Query("SELECT * FROM sake_food_reviews ORDER BY id ASC")
    suspend fun getAllOnce(): List<SakeFoodReviewEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SakeFoodReviewEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<SakeFoodReviewEntity>)

    @Query("DELETE FROM sake_food_reviews")
    suspend fun deleteAll()

    @Update
    suspend fun update(entity: SakeFoodReviewEntity): Int

    @Query("DELETE FROM sake_food_reviews WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("DELETE FROM sake_food_reviews WHERE sakeId = :sakeId")
    suspend fun deleteBySakeId(sakeId: Long): Int
}
